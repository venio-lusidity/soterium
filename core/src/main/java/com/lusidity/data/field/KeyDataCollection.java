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

package com.lusidity.data.field;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;

/**
 * A class used to make fields that use a JSON key value pair as the underlying data, managing multiple values.
 *
 * @param <T> An object type.
 */
@SuppressWarnings("StandardVariableNames")
public class KeyDataCollection<T> implements List<T>
{
	private final boolean isSingleInstance;
	private boolean cacheable = false;
	private List<T> underlying=new ArrayList<T>();
	private KeyData keyData;
	private boolean initialized = false;

	// Constructors
	public KeyDataCollection(DataVertex vertex, String keyName, Class fieldType, boolean cacheable, boolean isSingleInstance, boolean discoverable, Object defaultValue, IKeyDataHandler... fieldCallbacks)
	{
		super();
		this.cacheable = cacheable;
		this.keyData=new KeyData<>(false, vertex, keyName, fieldType, discoverable, defaultValue, fieldCallbacks);
		this.keyData.nullifyUnderlying();
		this.isSingleInstance=isSingleInstance;

		if (!this.getKeyData().getVertex().getVertexData().hasKey(this.getKeyData().getKeyName()))
		{
			this.getKeyData().getVertex().getVertexData().put(this.getKeyData().getKeyName(), JsonData.createArray());
			this.initialized = true;
		}
		else
		{
			this.load();
		}
	}

	public KeyData getKeyData()
	{
		return this.keyData;
	}

	private synchronized void load()
	{
		JsonData objects=this.getKeyData().getVertex().getVertexData().getFromPath(this.getKeyData().getKeyName());
		for (Object o : objects)
		{
			try
			{
				Object value = this.getKeyData().transform(o);
				if((value instanceof JSONObject) && ClassX.isKindOf(this.getKeyData().getFieldType(), DataVertex.class)){
					JsonData data = JsonData.create(value);
					@SuppressWarnings("unchecked")
					DataVertex vertex =ClassHelper.as(data, (Class<? extends DataVertex>)this.getKeyData().getFieldType());
					//noinspection unchecked
					this.underlying.add((T) vertex);
				}
				else if(null!=value){
					//noinspection unchecked
					this.underlying.add((T) value);
				}
			}
			catch (Exception ex)
			{
				ReportHandler.getInstance().severe(ex);
			}
		}
	}

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
		boolean result=false;
		if ((null!=t) && !this.underlying.contains(t))
		{
			JsonData items=this.getKeyData().getVertex().getVertexData().getFromPath(this.getKeyData().getKeyName());
			int len=this.getKeyData().getCallBacks().size();
			boolean authorized=true;
			T fValue=t;

			for (int i=0; i<len; i++)
			{
				IKeyDataHandler callback=(IKeyDataHandler) CollectionUtils.get(this.getKeyData().getCallBacks(), i);
				KeyDataTransformed transform=callback.handleSetterBefore(fValue, this.getKeyData());
				authorized=transform.isAuthorized();
				if(!authorized){
					break;
				}

				if(null!=transform.getValue())
				{
					//noinspection unchecked
					fValue=(T) transform.getValue();
				}
			}

			if (authorized)
			{
				if (ClassX.isKindOf(this.getKeyData().getFieldType(), DataVertex.class))
				{
					DataVertex vertex=(DataVertex) fValue;
					Object data=vertex.toJson(true);
					items.put(data);
				}
				else
				{
					items.put(fValue);
				}

				result=this.underlying.add(fValue);
				if(result)
				{
					this.getKeyData().getVertex().setDirty(true);
				}

				for (int i=0; i<len; i++)
				{
					IKeyDataHandler callback=(IKeyDataHandler) CollectionUtils.get(this.getKeyData().getCallBacks(), i);
					callback.handleSetterAfter(fValue, this.getKeyData());
				}
			}
		}
		return result;
	}

	@Override
	public boolean remove(Object o)
	{
		boolean result=this.underlying.remove(o);
		if (result)
		{
			if(this.isCacheable()){
				String key = null;
				if(o instanceof String){
					key = o.toString();
				}
				else if(o instanceof UriValue){
					UriValue uri = (UriValue)o;
					key = uri.fetchValue().getValue().toString();
				}
				if(!StringX.isBlank(key))
				{
					Class<? extends DataVertex> cls = this.getKeyData().getVertex().getClass();
					try
					{
						Environment.getInstance().getCache().remove(cls, cls, key);
					}
					catch (Exception ignored){}
				}
			}
			this.getKeyData().getVertex().getVertexData().remove(this.getKeyData().getKeyName());
			this.getKeyData().getVertex().getVertexData().put(this.getKeyData().getKeyName(), JsonData.createArray());
			JsonData items=this.getKeyData().getVertex().getVertexData().getFromPath(this.getKeyData().getKeyName());

			// remove from json
			for (T t : this.underlying)
			{
				if (ClassX.isKindOf(this.getKeyData().getFieldType(), DataVertex.class))
				{
					DataVertex dataVertex=(DataVertex) t;
					items.put(dataVertex.toJson(true));
				}
				else{
					items.put(t);
				}
			}
			this.getKeyData().getVertex().setDirty(true);
		}
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.underlying.contains(c);
	}

	@Override
	public boolean addAll(@Flow(sourceIsContainer=true, targetIsContainer=true) Collection<? extends T> c)
	{
		boolean result = false;
		//noinspection ConstantConditions
		if (null!=c)
		{
			try
			{
				for(T t: c){
					result = this.add(t);
					if(!result){
						break;
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	private boolean load(DataVertex vertex)
	{
		//noinspection unchecked
		boolean result=this.underlying.add((T) vertex);
		if (this.isSingleInstance && (this.underlying.size()>1))
		{
			this.remove(0);
		}
		//noinspection unchecked
		return result;
	}

	public boolean isCacheable()
	{
		return this.cacheable;
	}

	@Override
	public boolean addAll(int index, @Flow(sourceIsContainer=true, targetIsContainer=true) Collection<? extends T> c)
	{
		throw new NotImplementedException("KeyDataCollection does not implement addAll starting at index.");
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
	public synchronized void clear()
	{
		Collection<T> delete=new ArrayList<T>();

		for (T t : this)
		{
			delete.add(t);
		}

		for (T t : delete)
		{
			this.remove(t);
		}
	}

	@Override
	public T get(int index)
	{
		return this.underlying.get(index);
	}

	@Override
	public synchronized T set(int index, @Flow(targetIsContainer=true) T element)
	{
		return this.underlying.set(index, element);
	}

	@Override
	public synchronized void add(int index, @Flow(targetIsContainer=true) T element)
	{
		T check=this.get(index);
		if ((null==check) || !Objects.equals(check, element))
		{

			this.underlying.add(index, element);
		}
	}

	@Override
	public synchronized T remove(int index)
	{
		T result=null;
		int i=0;
		for (T t : this)
		{
			if (i==index)
			{
				result=t;
				this.remove(t);
				break;
			}
			i++;
		}
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

	@Override
	public int hashCode()
	{
		return 0;
	}

	@Override
	public boolean equals(Object o)
	{
		return this.underlying.equals(o);
	}

	public T get()
	{
		//noinspection unchecked
		return (this.underlying.isEmpty()) ? null : (T) CollectionUtils.get(this.underlying, 0);
	}
}
