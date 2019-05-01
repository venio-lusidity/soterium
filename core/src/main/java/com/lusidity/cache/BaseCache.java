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
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.math.MathX;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.IndexHelper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({
	"ParameterHidesMemberVariable",
	"FieldAccessedSynchronizedAndUnsynchronized"
})
public abstract class BaseCache implements ICache
{
	private boolean opened=true;
	private CacheConfig config=null;

	private long memorySize=0L;
	private long maxMemorySize=0L;

	private long attempts=0L;
	private Float averageSize=0f;
	private long hits=0L;
	private long missed=0L;
	private long total=0L;
	private boolean disabled=false;

	// Constructors
	public BaseCache()
	{
		super();
	}

	// Overrides
	@SuppressWarnings("Duplicates")
	@Override
	public boolean start(Object... params)
		throws Exception
	{
		for (Object param : params)
		{
			//noinspection InstanceofConcreteClass
			if (param instanceof CacheConfig)
			{
				try
				{
					this.config=(CacheConfig) param;
					this.load();
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
				break;
			}
		}
		return this.isOpened();
	}

	private void load()
	{
		if (this.config.isOpened())
		{
			this.maxMemorySize=this.config.parseMemorySize()*MathX.BYTES_IN_GB;
			this.attempts=0;
			this.averageSize=0f;
			this.hits=0;
			this.missed=0;
			this.total=0;
			this.memorySize=0;
			this.init();
		}
		else
		{
			this.opened=false;
		}
	}

	public abstract void init();

	@Override
	public boolean restart()
	{
		this.load();
		return this.isOpened();
	}

	@Override
	public boolean credentialsRequired()
	{
		return false;
	}

	@Override
	public boolean isOffline()
	{
		return false;
	}

	@Override
	public void setOffline(boolean offline)
	{

	}

	@Override
	public int getPort()
	{
		return 0;
	}

	@Override
	public boolean isOpened()
	{
		return this.opened;
	}

	public void setOpened(boolean opened)
	{
		this.opened=opened;
	}

	@Override
	public void close()
		throws IOException
	{
		this.attempts=0;
		this.averageSize=0f;
		this.hits=0;
		this.missed=0;
		this.total=0;
		this.memorySize=0;
		this.stop();
	}

	@Override
	public Map<Class<? extends DataVertex>, Long> getCacheableObjects()
	{
		return this.getConfig().getCacheableObjects();
	}

	public abstract void stop();

	@Override
	public long getTotal()
	{
		return this.total;
	}

	// Methods	public void setTotal(long total)
	{
		this.total=total;
	}

	public static String getEdgeCompositeKey(String key, DataVertex from, DataVertex to, String... keys)
	{
		String result=String.format("%s/%s/%s",
			IndexHelper.getValueForIndex(key),
			IndexHelper.getValueForIndex(from.fetchId().getValue()),
			IndexHelper.getValueForIndex(to.fetchId().getValue())
		);
		if (null!=keys)
		{
			for (String k : keys)
			{
				result=String.format("%s/%s", result, k);
			}
		}
		return result;
	}

	@SuppressWarnings("CollectionDeclaredAsConcreteClass")
	@Override
	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();

		LinkedHashMap<String, Object> _hits=BaseCache.create(this.getHits(), "Hits");
		LinkedHashMap<String, Object> misses=BaseCache.create(this.getMissed(), "Misses");
		LinkedHashMap<String, Object> hitRate=BaseCache.create(
			(BaseCache.rate(this.getHits(), this.getAttempts())*100), "Cache Hit Rate");
		LinkedHashMap<String, Object> missRate=BaseCache.create((BaseCache.rate(this.getMissed(), this.getAttempts())*100), "Cache Miss Rate");
		LinkedHashMap<String, Object> _attempts=BaseCache.create(this.getAttempts(), "Attempts");
		LinkedHashMap<String, Object> memAvgSize=BaseCache.create(this.getAverageSize(), "Average Size in Memory");
		LinkedHashMap<String, Object> totalSize=BaseCache.create(this.getMemorySize(), "Memory Size");
		LinkedHashMap<String, Object> totalCached=BaseCache.create(this.getTotal(), "Total Cached");

		result.put("hits", _hits);
		result.put("hit_rate", hitRate);
		result.put("misses", misses);
		result.put("miss_rate", missRate);
		result.put("attempts", _attempts);
		result.put("mem_avg_size", memAvgSize);
		result.put("mem_size", totalSize);
		result.put("total_cached", totalCached);
		result.put("enabled", this.isOpened());

		return result;
	}

	public static String getCompositeKey(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, String key)
	{
		String result;
		if (!Objects.equals(store, partition))
		{
			result=String.format("%s_%s_%s", ClassHelper.getIndexKey(store), ClassHelper.getIndexKey(partition), StringX.replace(key, " ", "_"));
		}
		else
		{
			result=String.format("%s_%s", ClassHelper.getIndexKey(store), StringX.replace(key, " ", "_"));
		}
		return result;
	}

	@Override
	public void resetCacheAttempts()
	{
		this.hits=0;
		this.missed=0;
		this.attempts=0;
	}

	public boolean isCacheable(Class<? extends DataVertex> cls)
	{
		return this.getConfig().isCacheable(cls);
	}

	@Override
	public void reload()
	{
		this.resetCacheAttempts();
	}

	public long getObjectCacheSize(Class<? extends DataVertex> cls)
	{
		return this.getConfig().getObjectCacheSize(cls);
	}

	@Override
	public boolean flushAll()
	{
		this.restart();
		return this.isOpened();
	}

	public synchronized void incrementAttempts(long attempts)
	{
		this.attempts+=attempts;
	}

	@Override
	public boolean isDisabled()
	{
		return this.disabled;
	}

	public synchronized void incrementAverageSize(Float averageSize)
	{
		this.averageSize+=averageSize;
	}

	@Override
	public void setDisabled(boolean disabled)
	{
		this.disabled=disabled;
	}

	public synchronized void incrementHits(long hits)
	{
		this.hits+=hits;
	}

	@Override
	public JsonData getStats()
	{
		return this.toJson();
	}

	public synchronized void incrementMissed(long missed)
	{
		this.missed+=missed;
	}

	private static LinkedHashMap<String, Object> create(Object value, String label)
	{
		LinkedHashMap<String, Object> result=new LinkedHashMap<>();
		result.put("value", value);
		result.put("label", label);
		return result;
	}

	public synchronized void incrementTotal(long total)
	{
		this.total+=total;
		if (this.total<0)
		{
			this.recount();
		}
	}

	public long getHits()
	{
		return this.hits;
	}

	protected abstract long recount();

	public long getMissed()
	{
		return this.missed;
	}

	public synchronized void incrementMemorySize(long memorySize)
	{
		this.memorySize=memorySize;
	}

	public static double rate(long value, long attempts)
	{
		@SuppressWarnings("UnnecessaryLocalVariable")
		Float result=(value>0) ? ((float) value/attempts) : 0.0f;
		DecimalFormat df=new DecimalFormat("#.##");
		return Double.valueOf(df.format(result));
	}

	public synchronized void incrementMaxMemorySize(long maxMemorySize)
	{
		this.maxMemorySize=maxMemorySize;
	}

	public long getAttempts()
	{
		return this.attempts;
	}

	// Getters and setters
	public Float getAverageSize()
	{
		return this.averageSize;
	}

	public long getMaxMemorySize()
	{
		return this.maxMemorySize;
	}

	public long getMemorySize()
	{
		return this.memorySize;
	}

	public CacheConfig getConfig()
	{
		return this.config;
	}


}
