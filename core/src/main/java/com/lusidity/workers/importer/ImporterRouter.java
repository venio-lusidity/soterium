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

package com.lusidity.workers.importer;


import com.lusidity.Environment;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

public class ImporterRouter
{

	private final File originalFile;
	private String originalType;
	private File file=null;
	private JsonData configuration=null;

// Constructors
	public ImporterRouter(File file, String type)
	{
		super();
		this.originalFile=file;
		this.originalType=type;
	}

	public boolean handle(InputStream inputStream)
	{
		boolean result=false;
		String key=StringX.getLast(this.originalType, ".");
		if (!StringX.isBlank(key))
		{
			File fConfig=new File(Environment.getInstance().getConfig().getResourcePath(),
				String.format("/routers/%s.json", key.toLowerCase())
			);
			if (fConfig.exists())
			{
				JsonData config=new JsonData(fConfig);
				if (config.isJSONArray())
				{
					JsonData data=new JsonData(this.originalFile);
					if (data.isValid())
					{
						result=this.handle(config, data);
						if (result)
						{
							result=this.makeFile(inputStream);
						}
					}
				}
			}
		}
		return result;
	}

	private boolean handle(JsonData config, JsonData data)
	{
		boolean result=false;

		if (config.isJSONArray())
		{
			for (Object o : config)
			{
				if (result)
				{
					break;
				}
				if (o instanceof JSONObject)
				{
					JsonData c=new JsonData(o);

					String path=c.getString("path");
					if (!StringX.isBlank(path))
					{
						JsonData node=(StringX.equals(path, "/")) ? data : data.getFromPath(path);

						if (node.isJSONArray())
						{
							for (Object n : node)
							{
								if (n instanceof JSONObject)
								{
									JsonData d=new JsonData(n);
									result=this.validate(c, d);
									if (result)
									{
										this.configuration=c;
										break;
									}
								}
							}
						}
						else if (data.isJSONObject())
						{
							result=this.validate(c, node);
						}
					}
				}
			}
		}

		return result;
	}

	private boolean makeFile(InputStream inputStream)
	{
		boolean result=false;
		try
		{
			JsonData names=this.configuration.getFromPath("names");
			String fileName=UUID.randomUUID().toString();
			String ext=StringX.getLast(this.originalFile.getAbsolutePath(), ".");
			if (names.isJSONArray())
			{
				for (Object o : names)
				{
					if (o instanceof String)
					{
						fileName+=String.format("_%s", o.toString());
					}
				}
			}
			File file=
				new File(this.originalFile.getParentFile().getAbsolutePath(), String.format("%s.%s", fileName, ext));
			Files.copy(inputStream, file.toPath());
			result=file.exists();
			if (result)
			{
				this.file=file;
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return result;
	}

	private boolean validate(JsonData config, JsonData data)
	{
		boolean result=false;
		String property=config.getString("property");
		if (!StringX.isBlank(property))
		{
			Object expected=config.getObjectFromPath("value");
			Object actual=data.getObjectFromPath(property);
			if (expected instanceof String)
			{
				expected=((String) expected).toLowerCase();
			}
			if (actual instanceof String)
			{
				actual=((String) actual).toLowerCase();
			}
			result=Objects.equals(expected, actual);
		}

		return result;
	}

// Getters and setters
	public File getFile()
	{
		return this.file;
	}

	public String getType()
	{
		Class cls=this.getImporterClass();
		return (null!=cls) ? cls.getName() : null;
	}

	public Class<? extends BaseImporter> getImporterClass()
	{
		Class<? extends BaseImporter> result=null;
		if ((null!=this.configuration) && this.configuration.hasKey("cls"))
		{
			Class cls=this.configuration.getClassFromName("cls");
			if (ClassX.isKindOf(cls, BaseImporter.class))
			{
				//noinspection unchecked
				result=(Class<? extends BaseImporter>) cls;
			}
		}
		return result;
	}
}
