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
import com.lusidity.framework.java.ClassX;
import org.joda.time.DateTime;

import java.util.Objects;

public class CachedItem
{
    private DateTime expiresDateTime = null;
    private long size = 0;
    private DateTime created = DateTime.now();
    private DateTime lastAccessed= DateTime.now();
    private Object value = null;
    private String key = null;
    private boolean duplicate = false;

	// Constructors
	public CachedItem(String key, Object value, boolean isDuplicate){
		super();
		this.value = value;
		this.key = key;
		this.duplicate = isDuplicate;
		try
		{
			if(!this.isDuplicate() && Environment.getInstance().getConfig().isCacheMemoryManagerEnabled())
			{
				if(ClassX.isKindOf(value, DataVertex.class)){
					// JsonData jsonData = ((DataVertex)item).toJson(false);
					//  this.size=jsonData.getSizeInBytes();
				}
			}
		}
		catch (Exception ignored){}
	}

	public boolean isDuplicate()
	{
		return this.duplicate;
	}

    public CachedItem(String key, Object value, DateTime expiresDateTime)
    {
        super();
        this.value = value;
        this.key = key;
        this.expiresDateTime = expiresDateTime;
    }

	// Overrides
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof CachedItem){
			CachedItem that = (CachedItem) obj;
			result =Objects.equals(this.getKey(), that.getKey());
		}
		return result;
	}

	public String getKey()
	{
		return this.key;
	}

	public void touch()
	{
		this.lastAccessed=DateTime.now();
	}

	public boolean isExpired(DateTime expiresAt)
	{
		// if this.expiresDateTime is null don't expire the cached item
		return (null!=this.expiresDateTime) && this.expiresDateTime.isBefore(expiresAt);
	}

	// Getters and setters
	public Object getValue() {
		return this.value;
	}

    public DateTime getCreated() {
        return this.created;
    }

    public DateTime getLastAccessed(){
        return this.lastAccessed;
    }

    public long getSizeInBytes(){
        return this.size;
    }
}
