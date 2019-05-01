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

package com.lusidity.discover;

import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.text.StringX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DiscoveryItems
{
	private final DiscoveryProvider provider;
	private final List<DiscoveryItem> items = new ArrayList<>();
	private volatile int hits=0;

	// Constructors
	public DiscoveryItems(DiscoveryProvider provider, int hits){
		super();
		this.provider = provider;
		this.hits = hits;
	}

	public synchronized boolean add(DiscoveryItem item){
		boolean result = false;
		if(!this.items.contains(item)){
			result = this.items.add(item);
		}
		return result;
	}

	public synchronized void merge(DiscoveryItems discoveryItems)
	{
		if(discoveryItems.getHits()>0)
		{
			this.addHits(discoveryItems.getHits());
			CollectionX.addAllIfUnique(this.getItems(), discoveryItems.getItems());
		}
	}

	public int getHits()
	{
		return this.hits;
	}

	public synchronized void addHits(Integer hits)
	{
		this.hits+=hits;
	}

	public List<DiscoveryItem> getItems()
	{
		return this.items;
	}

	public synchronized void setHits(int hits)
	{
		this.hits=hits;
	}

	public void sort()
	{
		try
		{
			if (!this.getItems().isEmpty())
			{
				Collections.sort(
					this.getItems(), new Comparator<DiscoveryItem>()
					{
						// Overrides
						@Override
						public int compare(DiscoveryItem o1, DiscoveryItem o2)
						{
							return StringX.compare(o1.getTitle(), o2.getTitle());
						}
					}
				);
			}
		}
		catch (Exception ignored){}
	}

	// Getters and setters
	public boolean isEmpty()
	{
		return this.getItems().isEmpty();
	}
}
