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

package com.lusidity.configuration;

import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.server.AthenaServer;
import org.json.JSONObject;
import org.restlet.data.Method;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SoteriumConfiguration extends BaseSoteriumConfiguration
{
	private static SoteriumConfiguration instance=null;
	private static Collection<Method> methods=null;
	private Collection<AthenaServer> athenaServers = null;
	private boolean obfuscated = false;

	// Constructors
	public SoteriumConfiguration(File file)
	{
		super(file);
		this.load();
		SoteriumConfiguration.instance=this;
	}

	@Override
	protected void load()
	{
		super.load();
		try
		{
			this.obfuscated = this.getData().getBoolean("obfuscated", "value");
			ReportHandler.getInstance().debug("Loading configurations");
			JsonData items=this.getData().getFromPath("configurations", "value");
			if ((null!=items) && items.isJSONArray())
			{
				for (Object o : items)
				{
					if (o instanceof JSONObject)
					{
						JsonData item=JsonData.create(o);
						ReportHandler.getInstance().debug("Loading configuration class %s", item.getString("configCls"));
						Class cls=item.getClassFromName("configCls");
						if (ClassX.isKindOf(cls, BaseSoteriumConfiguration.class))
						{
							try
							{
								ReportHandler.getInstance().debug("Constructing configuration class %s", item.getString("configCls"));
								File file=new File(this.getResourcePath(), item.getString("path"));
								if (file.exists())
								{
									@SuppressWarnings("unchecked")
									Constructor constructor=cls.getConstructor(File.class);
									constructor.newInstance(file);
								}
								else
								{
									ReportHandler.getInstance().warning("The file, %s, does not exists", file.getAbsolutePath());
								}
							}
							catch (Exception ex)
							{
								ReportHandler.getInstance().severe("Failed to load %s. %s", cls.getSimpleName(), ex.getMessage());
							}
						}
					}
				}
			}
			JsonData loggingMethods=this.getData().getFromPath("loggingMethods", "value");
			if ((null!=loggingMethods) && loggingMethods.isJSONArray())
			{
				SoteriumConfiguration.methods=new ArrayList<>();
				for (Object o : loggingMethods)
				{
					if (o instanceof String)
					{
						try
						{
							Method method=Method.valueOf(o.toString().toUpperCase());
							SoteriumConfiguration.methods.add(method);
						}
						catch (Exception ex)
						{
							ReportHandler.getInstance().severe(ex);
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe("Failed to load %s. %s", this.getClass().getName(), ex.getMessage());
		}
	}

	@Override
	public boolean isLoggable(Method method)
	{
		return SoteriumConfiguration.methods.contains(method);
	}

	@Override
	public boolean isXmlDtdDisabled()
	{
		return this.getData().getBoolean("xml_dtd_disabled", "value");
	}

	public boolean isObfuscated()
	{
		return this.obfuscated;
	}

	// Overrides
// Methods
	public static SoteriumConfiguration getInstance()
	{
		return SoteriumConfiguration.instance;
	}

	// Getters and setters
	public Collection<AthenaServer> getAthenaServers()
	{
		this.athenaServers=new ArrayList<>();
		JsonData servers=this.data.getFromPath("athenaServers", "value");
		if (null!=servers)
		{
			for (Object o : servers)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=JsonData.create(o);
					String title=item.getString("title");
					String relativePath=item.getString("relativePath");
					URI host=item.getUri("host");
					AthenaServer athenaServer=new AthenaServer(title, host, relativePath, false);
					this.athenaServers.add(athenaServer);
				}
			}
		}
		return this.athenaServers;
	}

	public Integer getTimeCheckServers()
	{
		return this.getData().getInteger(1, "timeCheckServers", "value");
	}

	public List<String> getFileImportMessagesClasses()
	{
		List<String> results = new ArrayList<>();
		JsonData items = this.getData().getFromPath("fileImportMessagesClasses", "value");
		if((null!=items) && items.isJSONArray()){
			for(Object o: items){
				if(o instanceof String){
					results.add(o.toString());
				}
			}
		}
		return results;
	}
}
