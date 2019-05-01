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

import com.lusidity.components.Module;
import com.lusidity.data.interfaces.BaseServerConfiguration;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

public class ServerEntry
{
	public boolean disabled=false;
	private final Class<? extends Module> store;
	private final Class<? extends BaseServerConfiguration> configuration;
	private final JsonData configData;
	private IEnvironmentConfiguration.ServerRoles role=null;

// Constructors
	public ServerEntry(Class<? extends Module> store, Class<? extends BaseServerConfiguration> configuration, JsonData configData)
	{
		super();
		this.store=store;
		this.configuration=configuration;
		this.configData=configData;
		this.load();
	}

	private void load()
	{
		if (this.configData.hasKey("disabled"))
		{
			this.disabled=this.configData.getBoolean("disabled");
		}

		if (this.configData.hasKey("role"))
		{
			this.role=this.configData.getEnum(IEnvironmentConfiguration.ServerRoles.class, "role");
		}
		else
		{
			this.disabled=true;
		}
	}

	public boolean isInRole(IEnvironmentConfiguration.ServerRoles role)
	{
		return !this.isDisabled() && (null!=this.role) && (this.role==role);
	}

	public boolean isDisabled()
	{
		return this.disabled;
	}

// Getters and setters
	public IEnvironmentConfiguration.ServerRoles getRole()
	{
		return this.role;
	}

	public Class<? extends Module> getStore()
	{
		return this.store;
	}

	public Class<? extends BaseServerConfiguration> getConfiguration()
	{
		return this.configuration;
	}

	public JsonData getConfigData()
	{
		return this.configData;
	}

	@Override
	public String toString()
	{
		String result;
		try
		{
			result=String.format("Store: %s Configuration: %s Role: %s", this.getStore().getName(), this.getConfiguration().getName(), this.getRole());
			String path=(null!=this.getConfigData()) ? this.getConfigData().getString("configFilePath") : null;
			if (!StringX.isBlank(path))
			{
				result=String.format("%s Path: %s", result, path);
			}
		}
		catch (Exception ex){
			result = String.format("%s: configuration load error.", this.getClass().getName());
		}
		return result;
	}
}
