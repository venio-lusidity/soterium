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

package com.lusidity.domains.system.io;


import com.lusidity.Environment;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDataHandlerFileExtension;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;

import java.io.File;

@AtSchemaClass(name="FileInfo", discoverable=false, description="A system file.", writable=true)
public class FileInfo extends BaseDomain
{
	private KeyData<Long> bytes=null;
	private KeyData<String> extension=null;
	private KeyData<String> path=null;
	private KeyData<String> download=null;
	private transient File file=null;

	// Constructors
	public FileInfo(){ super();	}

	public FileInfo(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public FileInfo(File file)
	{
		super();
		this.fetchTitle().setValue(StringX.getFirst(file.getName(), "."));
		this.fetchExtension().setValue(StringX.getLast(file.getName(), "."));
		this.fetchPath().setValue(file.getAbsolutePath());
	}

	// Overrides
	@Override
	public void beforeUpdate(LogEntry.OperationTypes operationType)
	{
		super.beforeUpdate(operationType);
		if (operationType==LogEntry.OperationTypes.delete)
		{
			if (this.getFile().exists())
			{
				try
				{
					//noinspection ResultOfMethodCallIgnored
					this.getFile().delete();
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
		}
	}

	public File getFile()
	{
		if (null==this.file)
		{
			this.file=new File(this.fetchPath().toString());
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

	public boolean exists()
	{
		return (null!=this.getFile()) && this.getFile().exists();
	}

	public KeyData<String> fetchExtension()
	{
		if (null==this.extension)
		{
			this.extension=new KeyData<>(this, "extension", String.class, false, null, new KeyDataHandlerFileExtension());
		}
		return this.extension;
	}

	// Getters and setters
	public long getFileInMb()
	{
		return (this.getFileInKb()/1024);
	}

	public long getFileInKb()
	{
		return (((Long) this.fetchBytes().getValue())/1024);
	}

	public KeyData<Long> fetchBytes()
	{
		if (null==this.bytes)
		{
			this.bytes=new KeyData<>(this, "bytes", Long.class, false,
				(((null!=this.getFile()) && this.getFile().exists()) ? this.getFile().length() : 0)
			);
		}
		return this.bytes;
	}

	public String getWebUrl(String... relativePaths)
	{
		String result = FileX.getWebUrl(this.getFile(), Environment.getInstance().getConfig().getBlobBaseUrl(), relativePaths);
		if (this.fetchDownload().isNullOrEmpty())
		{
			this.fetchDownload().setValue(result);
		}
		return result;
	}

	public KeyData<String> fetchDownload()
	{
		if (null==this.download)
		{
			this.download=new KeyData<>(this, "download", String.class, false, null);
		}
		return this.download;
	}
}
