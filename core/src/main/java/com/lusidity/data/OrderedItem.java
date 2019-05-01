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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OrderedItem
{
	private final Long ordinal;
	private final Object item;

// Constructors
	public OrderedItem(Long ordinal, Object item)
	{
		super();
		this.ordinal=ordinal;
		this.item=item;
	}

// Methods
	public static void sortAsc(List<OrderedItem> items)
	{
		items.sort(new Comparator<OrderedItem>()
		{
// Overrides
			@Override
			public int compare(OrderedItem o1, OrderedItem o2)
			{
				int result=0;
				if (Objects.equals(o1.getOrdinal(), o2.getOrdinal()))
				{
					result=0;
				}
				else if (null==o1.getOrdinal())
				{
					result=-1;
				}
				else if (null==o2.getOrdinal())
				{
					result=1;
				}
				else
				{
					result=o1.getOrdinal().compareTo(o2.getOrdinal());
				}
				return result;
			}
		});
	}

	public Long getOrdinal()
	{
		return this.ordinal;
	}

	public static void sortDesc(List<OrderedItem> items)
	{
		items.sort(new Comparator<OrderedItem>()
		{
			@Override
			public int compare(OrderedItem o1, OrderedItem o2)
			{
				int result=0;
				if (Objects.equals(o1.getOrdinal(), o2.getOrdinal()))
				{
					result=0;
				}
				else if (null==o1.getOrdinal())
				{
					result=-1;
				}
				else if (null==o2.getOrdinal())
				{
					result=1;
				}
				else
				{
					result=o1.getOrdinal().compareTo(o2.getOrdinal());
				}
				return result*-1;
			}
		});
	}

// Getters and setters
	public Object getItem()
	{
		return this.item;
	}
}
