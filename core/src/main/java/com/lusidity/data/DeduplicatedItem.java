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

package com.lusidity.data;

import java.util.Objects;

public class DeduplicatedItem
{
	private final Class<? extends DataVertex> type;
	private int found=0;
	private int deleted=0;
	private int hits=0;
	private int incrementProcessed=0;

// Constructors
	public DeduplicatedItem(Class<? extends DataVertex> type)
	{
		super();
		this.type=type;
	}

// Overrides
	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if (obj instanceof DeduplicatedItem)
		{
			DeduplicatedItem that=(DeduplicatedItem) obj;
			result=Objects.equals(this.getType(), that.getType());
		}
		return result;
	}

	public Class<? extends DataVertex> getType()
	{
		return this.type;
	}

	public
	synchronized void incrementProcessed()
	{
		this.incrementProcessed+=1;
	}

// Getters and setters
	public int getFound()
	{
		return this.found;
	}

	public void setFound(int found)
	{
		this.found=found;
	}

	public int getHits()
	{
		return this.hits;
	}

	public void setHits(int hits)
	{
		this.hits=hits;
	}

	public int getIncrementProcessed()
	{
		return this.incrementProcessed;
	}

	public int getDeleted()
	{
		return this.deleted;
	}

	public void setDeleted(int deleted)
	{
		this.deleted=deleted;
	}
}
