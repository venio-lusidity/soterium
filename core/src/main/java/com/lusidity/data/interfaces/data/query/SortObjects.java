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

package com.lusidity.data.interfaces.data.query;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SortObjects implements Collection<SortObject>
{

	Collection<SortObject> underlying=new ArrayList<>();

// Overrides
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
	public boolean contains(Object o)
	{
		return this.underlying.contains(o);
	}

	@Override
	public Iterator<SortObject> iterator()
	{
		return this.underlying.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return this.underlying.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return this.underlying.toArray(a);
	}

	@Override
	public boolean add(SortObject sortObject)
	{
		return this.underlying.add(sortObject);
	}

	@Override
	public boolean remove(Object o)
	{
		return this.underlying.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.underlying.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends SortObject> c)
	{
		return this.underlying.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return this.underlying.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return this.underlying.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.underlying.clear();
	}

	@Override
	public int hashCode()
	{
		return this.underlying.hashCode();
	}

// Methods
	public static SortObjects create(String key, BaseQueryBuilder.Sort sort)
	{
		SortObjects results=new SortObjects();
		return results.add(key, sort);
	}

	public SortObjects add(String key, BaseQueryBuilder.Sort sort)
	{
		SortObject sortObject=new SortObject(key, sort);
		this.add(sortObject);
		return this;
	}
}
