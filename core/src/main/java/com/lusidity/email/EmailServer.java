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

import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.util.Properties;

public class EmailServer
{
	private final JsonData data;
	private final String defaultFrom;
	private final String defaultTo;
	private final String defaultSubject;
	private final String systemAdmins;

	// Constructors
	public EmailServer(JsonData data, String defaultFrom, String defaultTo, String systemAdmins, String defaultSubject)
	{
		super();
		this.data=data;
		this.defaultFrom=defaultFrom;
		this.defaultTo=defaultTo;
		this.defaultSubject=defaultSubject;
		this.systemAdmins=systemAdmins;
	}

	// Getters and setters
	public String getAmi()
	{
		return this.data.getString("ami");
	}

	public String getHost()
	{
		return this.data.getString("host");
	}

	public String getUserName()
	{
		return this.data.getString("username");
	}

	public String getPassword()
	{
		return this.data.getString("password");
	}

	public int getPort()
	{
		return this.data.getInteger("port");
	}

	public String getReferer()
	{
		return this.data.getString("referer");
	}

	public String getDefaultFrom()
	{
		return this.defaultFrom;
	}

	public String getDefaultTo()
	{
		return this.defaultTo;
	}

	public String getDefaultSubject()
	{
		return this.defaultSubject;
	}

	public Properties getProperties()
	{
		@SuppressWarnings("AccessOfSystemProperties")
		Properties result=System.getProperties();
		JsonData properties=this.data.getFromPath("properties");
		if ((null!=properties) && properties.isJSONObject())
		{
			for (String key : properties.keys())
			{
				String value=properties.getString(key);
				if (!StringX.isBlank(value))
				{
					result.put(key, value);
				}
			}
		}
		return result;
	}

	public String getSystemAdmins()
	{
		return this.systemAdmins;
	}

	public boolean isTLS()
	{
		return this.data.getBoolean(false, "tls");
	}
}
