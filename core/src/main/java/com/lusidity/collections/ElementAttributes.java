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
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

@SuppressWarnings({
	"NullableProblems",
	"AbstractClassNamingConvention"
	,
	"StandardVariableNames"
})
public abstract class ElementAttributes<T extends DataVertex> implements List<T>
{
	// ------------------------------ FIELDS ------------------------------
	protected transient DataVertex vertex=null;
	protected List<T> underlying=null;
	protected PropertyAttributes propertyAttributes=null;
	private transient boolean dirty=false;

// --------------------------- CONSTRUCTORS ---------------------------

	// Constructors
	public ElementAttributes()
	{
		super();
	}

	public ElementAttributes(PropertyAttributes propertyAttributes)
	{
		super();
		this.setVertex(propertyAttributes.getVertex());
		this.propertyAttributes=propertyAttributes;
	}

	public abstract Iterator<T> iterator(int limit);

	public abstract boolean add(T value, EdgeData edgeData, ProcessStatus processStatus)
		throws ApplicationException;

	public abstract boolean add(T value, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args)
		throws ApplicationException;

	public abstract boolean update(T value, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args);

	public abstract void clearAndDelete();

	public abstract BaseQueryBuilder getQuery(int start, int limit);

	public abstract void load();

	public abstract void reload();

	public abstract Iterator<IQueryResult> getEntries(int limit);

	public T get()
	{
		return this.isEmpty() ? null : this.getAt(0);
	}

	@Override
	public boolean isEmpty()
	{
		return this.underlying.isEmpty();
	}

	public abstract T getAt(int idx);

	@Override
	public boolean contains(Object o)
	{
		return this.underlying.contains(o);
	}

	@Override
	public Object[] toArray()
	{
		return this.underlying.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a)
	{
		//noinspection SuspiciousToArrayCall
		return this.underlying.toArray(a);
	}

