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

package com.lusidity.data.bulk;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.IOperation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BulkItems<T extends DataVertex> implements List<T>
{
	private final Class<? extends DataVertex> store;
	private final Class<? extends DataVertex> partition;
	private final IOperation.Type operationType;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private List<T> underlying = Collections.synchronizedList(new ArrayList<>());

	public BulkItems(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, IOperation.Type operationType){
		super();
		this.store = store;
		this.partition = partition;
		this.operationType = operationType;
	}

	public Class<? extends DataVertex> getStore()
	{
		return this.store;
	}

	public Class<? extends DataVertex> getPartition()
	{
		return this.partition;
	}

	public IOperation.Type getOperationType()
	{
		return this.operationType;
	}

	public boolean save() throws Exception
	{
		IDataStore dataStore = Environment.getInstance().getDataStore();
		return dataStore.execute(dataStore.getOperation(this.getStore(), this.getPartition(), this.getOperationType(), this));
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
	public boolean contains(Object o)
	{
		return this.underlying.contains(o);
	}

	@Override
	public @NotNull Iterator<T> iterator()
	{
		return this.underlying.iterator();
	}

	@Override
	public @NotNull Object[] toArray()
	{
		return this.underlying.toArray();
	}

	@Override
	public @NotNull <T1> T1[] toArray(@NotNull T1[] a)
	{
		//noinspection SuspiciousToArrayCall
		return this.underlying.toArray(a);
	}

	/**
	 *
	 * @param t
	 * @return
	 */
	@Override
	public boolean add(T t)
	{
		return this.underlying.add(t);
	}

	@Override
	public boolean remove(Object o)
	{
		return this.underlying.remove(o);
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> c)
	{
		return this.underlying.containsAll(c);
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> c)
	{
		return this.underlying.addAll(c);
	}

	@Override
	public boolean addAll(int index, @NotNull Collection<? extends T> c)
	{
		return this.underlying.addAll(index, c);
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c)
	{
		return this.underlying.removeAll(c);
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c)
	{
		return this.underlying.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.underlying.clear();
	}

	@Override
	public T get(int index)
	{
		return this.underlying.get(index);
	}

	@Override
	public T set(int index, T element)
	{
		return this.underlying.set(index, element);
	}

	@Override
	public void add(int index, T element)
	{
		this.underlying.add(index, element);
	}

	@Override
	public T remove(int index)
	{
		return this.underlying.remove(index);
	}

	@Override
	public int indexOf(Object o)
	{
		return this.underlying.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return this.underlying.lastIndexOf(o);
	}

	@Override
	public @NotNull ListIterator<T> listIterator()
	{
		return this.underlying.listIterator();
	}

	@Override
	public @NotNull ListIterator<T> listIterator(int index)
	{
		return this.underlying.listIterator(index);
	}

	@Override
	public @NotNull List<T> subList(int fromIndex, int toIndex)
	{
		return this.underlying.subList(fromIndex, toIndex);
	}
}
