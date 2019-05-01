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

package com.lusidity.cache.rest;


import com.lusidity.Environment;
import com.lusidity.cache.CachedItem;
import org.joda.time.DateTime;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResponseExpiredCache
{
	private static ResponseExpiredCache instance=null;
	private Map<String, CachedItem> underlying = new HashMap<>();
	private LocalTime expiresAt = LocalTime.MIDNIGHT;
	private DateTime expiresDateTime = null;
	private boolean enabled = false;

	// Constructors
	public ResponseExpiredCache(LocalTime localTime)
	{
		super();
		this.expiresAt = localTime;
		ResponseExpiredCache.instance = this;
	}

	// Methods
	public static ResponseExpiredCache getInstance()
	{
		return ResponseExpiredCache.instance;
	}

	public boolean hasExpired(boolean overrideAndExpire)
	{
		boolean result = false;
		DateTime current = DateTime.now();
		if((null==this.expiresDateTime) || overrideAndExpire || this.expiresDateTime.isBefore(current)){
			Collection<String> removals = new ArrayList<>();
			for(Map.Entry<String, CachedItem> entry: this.underlying.entrySet()){
				if(entry.getValue().isExpired(current)){
					removals.add(entry.getKey());
				}
			}

			for(String key: removals){
				this.remove(key);
			}

			current = current.plusDays(1);
			this.expiresDateTime = new DateTime(
				current.year().get(),
				current.monthOfYear().get(),
				current.dayOfMonth().get(),
				this.expiresAt.getHour(),
				this.expiresAt.getMinute(), 0);
			result = true;
			Environment.getInstance().getReportHandler().info("Response Expired Cache expired and evicted %d items.", removals.size());
		}
		return result;
	}


	public synchronized void put(Object value, String... keys){
		if(this.isEnabled())
		{
			String key=this.getKey(keys);
			CachedItem item=new CachedItem(key, value, this.expiresDateTime);
			this.underlying.put(key, item);
		}
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		if(!this.enabled){
			this.resetCache();
		}
	}

	private void resetCache()
	{
		this.underlying=new HashMap<>();
	}

	private String getKey(String... keys)
	{
		StringBuilder key = new StringBuilder();
		for(String k: keys){
			key.append(k);
		}
		return key.toString();
	}

	public synchronized void put(Object value, int hoursExpiresIn, String... keys)
	{
		if (this.isEnabled())
		{
			String key=this.getKey(keys);
			DateTime expiresAt=DateTime.now().plusHours(hoursExpiresIn);
			CachedItem item=new CachedItem(key, value, expiresAt);
			this.underlying.put(key, item);
		}
	}

	public Object get(String... keys){
		Object result = null;
		if(this.isEnabled() && !this.hasExpired(false))
		{
			CachedItem item=this.underlying.get(this.getKey(keys));
			if ((null!=item) && !item.isExpired(DateTime.now()))
			{
				result=item.getValue();
			}
			else{
				this.remove(keys);
			}
		}
		return result;
	}

	public void remove(String... keys){
		if(this.isEnabled())
		{
			this.underlying.remove(this.getKey(keys));
		}
	}
}
