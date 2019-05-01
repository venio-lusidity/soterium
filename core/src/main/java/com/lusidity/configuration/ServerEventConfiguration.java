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

import com.lusidity.data.DataVertex;
import com.lusidity.framework.json.JsonData;
import com.lusidity.jobs.server.events.IServerEventHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ServerEventConfiguration extends BaseSoteriumConfiguration
{
	private static ServerEventConfiguration instance = null;
	private Collection<Class<? extends DataVertex>> types = new ArrayList<>();

	// Constructors
	public ServerEventConfiguration(File file)
	{
		super(file);
		this.load();
		ServerEventConfiguration.instance=this;
	}

	// Methods
	public static ServerEventConfiguration getInstance()
	{
		return ServerEventConfiguration.instance;
	}

	public boolean isEnabled(Class<? extends IServerEventHandler> handler)
	{
		return (null==handler) || (this.isEnabled() && this.hasEvent(handler));
	}

	private boolean isEnabled()
	{
		return this.getData().getBoolean("enabled", "value");
	}

	private boolean hasEvent(Class<? extends IServerEventHandler> handler)
	{
		return this.getData().getBoolean("events", "handlers", handler.getName(), "enabled");
	}

	public int interval()
	{
		return this.getData().getInteger(1, "interval", "value");
	}

	public Set<String> getRecipients(Class<? extends IServerEventHandler> handler)
	{
		Set<String> results = new LinkedHashSet<>();
		JsonData items = this.getData().getFromPath("events", "handlers", handler.getName(), "recipients");
		if(items.isJSONArray()){
			for(Object o: items){
				if(o instanceof String){
					results.add((String) o);
				}
			}
		}
		return results;
	}
}