	@Override
	public boolean remove(Object o)
	{
		boolean result=this.vertex.getEdgeHelper().removeEdges(Edge.class, (DataVertex) o, this.getKey(), this.getDirection());
		if (result && (null!=this.underlying))
		{
			this.underlying.remove(o);
		}
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.underlying.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		boolean result=false;
		//noinspection ConstantConditions
		if (null!=c)
		{
			for (T t : c)
			{
				result=this.add(t);
				if (!result)
				{
					break;
				}
			}
		}
		return result;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		return this.underlying.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean result=this.isEmpty();

		if (!result)
		{
			result=true;
			Collection<Object> temp=new ArrayList<>();
			for (Object o : c)
			{
				temp.add(o);
			}
			for (Object o : temp)
			{
				DataVertex item=(DataVertex) o;
				boolean removed=this.remove(item);
				if (!removed)
				{
					result=false;
				}
			}
			if (result)
			{
				this.underlying=new ArrayList<>();
			}
		}

		return result;
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
	// --------------------- GETTER / SETTER METHODS ---------------------

	@Override
	public T get(int index)
	{
		return this.underlying.get(index);
	}

	// --------------------- LIST IMPLEMENTATION ---------------------

	@Override
	public T set(int index, T element)
	{
		return this.underlying.set(index, element);
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
	public ListIterator<T> listIterator()
	{
		return this.underlying.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index)
	{
		return this.underlying.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		return this.underlying.subList(fromIndex, toIndex);
	}

	public JsonData toJson(boolean storing)
	{
		return this.toJson(storing, Environment.COLLECTIONS_DEFAULT_LIMIT);
	}

	public JsonData toJson(boolean storing, int limit)
	{
		JsonData result=null;
		JsonData results=JsonData.createArray();

		for (T item : this)
		{
			try
			{
				results.put(item.toJson(storing));
				if ((limit>0) && (results.length()==limit))
				{
					break;
				}
			}
			catch (Exception ex)
			{
				//noinspection CallToPrintStackTrace
				ex.printStackTrace();
			}

		}

		if (!results.isEmpty())
		{
			try
			{
				result=JsonData.createObject();
				result.put("key", this.getKey());
				result.put("direction", this.getDirection().toString());
				result.put("count", results.length());
				result.put("expectedType", this.getExpectedType().getName());
				result.put("collectionType", this.getClass().getName());
				result.put("results", results);
			}
			catch (Exception ex)
			{
				//noinspection CallToPrintStackTrace
				ex.printStackTrace();
			}

		}

		return result;
	}

	public String getKey()
	{
		return this.propertyAttributes.getKey();
	}

	public boolean allowDeprecated()
	{
		return this.propertyAttributes.allowDeprecated();
	}

	public Common.Direction getDirection()
	{
		return this.propertyAttributes.getDirection();
	}

	public Class<? extends DataVertex> getExpectedType()
	{
		return this.propertyAttributes.getExpectedType();
	}

	@SuppressWarnings("UnusedParameters")
	public boolean finalize(T value, Edge edge, ProcessStatus processStatus)
	{
		boolean result=(null!=edge) && edge.hasId();
		if (result)
		{
			if(null!=this.getVertex().getCredentials())
			{
				edge.setCredentials(this.getVertex().getCredentials());
				value.setCredentials(this.getVertex().getCredentials());
			}
			this.setDirty(true);
		}
		return result;
	}

	public DataVertex getVertex()
	{
		return this.vertex;
	}

	public void setVertex(DataVertex vertex)
	{
		this.vertex=vertex;
	}

	/**
	 * Always treat de-serialization as a full-blown constructor, by
	 * validating the final state of the de-serialized object.
	 */
	@SuppressWarnings("unused")
	private void readObject(
		ObjectInputStream aInputStream
	)
		throws ClassNotFoundException, IOException
	{
		//always perform the default de-serialization first
		aInputStream.defaultReadObject();
	}

	/**
	 * This is the default implementation of writeObject.
	 * Customise if necessary.
	 */
	@SuppressWarnings("unused")
	private void writeObject(
		ObjectOutputStream aOutputStream
	)
		throws IOException
	{
		//perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();
	}

	// Getters and setters
	public PropertyAttributes getPropertyAttributes()
	{
		return this.propertyAttributes;
	}

	public String getDescription()
	{
		return this.propertyAttributes.getDescription();
	}

	public JsonData getFilters()
	{
		return this.propertyAttributes.getFilters();
	}

	public Class<? extends DataVertex> getPartitionType()
	{
		return this.propertyAttributes.getPartitionType();
	}

	public int getLimit()
	{
		return this.propertyAttributes.getLimit();
	}

	public String getName()
	{
		return this.propertyAttributes.getName();
	}

	public boolean isSingleInstance()
	{
		return this.getPropertyAttributes().isSingleInstance();
	}

	public String getSortKey()
	{
		return this.propertyAttributes.getSortKey();
	}

	public BaseQueryBuilder.Sort getSortDirection(){
		return this.propertyAttributes.getSortDirection();
	}

	public boolean isIndexed()
	{
		return this.propertyAttributes.isIndexed();
	}

	public boolean isDirty()
	{
		return this.dirty;
	}

	public void setDirty(Boolean dirty)
	{
		this.dirty=dirty;
	}

	public boolean isImmediate()
	{
		return this.propertyAttributes.isImmediate();
	}

	List<T> getUnderlying()
	{
		return this.underlying;
	}

	@SuppressWarnings("unchecked")
	void setUnderlying(Collection<T> collection)
	{
		if (null==collection)
		{
			this.underlying=null;
		}
		else if (collection instanceof List)
		{
			this.underlying=(List) collection;
		}
		else
		{
			this.underlying=new ArrayList(collection);
		}
	}

	public abstract Class<? extends Edge> getEdgeType();

	// --------------------- Custom Methods ---------------------


}
