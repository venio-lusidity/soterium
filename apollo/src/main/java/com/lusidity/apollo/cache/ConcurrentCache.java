/*
 * Copyright (c) 2016, Venio, Inc, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Venio Incorporated
 * and its suppliers, if any.  The intellectual and technical concepts contained herein are proprietary
 * to Venio Incorporated and its suppliers and may be covered by U.S. and Foreign Patents, patents
 * in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from Venio Incorporated.
 *
 * references:
 * http://www.ehcache.org/
 */

package com.lusidity.apollo.cache;

import com.lusidity.Environment;
import com.lusidity.cache.BaseCache;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Max thread use 16 at a time.
 */
public class ConcurrentCache extends BaseCache
{
	@SuppressWarnings("CollectionDeclaredAsConcreteClass")
	private volatile ConcurrentHashMap<String, DataVertex> cache = null;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private Collection<String> misses=null;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private boolean sample = false;

	// Constructors
	public ConcurrentCache()
	{
		super();
	}

	// Overrides
	@Override
	public synchronized void init()
	{
		if (null!=this.getConfig())
		{
			this.setDisabled(!this.getConfig().isEnabled());

			int maxSize = this.getConfig().getMaxSize();
			this.cache = new ConcurrentHashMap<>(maxSize);
			this.sample = this.getConfig().sample();
			this.setOpened(true);
		}
	}

	@Override
	public void close()
		throws IOException
	{
		this.cache.clear();
		super.close();
	}

	@Override
	public void stop()
	{
		this.setOpened(false);
	}

	@Override
	public long getTotal()
	{
		return this.cache.size();
	}

	@Override
	protected long recount()
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
	public void checkAndLoad()
	{
		this.getConfig().checkAndReload();
	}

	@Override
	public <T extends DataVertex> T get(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key)
		throws ApplicationException
	{
		T result=null;
		if (!this.isDisabled() && this.isOpened() && (null!=store) && this.isCacheable(store))
		{
			String finalKey="";
			try
			{
				this.incrementAttempts(1);
				finalKey=BaseCache.getCompositeKey(store, partitionType, key);
				Object value=this.cache.get(finalKey);
				if (value!=null)
				{
					//noinspection unchecked
					result=(T) value;
					this.incrementHits(1);
				}
				else
				{
					this.incrementMissed(1);
					if (this.getConfig().isLogEnabled())
					{
						if (this.addMiss(finalKey))
						{
							Environment.getInstance().getReportHandler().info("Not in cache %s.", finalKey);
						}
					}
				}
			}
			catch (Exception ignored)
			{
			}
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
		if (null==this.misses)
		{
			this.misses=new ArrayList<>();
		}
		boolean result=!this.misses.contains(key);
		if (result)
		{
			this.misses.add(key);
		}
		return result;
	}

	@Override
	public void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, String entry)
		throws ApplicationException
	{
		Environment.getInstance().getReportHandler().notImplemented();
	}

	@Override
	public void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, DataVertex entry)
		throws ApplicationException
	{
		if (!this.isDisabled() && this.isOpened() && (null!=store) && this.isCacheable(store))
		{
			this.putSync(store, partitionType, key, entry);
			this.setTotal(this.cache.size());
		}
	}

	@Override
	public void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException
	{
		this.put(store, partitionType, entry.fetchId().getValue(), entry);
	}

	private void putSync(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, DataVertex entry)
	{
		try
		{
			this.remove(store, partitionType, entry);
			String finalKey=BaseCache.getCompositeKey(store, partitionType, key);
			if(this.sample){
				String fileName = String.format("enhanced_cache_sample_%s.csv", this.getClass().getSimpleName());
				FileX.appendLine(new File(Environment.getInstance().getConfig().getTempDir(), fileName),
					String.format("%s,%s", store.getSimpleName(), finalKey));
			}
			this.cache.put(finalKey, entry);
			if (ClassX.isKindOf(entry, BaseDomain.class))
			{
				BaseDomain vertex=(BaseDomain) entry;
				for (UriValue value : vertex.fetchIdentifiers())
				{
					String lid=value.fetchValue().getValue().toString();
					if ((StringX.startsWithIgnoreCase(lid, "lid:") || StringX.startsWithIgnoreCase(lid, "cpe:"))
					    && !StringX.containsIgnoreCase(lid, "_importer/"))
					{
						lid=BaseCache.getCompositeKey(store, partitionType, lid);
						this.cache.put(lid, entry);
					}
				}
				for (UriValue value : vertex.fetchVolatileIdentifiers())
				{
					String lid=value.fetchValue().getValue().toString();
					if (StringX.startsWithIgnoreCase(lid, "lid:")
					    && !StringX.containsIgnoreCase(lid, "_importer/"))
					{
						lid=BaseCache.getCompositeKey(store, partitionType, lid);
						this.cache.put(lid, entry);
					}
				}
			}
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException
	{
		try
		{
			if (!this.isDisabled() && this.isCacheable(store) && (null!=entry))
			{
				String finalKey=BaseCache.getCompositeKey(store, partitionType, entry.fetchId().getValue());
				if (this.cache.containsKey(finalKey))
				{
					this.cache.remove(finalKey);
				}
				if (ClassX.isKindOf(entry, BaseDomain.class))
				{
					BaseDomain vertex=(BaseDomain) entry;
					for (UriValue value : vertex.fetchIdentifiers())
					{
						String key=value.fetchValue().getValue().toString();
						if (StringX.startsWithIgnoreCase(key, "lid:") || StringX.startsWithIgnoreCase(key, "cpe:"))
						{
							key=BaseCache.getCompositeKey(store, partitionType, key);
							this.cache.remove(key);
						}
					}
					for (UriValue value : vertex.fetchVolatileIdentifiers())
					{
						String key=value.fetchValue().getValue().toString();
						if (StringX.startsWithIgnoreCase(key, "lid:"))
						{
							key=BaseCache.getCompositeKey(store, partitionType, key);
							this.cache.remove(key);
						}
					}
				}
				this.setTotal(this.cache.size());
			}
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key)
		throws ApplicationException
	{
		try
		{
			if (!this.isDisabled() && this.isCacheable(store))
			{
				String finalKey=BaseCache.getCompositeKey(store, partitionType, key);
				if (this.cache.containsKey(finalKey))
				{
					this.cache.remove(finalKey);
					this.incrementTotal(-1);
				}
				this.setTotal(this.cache.size());
			}
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public void resetCache()
	{
		this.misses=null;
		this.resetCacheAttempts();
		this.restart();
	}
}
