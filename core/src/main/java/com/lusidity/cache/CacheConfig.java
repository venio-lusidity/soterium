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
import com.lusidity.data.interfaces.BaseServerConfiguration;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CacheConfig extends BaseServerConfiguration
{
	private static final long DEFAULT_CACHE_SIZE=100000l;
	//private JsonData cacheableObjects=JsonData.createArray();
	private Map<Class<? extends DataVertex>, Long> cacheableObjects=new HashMap<>();
	private JsonData cacheData=null;

// Constructors
	public CacheConfig(JsonData jsonData)
	{
		super(jsonData);
	}

// Overrides
	@Override
	public boolean transform(JsonData item)
		throws Exception
	{
		if (!StringX.isBlank(this.getConfigFilePath()))
		{
			File file=BaseServerConfiguration.getFile(this.getConfigFilePath());
			if (file.exists())
			{
				this.setLastModified(file.lastModified());
				this.cacheData=new JsonData(file);
				if (this.isEnabled())
				{
					this.setOpened(this.cacheData.isJSONObject() && this.cacheData.hasKey("cacheableObjects"));
					if (this.isOpened())
					{
						JsonData nodes=this.cacheData.getFromPath("cacheableObjects");
						for (Object o : nodes)
						{
							if (o instanceof JSONObject)
							{
								JsonData node=JsonData.create(o);
								String key=node.getString("cls");
								Class<? extends DataVertex> cls=BaseDomain.getDomainType(key);
								Long size=node.getLong("size");
								size=(null==size) ? CacheConfig.DEFAULT_CACHE_SIZE : size;
								this.cacheableObjects.put(cls, size);
							}
						}
					}
				}
			}
			else
			{
				Environment.getInstance().getReportHandler().severe("The configuration file for the cache is missing.");
			}
		}
		return this.isOpened();
	}

	private JsonData getCacheData()
	{
		return this.cacheData;
	}

	@Override
	public boolean isEnabled()
	{
		return !this.getCacheData().getBoolean("disabled");
	}

	@Override
	public void close()
		throws IOException
	{

	}

	/**
	 * Get host name of cache server.
	 *
	 * @return Host name of cache server.
	 */
	public String getHost()
	{
		return this.getCacheData().getString("host");
	}

	public long parseMemorySize()
	{
		Long result = StringX.getNumberFromString(this.getMemorySize());
		return (null==result) ? 0 : result;
	}

	/**
	 * Get size for cache server. This is from the "size" property and must be in a format
	 * suitable for the underlying cache provider (e.g., "8g" for an 8GB Redis cache).
	 *
	 * @return Cache size.
	 */
	public String getMemorySize()
	{
		return this.getCacheData().getString("memory", "size");
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	public boolean isCacheable(Class<? extends DataVertex> cls)
	{
		boolean result=false;
		if ((null!=cls) && ClassX.isKindOf(cls, DataVertex.class))
		{
			result=this.getCacheableObjects().containsKey(cls);
		}
		if (!result)
		{
			for (Map.Entry<Class<? extends DataVertex>, Long> entry : this.getCacheableObjects().entrySet())
			{
				if (ClassX.isKindOf(cls, entry.getKey()))
				{
					result=true;
					break;
				}
			}
		}
		return result;
	}

	public Map<Class<? extends DataVertex>, Long> getCacheableObjects()
	{
		return this.cacheableObjects;
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	public long getObjectCacheSize(Class<? extends DataVertex> cls)
	{
		return this.cacheableObjects.get(cls);
	}

	public String getProperty(String key)
	{
		return this.getConfigData().getString(key);
	}

// Getters and setters
	/**
	 * Get IP port of cache server.
	 *
	 * @return IP port number of cache server.
	 */
	public Integer getPort()
	{
		return this.getCacheData().getInteger("port");
	}

	/**
	 * Get password for cache server.
	 *
	 * @return Password for cache server.
	 */
	public String getPassword()
	{
		return this.getCacheData().getString("password");
	}

	public long getDefaultSize()
	{
		return (this.getCacheData().hasKey("defaultSize") ?
			this.getCacheData().getLong("defaultSize") : CacheConfig.DEFAULT_CACHE_SIZE);
	}

	/**
	 * Get cache expiration strategy. This is from the "strategy" property and depends on
	 * the underlying cache provider. For example, for a simple LRU expiration strategy
	 * for all keys in Redis, use "allkeys-lru".
	 *
	 * @return Cache expiration strategy.
	 */
	public String getStrategy()
	{
		return this.getCacheData().getString("strategy");
	}

	/**
	 * The maximum size the cache is allowed to grow to.
	 *
	 * @return The maximum size of the cache.
	 */
	public int getMaxSize()
	{
		return this.getCacheData().getInteger("size");
	}

	/**
	 * @return true or false;
	 */
	public boolean sample()
	{
		return this.getCacheData().getBoolean(false,"sample");
	}

	/**
	 * Combined with "getUnitOfTime" it determines when a cache item can be disposed of.
	 *
	 * @return A number that represents a measurement of time.
	 */
	public long getTtl()
	{
		return this.getCacheData().getLong("ttl");
	}

	/**
	 * Combined with "getTtl" it determines when a cache item can be disposed of.
	 *
	 * @return A minutes, second hour, etc...
	 */
	public TimeUnit getUnitOfTime()
	{
		return this.getCacheData().getEnum(TimeUnit.class, "unitOfTime");
	}

	public boolean isLogEnabled()
	{
		return this.getConfigData().getBoolean("logEnabled");
	}
}
