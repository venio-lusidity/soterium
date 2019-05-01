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

package com.lusidity.cache.acs;

import com.lusidity.Environment;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.domains.acs.security.BasePrincipal;
import org.joda.time.DateTime;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class AcsExpiredCache
{
	private static AcsExpiredCache instance=null;
	private Map<String, Map<String, AcsCachedItem>> underlying = new HashMap<>();
	private LocalTime expiresAt = LocalTime.MIDNIGHT;
	private DateTime expiresDateTime = null;

	// Constructors
	public AcsExpiredCache(LocalTime localTime)
	{
		super();
		this.expiresAt = localTime;
		AcsExpiredCache.instance = this;
	}

	// Methods
	public static AcsExpiredCache getInstance()
	{
		return AcsExpiredCache.instance;
	}

	public synchronized void put(BasePrincipal principal, AcsCachedItem value){
		if(this.isEnabled())
		{
			String key = principal.fetchId().getValue();
			Map<String, AcsCachedItem> item = this.underlying.get(key);
			if(null==item){
				item = new HashMap<>();
				this.underlying.put(key, item);
			}
			item.remove(value.getKey());
			item.put(value.getKey(), value);
		}
	}

	public boolean isEnabled()
	{
		return ScopedConfiguration.getInstance().isEnabled();
	}

	public synchronized void clear(BasePrincipal principal)
	{
		if(null!=principal)
		{
			String expected=principal.fetchId().getValue();
			this.underlying.remove(expected);
		}
		else{
			Environment.getInstance().getReportHandler().info("The principal is null.");
		}
	}

	public AcsCachedItem get(BasePrincipal principal, String contextId){
		AcsCachedItem result = null;
		if(this.isEnabled())
		{
			Map<String, AcsCachedItem> pItem = this.underlying.get(principal.fetchId().getValue());
			if(null!=pItem)
			{
				AcsCachedItem item = pItem.get(contextId);
				if ((null!=item) && !item.isExpired(DateTime.now()))
				{
					result=item;
				}
			}
		}
		if((null!=result) && result.isExpired()){
			this.remove(principal, result.getKey());
			result = null;
		}
		return result;
	}

	public void remove(BasePrincipal principal, String... contextIds){
		if(this.isEnabled())
		{
			Map<String, AcsCachedItem> pItem = this.underlying.get(principal.fetchId().getValue());
			if(null!=pItem)
			{
				for(String key: contextIds)
				{
					pItem.remove(key);
				}
			}
		}
	}

	public void resetCache()
	{
		this.underlying = new HashMap<>();
	}

	private String getKey(String... keys)
	{
		StringBuilder key=new StringBuilder();
		for (String k : keys)
		{
			if (key.length()>0)
			{
				key.append(":");
			}
			key.append(k);
		}
		return key.toString();
	}
}
