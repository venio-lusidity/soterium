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

package com.lusidity.collections;

import com.lusidity.Environment;
import com.lusidity.data.BaseVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.object.BaseRelationship;
import com.lusidity.framework.java.ClassX;
import org.apache.commons.collections.CollectionUtils;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Relationships<T extends BaseRelationship> implements List<T>
{
	private final IDataStore dataStore;
	private final String key;
	private final Class<? extends DataVertex> store;
	private final DataVertex vertex;
	private List<T> underlying=null;

	// Constructors
	public Relationships(DataVertex vertex, Class<? extends DataVertex> store, String propertyName, IDataStore dataStore){
		super();
		this.vertex = vertex;
		this.dataStore = dataStore;
		this.key = String.format("/rel%s", ClassHelper.getPropertyKey(store, propertyName));
		this.store = store;
		this.load();
	}

	public void load(){
		this.underlying = new ArrayList<T>();
		if(this.getVertex().fetchId().isNotNullOrEmpty())
		{
			BaseQueryBuilder qb=this.getQuery();
			qb.setApi(BaseQueryBuilder.API._count);
			QueryResults queryResults=qb.execute();
			if (!queryResults.isEmpty())
			{
				qb.setLimit(queryResults.size());
				for (IQueryResult queryResult : queryResults)
				{
					DataVertex result=queryResult.getVertex();
					//noinspection unchecked
					this.underlying.add((T) result);
				}
			}
		}
	}

	public DataVertex getVertex()
	{
		return this.vertex;
	}

	public BaseQueryBuilder getQuery(){
		BaseQueryBuilder result = this.dataStore.getIndexStore().getQueryBuilder(this.store, this.store, 0, 0);
		result.filter(BaseQueryBuilder.Operators.must, "relatedId", BaseQueryBuilder.StringTypes.raw, this.getVertex().getUri().toString());
		result.filter(BaseQueryBuilder.Operators.must, "relatedLabel", BaseQueryBuilder.StringTypes.raw, this.key);
		result.filter(BaseQueryBuilder.Operators.must, "deprecated", BaseQueryBuilder.StringTypes.na, false);
		return result;
	}

	public IDataStore getDataStore()
	{
		return this.dataStore;
	}

	public T get()
	{
		//noinspection unchecked
		return (this.isEmpty()) ? null : (T) this.underlying.get(0);
	}

	// Getters and setters
	public String getKey()
	{
		return this.key;
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
	public @NotNull <T1> T1[] toArray(T1[] a)
	{
		return this.underlying.toArray(a);
	}

	@Override
	public boolean add(@Flow(targetIsContainer=true) T t)
	{
		boolean result = false;
		try
		{
			if(this.vertex.hasId() && t.fetchRelatedId().isNullOrEmpty()){
				t.fetchRelatedId().setValue(this.getVertex().getUri().toString());
				t.fetchRelatedLabel().setValue(this.key);
			}
			if (this.vertex.hasId() && !t.hasId())
			{
				t.save(this.dataStore);
			}
			result=this.underlying.add(t);
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	@Override
	public boolean remove(Object o)
	{
		boolean result = false;
		if(ClassX.isKindOf(o, BaseRelationship.class)){
			result = this.underlying.remove(o);
			if(result)
			{
				result=((BaseVertex) o).delete(this.getDataStore());
			}

		}
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.underlying.containsAll(c);
	}

	@Override
	public boolean addAll(@Flow(sourceIsContainer=true, targetIsContainer=true) Collection<? extends T> c)
	{
		boolean result = !c.isEmpty();
		if(result){
			for(T t: c){
				boolean added = this.add(t);
				if(!added){
					result = false;
				}
			}
		}
		return result;
	}

	@Override
	public boolean addAll(int index, @Flow(sourceIsContainer=true, targetIsContainer=true) Collection<? extends T> c)
	{
		boolean result = !c.isEmpty();
		if(result){
			int on = index;
			for(T t: c){
				this.add(on, t);
				on++;
			}
		}
		return result;
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
		int len = this.size();
		for(int i=0;i<len;i++){
			T removed = this.remove(i);
			if(null!=removed){
				i--;
				len--;
			}
		}
	}


	@Override
	public T get(int index)
	{
		return this.underlying.get(index);
	}

	@Override
	public T set(int index, @Flow(targetIsContainer=true) T element)
	{
		return this.underlying.set(index, element);
	}

	@Override
	public void add(int index, @Flow(targetIsContainer=true) T element)
	{
		boolean result = false;
		try
		{
			if (!element.hasId())
			{
				element.save(this.dataStore);
			}
			if(element.hasId())
			{
				this.underlying.add(index, element);
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	@Override
	public T remove(int index)
	{
		@SuppressWarnings("unchecked")
		T result = (T) CollectionUtils.get(this.underlying, index);
		this.remove(result);
		return result;
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
