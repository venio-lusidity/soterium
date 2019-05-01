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

package com.lusidity.cache;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;

public class ExpiredCache extends BaseCache
{
	private static ExpiredCache instance=null;
	private CacheManager cacheManager = null;
	private Cache<String, DataVertex> cache = null;
	private Collection<String> misses = null;
	private LocalTime expiresAt = LocalTime.MIDNIGHT;
	private DateTime expiresDateTime = null;

	// Constructors
	public ExpiredCache(){
		super();
		ExpiredCache.instance = this;
	}

	// Overrides
	@Override
	public void init()
	{
		this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
		                                       .withCache("appCache",
			                                       CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, DataVertex.class,
				                                       ResourcePoolsBuilder.heap(this.getConfig().getMaxSize())).build()).build(true);
		this.cache = this.cacheManager.getCache("appCache", String.class, DataVertex.class);
		this.expiresAt = LocalTime.parse(this.getConfig().getProperty("expiresAt"));
		this.hasExpired(false);
		this.setOpened(true);
	}

	public boolean hasExpired(boolean overrideAndExpire)
	{
		boolean result = false;
		DateTime current = DateTime.now();
		if((null==this.expiresAt) || overrideAndExpire || this.expiresDateTime.isBefore(current)){
			this.resetCache();
			current.plusDays(1);
			this.expiresDateTime = new DateTime(
				current.year().get(),
				current.monthOfYear().get(),
				current.dayOfYear().get(),
				this.expiresAt.getHour(),
				this.expiresAt.getMinute(), 0);
			result = true;
		}
		return result;
	}

	@Override
	public void close()
		throws IOException
	{
		this.cacheManager.close();
		this.cacheManager=null;
		super.close();
	}

	@Override
	public
	void stop()
	{
		this.setOpened(false);
	}

	@Override
	protected
	long recount()
	{
		return this.getTotal();
	}

	@Override
	public void put(DataVertex entry)
		throws ApplicationException
	{
		if (!this.isDisabled())
		{
			Class<? extends DataVertex> partitionType=entry.getClass();
			if (ClassX.isKindOf(entry, Edge.class))
			{
				partitionType=((Edge) entry).fetchEndpointTo().getValue().getRelatedClass();
			}
			this.put(entry.getClass(), partitionType, entry.fetchId().getValue(), entry);
		}
	}

	@Override
	public
	void checkAndLoad()
	{
		this.getConfig().checkAndReload();
	}

	@Override
	public
	<T extends DataVertex> T get(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key)
		throws ApplicationException
	{
		T result = null;
		if(!this.hasExpired(false) && !this.isDisabled() && this.isOpened() && (null!=store) && this.isCacheable(store))
		{
			String finalKey = "";
			try
			{
				this.incrementAttempts(1);
				finalKey = BaseCache.getCompositeKey(store, partitionType, key);
				Object value=this.cache.get(finalKey);
				if (value!=null)
				{
					//noinspection unchecked
					result=(T) value;
					this.incrementHits(1);
				}
				else{
					this.incrementMissed(1);
					if(this.getConfig().isLogEnabled()){
						if(this.addMiss(finalKey))
						{
							Environment.getInstance().getReportHandler().info("Not in cache %s.", finalKey);
						}
					}
				}
			}
			catch (Exception ignored){}
		}
		return result;
	}

	@Override
	public <T extends DataVertex> T get(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, URI key)
		throws ApplicationException
	{
		T result=null;
		if ((null!=key) && StringX.startsWith(key.toString(), "lid:"))
		{
			result=this.get(store, partitionType, key.toString());
		}
		return result;
	}

	private synchronized boolean addMiss(String key)
	{
		if(null==this.misses){
			this.misses = new ArrayList<>();
		}
		boolean result = !this.misses.contains(key);
		if(result){
			this.misses.add(key);
		}
		return result;
	}

	@Override
	public
	void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, String entry)
		throws ApplicationException
	{
		Environment.getInstance().getReportHandler().notImplemented();
	}

	@Override
	public
	void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, DataVertex entry)
		throws ApplicationException
	{
		if(!this.isDisabled() && this.isOpened() && (null!=store) && this.isCacheable(store))
		{
			this.putSync(store, partitionType, key, entry);
		}
	}

	@Override
	public void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException
	{
		this.put(store, partitionType, entry.fetchId().getValue(), entry);
	}

	private synchronized void putSync(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, DataVertex entry)
	{
		try
		{
			String finalKey = BaseCache.getCompositeKey(store, partitionType, key);
			if(!this.cache.containsKey(finalKey)){
				this.incrementTotal(1);
			}
			this.cache.put(finalKey, entry);
			if(ClassX.isKindOf(entry, BaseDomain.class))
			{
				BaseDomain vertex = (BaseDomain)entry;
				for (UriValue value : vertex.fetchIdentifiers()){
					String lid = value.fetchValue().getValue().toString();
					if(StringX.startsWithIgnoreCase(lid, "lid:"))
					{
						lid = BaseCache.getCompositeKey(store, partitionType, lid);
						if(!this.cache.containsKey(lid)){
							this.incrementTotal(1);
						}
						this.cache.put(lid, entry);
					}
				}
				for (UriValue value : vertex.fetchVolatileIdentifiers()){
					String lid = value.fetchValue().getValue().toString();
					if(StringX.startsWithIgnoreCase(lid, "lid:"))
					{
						lid = BaseCache.getCompositeKey(store, partitionType, lid);
						if(!this.cache.containsKey(lid)){
							this.incrementTotal(1);
						}
						this.cache.put(lid, entry);
					}
				}
			}
		}
		catch (Exception ignored){}
	}

	@Override
	public synchronized
	void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException
	{
		this.remove(store, partitionType, entry.fetchId().getValue());
	}

	@Override
	public void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key)
		throws ApplicationException
	{
		try
		{
			if(!this.isDisabled())
			{
				String finalKey= BaseCache.getCompositeKey(store, partitionType, key);
				if (this.cache.containsKey(finalKey))
				{
					this.cache.remove(finalKey);
					this.incrementTotal(-1);
				}
			}
		}
		catch (Exception ignored){}
	}

	@Override
	public
	void resetCache()
	{
		this.misses = null;
		if(null!=this.cacheManager)
		{
			this.cacheManager.close();
			this.cacheManager = null;
		}
		this.resetCacheAttempts();
		this.restart();
	}

	// Methods
	public static ExpiredCache getInstance()
	{
		return ExpiredCache.instance;
	}
}
