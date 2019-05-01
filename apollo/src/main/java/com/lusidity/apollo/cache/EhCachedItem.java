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
import com.lusidity.data.DataVertex;
import com.lusidity.framework.java.ClassX;
import org.joda.time.DateTime;

public class EhCachedItem
{
	private long size=0;
	private DateTime created=DateTime.now();
	private DateTime lastAccessed=DateTime.now();
	private Object item=null;
	private Class<? extends DataVertex> store=null;
	private Class<? extends DataVertex> partitionType=null;
	private String key=null;
	private boolean duplicate=false;

	// Constructors
	public EhCachedItem(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, Object item, boolean isDuplicate)
	{
		super();
		this.item=item;
		this.store=store;
		this.partitionType=partitionType;
		this.key=key;
		this.duplicate=isDuplicate;
		try
		{
			if (!this.isDuplicate() && Environment.getInstance().getConfig().isCacheMemoryManagerEnabled())
			{
				if (ClassX.isKindOf(item, DataVertex.class))
				{
					// JsonData jsonData = ((DataVertex)item).toJson(false);
					//  this.size=jsonData.getSizeInBytes();
				}
			}
		}
		catch (Exception ignored)
		{
		}
	}

	public boolean isDuplicate()
	{
		return this.duplicate;
	}

	// Overrides
	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if (obj instanceof EhCachedItem)
		{
			EhCachedItem that=(EhCachedItem) obj;
			result=this.getStore().equals(that.getStore())
			       && this.getPartitionType().equals(that.getPartitionType())
			       && this.getKey().equals(that.getKey());
		}
		return result;
	}

	public Class<? extends DataVertex> getStore()
	{
		return this.store;
	}

	public Class<? extends DataVertex> getPartitionType()
	{
		return this.partitionType;
	}

	public String getKey()
	{
		return this.key;
	}

	public void touch()
	{
		this.lastAccessed=DateTime.now();
	}

	// Getters and setters
	public Object getItem()
	{
		return this.item;
	}

	public DateTime getCreated()
	{
		return this.created;
	}

	public DateTime getLastAccessed()
	{
		return this.lastAccessed;
	}

	public long getSizeInBytes()
	{
		return this.size;
	}
}
