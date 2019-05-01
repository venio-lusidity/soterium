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

package com.lusidity.apollo.cache;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.math.MathX;
import com.lusidity.framework.text.StringX;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EhCachedItems implements Map<String, EhCachedItem>
{
	private long maxMemorySizeInBytes=0;
	private long sizeInBytes=0;
	private Map<String, EhCachedItem> underlying=null;

	// Constructors
	public EhCachedItems(long maxMemorySizeInBytes)
	{
		super();
		this.underlying=new HashMap<>(100000000);
		this.maxMemorySizeInBytes=maxMemorySizeInBytes;
	}

	// Methods
	public static String getCompositeKey(String key, Class<? extends DataVertex> store,
	                                     Class<? extends DataVertex> partitionType, String... keys)
		throws ApplicationException
	{
		StringBuffer sb=new StringBuffer();
		if (null!=store)
		{
			sb.append(ClassHelper.getIndexKey(store));
		}
		if (null!=partitionType)
		{
			sb.append(ClassHelper.getIndexKey(partitionType));
		}
		if (StringX.isBlank(key))
		{
			throw new ApplicationException("Key cannot be null.");
		}
		if (null!=keys)
		{
			for (String k : keys)
			{
				sb.append(k);
			}
		}
		sb.append(key);
		return sb.toString();
	}

	// Getters and setters
	public long getAverageSize()
	{
		return this.getSizeInBytes()/this.size();
	}

	public long getSizeInBytes()
	{
		return this.sizeInBytes;
	}

	@Override
	public int size()
	{
		return this.underlying.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.underlying.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.underlying.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.underlying.containsValue(value);
	}

	@Override
	public EhCachedItem get(Object key)
	{
		return this.underlying.get(key);
	}

	@Override
	public EhCachedItem put(String key, EhCachedItem value)
	{
		try
		{
			// The object must first be removed so that the size of the object can be properly accounted for.
			if (this.underlying.containsKey(key))
			{
				this.underlying.remove(key);
			}
			this.underlying.put(key, value);
			this.sizeInBytes+=value.getSizeInBytes();
			this.check();
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return value;
	}

	@Override
	public EhCachedItem remove(Object key)
	{
		EhCachedItem result=this.get(key);
		if (null!=result)
		{
			this.sizeInBytes-=result.getSizeInBytes();
			this.underlying.remove(key);
		}
		return result;
	}

	@Override
	public void putAll(Map<? extends String, ? extends EhCachedItem> m)
	{
		this.underlying.putAll(m);
	}

	@Override
	public void clear()
	{
		this.underlying.clear();
	}

	@Override
	public @NotNull
	Set<String> keySet()
	{
		return this.underlying.keySet();
	}

	@Override
	public @NotNull
	Collection<EhCachedItem> values()
	{
		return this.underlying.values();
	}

	@Override
	public @NotNull
	Set<Map.Entry<String, EhCachedItem>> entrySet()
	{
		return this.underlying.entrySet();
	}

	public void check()
		throws ApplicationException
	{
		if ((this.getSizeInBytes()+MathX.BYTES_IN_MB)>this.maxMemorySizeInBytes)
		{
			this.evict();
		}
	}

	private void evict()
		throws ApplicationException
	{
		List<EhCachedItem> items=this.getEntriesByAge();
		if (!items.isEmpty())
		{
			for (EhCachedItem item : items)
			{
				this.remove(item.getKey());
				if ((this.getSizeInBytes()+MathX.BYTES_IN_MB)<this.maxMemorySizeInBytes)
				{
					break;
				}
			}
		}
		Environment.getInstance().getReportHandler().info("Evicted %d items from cache.", items.size());
	}

	private List<EhCachedItem> getEntriesByAge()
	{
		List<EhCachedItem> results=new ArrayList<>();
		results.addAll(this.values());
		Collections.sort(results, new Comparator<EhCachedItem>()
		{
			// Overrides
			@Override
			public int compare(EhCachedItem o1, EhCachedItem o2)
			{
				return o1.getLastAccessed().compareTo(o2.getLastAccessed());
			}
		});
		return results;
	}
}
