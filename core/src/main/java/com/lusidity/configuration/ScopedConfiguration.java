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

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.io.ScopedDirectory;
import com.lusidity.system.security.UserCredentials;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ScopedConfiguration extends BaseSoteriumConfiguration
{
	private static ScopedConfiguration instance=null;
	private boolean enabled = false;
	private boolean vertexAuthEnabled = false;
	private Collection<Class<? extends DataVertex>> types = new ArrayList<>();

	// Constructors
	public ScopedConfiguration(File file)
	{
		super(file);
		this.load();
		ScopedConfiguration.instance=this;
		this.start();
	}

	protected void start()
	{
		this.types=new ArrayList<>();
		this.enabled = this.data.getBoolean(false, "enabled", "value");
		this.vertexAuthEnabled = this.data.getBoolean(false, "vertexAuthEnabled", "value");
		JsonData item=this.data.getFromPath("cachable", "value");
		if (null!=item)
		{
			Collection<String> keys=item.keys();
			for (String key : keys)
			{
				boolean add=item.getBoolean(key);
				if (add)
				{
					try
					{
						//noinspection unchecked
						this.types.add((Class<? extends DataVertex>) Class.forName(key));
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().warning("ScopedConfiguration: The class, %s, is not a valid vertex.", key);
					}
				}
			}
		}
	}

	// Methods
	public static ScopedConfiguration getInstance()
	{
		return ScopedConfiguration.instance;
	}

	public boolean isCacheable(DataVertex context)
	{
		boolean result = false;
		if(null!=this.types)
		{
			Class<? extends DataVertex> actual=context.getClass();
			for (Class<? extends DataVertex> type : this.types)
			{
				result=ClassX.isKindOf(actual, type);
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	public boolean isPowerUser(BasePrincipal principal)
	{
		boolean result = (null!=principal) && Objects.equals(principal, SystemCredentials.getInstance().getPrincipal());
		if(!result)
		{
			Collection<String> groups=this.getPowerGroups();
			if (null!=principal)
			{
				for (String group : groups)
				{
					result=Group.isInGroup(principal, group);
					if (result)
					{
						break;
					}
				}
			}
		}
		return result;
	}

	public Collection<String> getPowerGroups()
	{
		JsonData items = this.getData().getFromPath("powerGroups", "value");
		Collection<String> results = new ArrayList<>();
		if(null!=items){
			for(Object o: items){
				if(o instanceof String){
					results.add((String) o);
				}
			}
		}
		return results;
	}

	public boolean isAccountManager(UserCredentials credentials)
	{
		SystemCredentials sc = SystemCredentials.getInstance();
		boolean result = (null!=credentials) && Objects.equals(sc, credentials);
		if(!result && (null!=credentials))
		{
			Collection<String> groups = this.getAccountManagers();
			BasePrincipal principal = credentials.getPrincipal();
			if(null!=principal)
			{
				for (String group : groups)
				{
					result=Group.isInGroup(principal, group);
					if (result)
					{
						break;
					}
				}
			}
		}
		return result;
	}

	public Collection<String> getAccountManagers()
	{
		JsonData items=this.getData().getFromPath("accountManagers", "value");
		Collection<String> results=new ArrayList<>();
		if (null!=items)
		{
			for (Object o : items)
			{
				if (o instanceof String)
				{
					results.add((String) o);
				}
			}
		}
		return results;
	}

	public Collection<String> getPrivileged()
	{
		JsonData items=this.getData().getFromPath("privileged", "value");
		Collection<String> results=new ArrayList<>();
		if (null!=items)
		{
			for (Object o : items)
			{
				if (o instanceof String)
				{
					results.add((String) o);
				}
			}
		}
		return results;
	}

	// Getters and setters
	public boolean isVertexAuthEnabled()
	{
		return this.isEnabled() && this.vertexAuthEnabled;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public Collection<ScopedDirectory> getScopedDirectories()
	{
		Collection<ScopedDirectory> results = new ArrayList<>();
		JsonData items = this.data.getFromPath("directories", "value");
		if(items.isJSONArray()){
			for(Object o: items){
				JsonData item = JsonData.create(o);
				ScopedDirectory sd = new ScopedDirectory(item.getString("path"), item.getString("category"));
				results.add(sd);
			}
		}
		return results;
	}
}
