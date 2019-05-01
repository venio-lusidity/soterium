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

package com.lusidity.email;

import com.lusidity.configuration.BaseSoteriumConfiguration;
import com.lusidity.framework.json.JsonData;
import org.json.JSONObject;
import org.restlet.data.Method;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class EmailConfiguration extends BaseSoteriumConfiguration
{
	private static EmailConfiguration instance=null;
	private JsonData templates = null;

// Constructors
	public EmailConfiguration(File file)
	{
		super(file);
		this.load();
		EmailConfiguration.instance=this;
	}

// Overrides
	@Override
	public boolean isLoggable(Method method)
	{
		return false;
	}

// Methods
	public static synchronized EmailConfiguration getInstance()
	{
		return EmailConfiguration.instance;
	}

	public Collection<EmailServer> getServers(String serverKey)
	{
		Collection<EmailServer> results=new ArrayList<>();
		JsonData server=this.getData().getFromPath(serverKey);
		if ((null!=server) && server.isJSONObject())
		{
			JsonData servers=server.getFromPath("servers");
			if ((null!=servers) && servers.isJSONArray())
			{
				for (Object o : servers)
				{
					if (o instanceof JSONObject)
					{
						EmailServer emailServer=
							new EmailServer(JsonData.create(o), this.getDefaultFrom(serverKey), this.getDefaultTo(serverKey), this.getSystemAdmins(serverKey), this.getDefaultSubject(serverKey));
						results.add(emailServer);
					}
				}
			}
		}
		return results;
	}

	public String getDefaultFrom(String serverKey)
	{
		String result=null;
		JsonData server=this.getSever(serverKey);
		if (null!=server)
		{
			result=server.getString("default_from");
		}
		return result;
	}

	public String getDefaultTo(String serverKey)
	{
		String result=null;
		JsonData server=this.getSever(serverKey);
		if (null!=server)
		{
			result=server.getString("default_to");
		}
		return result;
	}

	public String getSystemAdmins(String serverKey)
	{
		String result=null;
		JsonData server=this.getSever(serverKey);
		if (null!=server)
		{
			result=server.getString("system_admins");
		}
		return result;
	}

	public String getDefaultSubject(String serverKey)
	{
		String result=null;
		JsonData server=this.getSever(serverKey);
		if (null!=server)
		{
			result=server.getString("default_subject");
		}
		return result;
	}

	public JsonData getSever(String serverKey)
	{
		return this.getData().getFromPath(serverKey);
	}

	public EmailTemplate getTemplate(String key)
	{
		EmailTemplate result = null;
		JsonData jd =  this.getTemplates().getFromPath(key);
		if((null!=jd) && jd.isValid()){
			result = new EmailTemplate(jd);
		}
		return result;
	}

	public JsonData getTemplates()
	{
		return this.getData().getFromPath("templates", "value");
	}

	// Getters and setters
	public boolean isDisabled()
	{
		return this.getData().getBoolean("disabled", "value");
	}
}
