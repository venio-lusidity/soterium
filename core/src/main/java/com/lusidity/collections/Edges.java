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
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.system.primitives.Primitive;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Experimental class do not use.
 * @param <T>
 */
public class Edges<T extends DataVertex> extends ElementAttributes<T>
{
	private boolean loaded = false;

	// Constructors
	public Edges(PropertyAttributes propertyAttributes)
	{
		super(propertyAttributes);
		if (propertyAttributes.preLoad())
		{
			this.load();
		}
	}

	public synchronized boolean isLoaded()
	{
		return this.loaded;
	}

	private synchronized void setLoaded(boolean loaded)
	{
		this.loaded=loaded;
	}

	// Overrides
	@Override
	public Iterator<T> iterator(int limit)
	{
		if(!this.isLoaded()){
			this.load();
		}
		return this.getUnderlying().listIterator(limit);
	}

	@Override
	public boolean add(T value, EdgeData edgeData, ProcessStatus processStatus)
		throws ApplicationException
	{
		if(!this.isLoaded()){
			this.load();
		}
		return this.add(value, edgeData, null, (null==processStatus) ? new ProcessStatus() : processStatus);
	}

	@Override
	public boolean add(T value, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args)
		throws ApplicationException
	{
		if(!this.isLoaded()){
			this.load();
		}
		boolean result=false;
		if (!this.getVertex().hasId())
		{
			try
			{
				ElementEdges.saveVertex(this.getVertex());
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		if (!this.getVertex().hasId())
		{
			throw new ApplicationException("This collections main vertex has not been saved.");
		}
		try
		{
			boolean immediate = (!(!this.getVertex().isImmediate() || !value.isImmediate()));
			processStatus.fetchQueries().getValue().increment();

			if ((null!=value) && (!value.hasId() || !this.contains(value)))
			{
				if (!value.hasId())
				{
					value.save();
					processStatus.fetchCreated().getValue().increment();
				}

				// prevent the vertex from linking to itself
				if (value.hasId() && !StringX.equals(this.getVertex().fetchId().getValue(), value.fetchId().getValue()))
				{
					if (this.isSingleInstance())
					{
						if (ClassX.isKindOf(value.getClass(), Primitive.class))
						{
							this.clearAndDelete();
						}
						else
						{
							this.clear();
						}
					}
					//  Write JSON representation of value to a new node
					if (this.getVertex().fetchId().isNotNullOrEmpty() && (null!=value.fetchId().getValue()))
					{
						edgeData.setEdgeType(this.getEdgeType());
						edgeData.setKey(this.getKey());
						edgeData.setDirection(this.getDirection());
						edgeData.setCredentials(this.getVertex().getCredentials());

						Edge edge=this.getVertex().getEdgeHelper().addEdge(this.getVertex(), value, edgeData, immediate);
						if (edge.hasId())
						{
							this.getUnderlying().add(value);
							processStatus.fetchCreated().getValue().increment();
							result=this.finalize(value, edge, processStatus);
							value.setCredentials(this.getVertex().getCredentials());
							if (result && (null!=edge.getHandler()))
							{
								result=edge.getHandler().handle(this.getVertex(), value, edgeData, data, processStatus, args);
							}
						}
					}
					else
					{
						Environment.getInstance().getReportHandler().severe("The value node is null.");
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	@Override
	public boolean update(T value, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args)
	{
		return false;
	}

	@Override
	public void clearAndDelete()
	{
		try
		{
			if(!this.isLoaded()){
				this.load();
			}
			Collection<T> delete=new ArrayList<>();
			for (T t : this)
			{
				delete.add(t);
			}
			for (T t : delete)
			{
				this.remove(t);
			}
			boolean del=false;
			int len=delete.size();
			for (int i=0; i<len; i++)
			{
				try
				{
					DataVertex fVertex=(DataVertex) CollectionUtils.get(delete, i);
					if (null!=fVertex)
					{
						boolean deleted=false;
						try
						{
							deleted=fVertex.delete();
						}
						catch (Exception ex)
						{
							Environment.getInstance().getReportHandler().warning(ex);
						}
						if (deleted)
						{
							del=true;
							this.getUnderlying().remove(fVertex);
						}
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
			if (del)
			{
				this.setDirty(true);
			}
		}
		catch (Exception x)
		{
			Environment.getInstance().getReportHandler().severe(x);
		}
	}

	@Override
	public BaseQueryBuilder getQuery(int start, int limit)
	{
		String endpointLabel=Endpoint.getDirectionalKey(this.getDirection());
		Class<? extends DataVertex> partitionType=(this.getDirection()==Common.Direction.OUT) ? this.getPartitionType() : null;
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(this.getEdgeType(),
			partitionType, start, limit
		);
		qb.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, this.getKey());

		if(null!=this.getVertex())
		{
			qb.filter(BaseQueryBuilder.Operators.must, endpointLabel, BaseQueryBuilder.StringTypes.raw, this.getVertex().fetchId().getValue());
		}

		if(!this.allowDeprecated())
		{
			qb.filter(BaseQueryBuilder.Operators.must, "deprecated", BaseQueryBuilder.StringTypes.raw, false);
		}

		if(null!=this.getSortKey())
		{
			qb.sort(this.getSortKey(), this.getSortDirection());
		}
		if ((this.getFilters()!=null) && this.getFilters().isJSONObject())
		{
			for (String key : this.getFilters().keys())
			{
				qb.filter(
					BaseQueryBuilder.Operators.must,
					key,
					BaseQueryBuilder.StringTypes.raw,
					this.getFilters().getObjectFromPath(key)
				);
			}
		}
		return qb;
	}

	@Override
	public synchronized void load()
	{
		if(!this.isLoaded()){
			BaseQueryBuilder qb = this.getQuery(0, 1);
			VertexIterator vi = new VertexIterator(qb.getStore(), qb, 0, this.getLimit());
			String id = this.getVertex().fetchId().getValue();
			EdgeIteratorHandler handler = new EdgeIteratorHandler(id);
			vi.iterate(handler, new ProcessStatus(), 0);
			if(handler.getResults().isEmpty()){
				this.setUnderlying(new ArrayList<>());
			}
			else
			{
				//noinspection unchecked
				this.setUnderlying((Collection<T>) handler.getResults());
			}
			this.setLoaded(true);
		}
	}

	@Override
	public synchronized void reload()
	{
		this.setLoaded(false);
		this.setUnderlying(new ArrayList<>());
		this.load();
		this.setDirty(false);
	}

	@Override
	public Iterator<IQueryResult> getEntries(int limit)
	{
		int fLimit=limit;
		if (limit==0)
		{
			fLimit=this.getLimit();
		}
		return new ElementEntries<>(this.getVertex(), this.getQuery(0, 0), 0, fLimit, this.size());
	}

	@Override
	public T getAt(int idx)
	{
		if(!this.isLoaded()){
			this.load();
		}
		return this.getUnderlying().get(idx);
	}

	@Override
	public Class<? extends Edge> getEdgeType()
	{
		return this.propertyAttributes.getEdgeType();
	}

	@Override
	public int size()
	{
		return this.getUnderlying().size();
	}

	@Override
	public @NotNull Iterator<T> iterator()
	{
		if(!this.isLoaded()){
			this.load();
		}
		return this.getUnderlying().iterator();
	}

	@Override
	public boolean add(T t)
	{
		boolean result=false;
		try
		{
			if(!this.isLoaded()){
				this.load();
			}
			Constructor constructor=this.getEdgeType().getConstructor();
			Edge edge=(Edge) constructor.newInstance();
			edge.setCredentials(this.vertex.getCredentials());
			Class<? extends EdgeData> edt=edge.getEdgeDataType();
			constructor=edt.getConstructor();
			EdgeData edgeData=(EdgeData) constructor.newInstance();

			result=this.add(t, edgeData, new ProcessStatus());
			if (result)
			{
				this.setDirty(true);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	@Override
	public void add(int i, T t)
	{
		try
		{
			if(!this.isLoaded()){
				this.load();
			}
			Constructor constructor=this.getEdgeType().getConstructor();
			Edge edge=(Edge) constructor.newInstance();
			edge.setCredentials(this.vertex.getCredentials());
			Class<? extends EdgeData> edt=edge.getEdgeDataType();
			constructor=edt.getConstructor();
			EdgeData edgeData=(EdgeData) constructor.newInstance();
			edgeData.setFromOrdinal((long) this.size());

			boolean result=this.add(t, edgeData, new ProcessStatus());
			if (result)
			{
				this.setDirty(true);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	@Override
	public T remove(int i)
	{
		T result=null;
		if (i<this.size())
		{
			ElementIterator iterator=new ElementIterator(this.getVertex(), this.getQuery(i, 1), 0, 1, this.size());
			//noinspection unchecked
			result=(T) iterator.remove(i, this.getEdgeType(), this.getVertex(), this.getKey(), this.getDirection());
			this.getUnderlying().remove(i);
		}
		return result;
	}
}
