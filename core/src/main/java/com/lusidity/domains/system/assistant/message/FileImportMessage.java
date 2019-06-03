/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.domains.system.assistant.message;


import com.lusidity.Environment;
import com.lusidity.configuration.SoteriumConfiguration;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.data.field.KeyDataTransformed;
import com.lusidity.data.handler.KeyDataHandlerFileExtension;
import com.lusidity.data.handler.KeyDataHandlerStringToClass;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;
import com.lusidity.jobs.importer.ImporterPreprocessor;
import com.lusidity.workers.importer.BaseImporter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AtSchemaClass(name="FileInfo Import Message", discoverable=false, description="A message about a file used to import data.", writable=true)
public class FileImportMessage extends AssistantMessage
{

	// Fields
	public static final int BYTE_FACTOR=1024;
	private KeyData<Long> bytes = null;
	private KeyData<String> extension=null;
	private KeyData<String> originalName=null;
	private KeyData<String> path=null;
	private KeyData<String> vertexId=null;
	private transient File file=null;
	private KeyDataCollection<Class<? extends BaseImporter>> importerTypes = null;
	private transient Collection<String> historicalTypes=new ArrayList<>();

	// Constructors
	public FileImportMessage()
	{
		super();
	}

	@SuppressWarnings("unused")
	public FileImportMessage(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public JsonData toJson(boolean storing, String... languages)
	{
		JsonData result=super.toJson(storing, languages);
		result.update("kb", this.getFileInKb());
		result.update("mb", this.getFileInMb());

		if (storing)
		{
			result.remove("historicalType");
		}
		else{
			result.update("historicalType", StringX.getLastStrings(this.getHistoricalTypes(), ", ", "."));
		}
		return result;
	}

	public long getFileInKb()
	{
		return (this.fetchBytes().isNotNullOrEmpty()) ? (this.fetchBytes().getValue()/FileImportMessage.BYTE_FACTOR) : 0L;
	}

	public long getFileInMb()
	{
		return (0L==this.getFileInKb()) ?  0L : (this.getFileInKb()/FileImportMessage.BYTE_FACTOR);
	}

	public Collection<String> getHistoricalTypes()
	{
		return this.historicalTypes;
	}

	public KeyData<Long> fetchBytes()
	{
		if(null==this.bytes){
			this.bytes = new KeyData<>(this, "bytes", Long.class, false, null);
		}
		return this.bytes;
	}

	@Override
	public boolean delete()
	{
		File lFile = null;
		try
		{
			lFile=this.getFile();
		}
		catch (Exception ignored){}

		boolean result=false;
		try
		{
			result=super.delete();
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		if (result)
		{
			if ((null!=lFile) && lFile.isFile())
			{
				String dName=StringX.replace(lFile.getName(), ".zip", "");
				File dir=new File(lFile.getParentFile().getAbsolutePath(), dName);
				FileX.deleteRecursively(dir);
			}
			if (null!=lFile)
			{
				FileX.deleteRecursively(lFile);
				result=!lFile.exists();
			}
		}
		return result;
	}

	public File getFile()
	{
		if (null==this.file)
		{
			this.file=new File(this.fetchPath().getValue());
		}
		return this.file;
	}

	public KeyData<String> fetchPath()
	{
		if (null==this.path)
		{
			this.path=new KeyData<>(this, "path", String.class, false, null);
		}
		return this.path;
	}

	// Methods
	public static synchronized FileImportMessage create(String vertexId, String types, String fileName, String originalName, String extension, String path, File file)
	{
		FileImportMessage result=null;
		if (file.exists())
		{
			try
			{
				result=new FileImportMessage();
				result.fetchVertexId().setValue(vertexId);
				result.fetchBytes().setValue(file.length());
				result.fetchTitle().setValue(fileName);
				result.fetchExtension().setValue(extension);
				result.fetchPath().setValue(path);
				result.fetchOriginalName().setValue(originalName);
				KeyDataHandlerStringToClass handler = new KeyDataHandlerStringToClass();
				KeyDataTransformed transformed = handler.handleSetterBefore(types, null);

				Collection<String> separates = new ArrayList<>();
				List<String> msgs =SoteriumConfiguration.getInstance().getFileImportMessagesClasses();

				for(String msg: msgs){
					separates.add(msg);
				}

				Collection<MessageObject> messageObjects = new ArrayList<>();

				if(null!=transformed.getValue())
				{
					//noinspection unchecked
					Iterable<Class> values = (Iterable<Class>) transformed.getValue();
					int size = 0;
					for(Class cls: values)
					{
						if (ClassX.isKindOf(cls, BaseImporter.class))
						{
							size++;
						}
					}

					for(Class cls: values){
						if(ClassX.isKindOf(cls, BaseImporter.class))
						{
							if((size>1) && separates.contains(cls.getName())){
								MessageObject mo = FileImportMessage.separate(vertexId, cls.getName(), originalName, extension, file);
								if(null!=mo){
									messageObjects.add(mo);
								}
							}
							else
							{
								//noinspection unchecked
								result.fetchImporterTypes().add(cls);
							}
						}
					}
				}
				Environment.getInstance().getReportHandler().info("File Importer %s", fileName);
				result.save();
				if (result.hasId())
				{
					ImporterPreprocessor.getInstance().getMessages().add(result);
				}

				for(MessageObject mo: messageObjects){
					mo.create();
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	public KeyData<String> fetchVertexId()
	{
		if (null==this.vertexId)
		{
			this.vertexId=new KeyData<>(this, "vertexId", String.class, false, null);
		}
		return this.vertexId;
	}

	public KeyData<String> fetchExtension()
	{
		if (null==this.extension)
		{
			this.extension=new KeyData<>(this, "extension", String.class, false, null, new KeyDataHandlerFileExtension());
		}
		return this.extension;
	}

	public KeyData<String> fetchOriginalName()
	{
		if (null==this.originalName)
		{
			this.originalName=new KeyData<>(this, "originalName", String.class, false, null);
		}
		return this.originalName;
	}

	private static MessageObject separate(String vertexId, String types, String originalName, String extension, File file)
	{
		MessageObject result = null;
		try
		{
			String fileName=String.format("%s.%s", UUID.randomUUID().toString(), extension);
			File copy=new File(file.getParentFile().getAbsolutePath(), fileName);
			FileUtils.copyFile(file, copy);
			result = new MessageObject(vertexId, types, fileName, originalName, extension, copy.getAbsolutePath(), copy);
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	public KeyDataCollection<Class<? extends BaseImporter>> fetchImporterTypes()
	{
		if (null==this.importerTypes)
		{
			this.importerTypes=new KeyDataCollection<>(this, "importerTypes", Class.class, false, false, false, null, new KeyDataHandlerStringToClass());
		}
		return this.importerTypes;
	}

	public static synchronized FileImportMessage create(String types, String fileName, String originalName, String extension, String path, File file)
	{
		FileImportMessage result=null;
		if (file.exists())
		{
			result=new FileImportMessage();
			result.fetchBytes().setValue(file.length());
			result.fetchTitle().setValue(fileName);
			result.fetchExtension().setValue(extension);
			result.fetchPath().setValue(path);
			result.fetchOriginalName().setValue(originalName);
			KeyDataHandlerStringToClass handler = new KeyDataHandlerStringToClass();
			KeyDataTransformed transformed = handler.handleSetterBefore(types, null);
			if(null!=transformed.getValue())
			{
				//noinspection unchecked
				result.fetchImporterTypes().add((Class<? extends BaseImporter>) transformed.getValue());
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	public boolean exists()
	{
		return (null!=this.getFile()) && this.getFile().exists();
	}

	// Getters and setters
	public File getDirectory()
	{
		return this.getFile().isDirectory() ? this.getFile() : this.getFile().getParentFile();
	}
}
