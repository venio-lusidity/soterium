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
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("NullableProblems")
public class StatEdges<T extends DataVertex> extends ElementAttributes<T>
{
	private boolean loaded=false;

// Constructors
	public StatEdges(PropertyAttributes propertyAttributes)
	{
		super(propertyAttributes);
		if (propertyAttributes.preLoad())
		{
			this.load();
		}
		else
		{
			this.setDirty(true);
		}
	}

// Overrides
	@Override
	public Iterator<T> iterator(int limit)
	{
		//noinspection unchecked
		return new ElementIterator(this.getVertex(), this.getQuery(0, 0), 0, limit, this.size());
	}

	@Override
	public int size()
	{
		BaseQueryBuilder qb=this.getQuery(0, 0);
		qb.setApi(BaseQueryBuilder.API._count);
		return qb.execute().getCount();
	}

	@Override
	public Iterator<T> iterator()
	{
		//noinspection unchecked
		return new ElementIterator(this.getVertex(), this.getQuery(0, 0), 0, this.getLimit(), this.size());
	}

	@SuppressWarnings({
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	@Override
	public boolean add(T value)
	{
		boolean result=this.add(value, new EdgeData(), new ProcessStatus());
		if (result)
		{
			this.setDirty(true);
		}
		return result;
	}

	@Override
	public void add(int index, T element)
	{
		throw new NotImplementedException("This will not be implemented.");
	}

	@Override
	public T remove(int index)
	{
		T result=null;
		if (index<this.size())
		{
			ElementIterator iterator=new ElementIterator(this.getVertex(), this.getQuery(index, 1), 0, 1, this.size());
			//noinspection unchecked
			result=(T) iterator.remove(index, this.getEdgeType(), this.getVertex(), this.getKey(), this.getDirection());
		}
		return result;
	}

	@Override
	public boolean add(T value, EdgeData edgeData, ProcessStatus processStatus)
	{
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
			Environment.getInstance().getReportHandler().severe("This collections main vertex has not been saved.");
		}
		else
		{
			try
			{
				if ((null!=value) && (!value.hasId() || !this.contains(value)))
				{
					if (!value.hasId())
					{
						value.save();
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
						if (this.getVertex().fetchId().isNotNullOrEmpty() && value.fetchId().isNotNullOrEmpty())
						{
							DataVertex fromVertex = (this.getDirection()==Common.Direction.OUT) ? this.getVertex() : value;
							DataVertex toVertex = (this.getDirection()==Common.Direction.OUT) ? value : this.getVertex();

							edgeData.setEdgeType(this.getEdgeType());
							edgeData.setKey(this.getKey());
							edgeData.setDirection(this.getDirection());
							edgeData.setCredentials(this.getVertex().getCredentials());

							Edge edge=this.getVertex().getEdgeHelper().addEdge(fromVertex, toVertex, edgeData, this.isImmediate());
							edgeData.setEdge(edge);
							result=this.finalize(value, edge, processStatus);
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
		}
		return result;
	}

	@Override
	public boolean add(T value, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args)
	{
		throw new NotImplementedException();
	}

	@Override
	public boolean update(T value, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args)
	{
		throw new NotImplementedException();
	}

	@Override
	public void clearAndDelete()
	{
		try
		{
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
					DataVertex vertex=(DataVertex) CollectionUtils.get(delete, i);
					if (null!=vertex)
					{
						boolean d=false;
						try
						{
							vertex.delete();
						}
						catch (Exception ex)
						{
							Environment.getInstance().getReportHandler().warning(ex);
						}
						if (d)
						{
							del=true;
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
		qb.filter(BaseQueryBuilder.Operators.must, endpointLabel, BaseQueryBuilder.StringTypes.raw, this.getVertex().fetchId().getValue());
		qb.filter(BaseQueryBuilder.Operators.must, "deprecated", BaseQueryBuilder.StringTypes.raw, false);
		qb.sort(this.getSortKey(), BaseQueryBuilder.Sort.asc);
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

	@SuppressWarnings("OverlyNestedMethod")
	@Override
	public void load()
	{
		if (this.loaded)
		{
			this.reload();
		}
		else
		{
			this.loaded=true;
		}
		this.setDirty(false);
	}

	@Override
	public void reload()
	{
		this.loaded=false;
		this.load();
	}

	@Override
	public Iterator<IQueryResult> getEntries(int limit)
	{
		if (limit==0)
		{
			limit=this.getLimit();
		}
		return new ElementEntries<>(this.getVertex(), this.getQuery(0, 0), 0, limit, this.size());
	}

	@Override
	public boolean isEmpty()
	{
		return (this.size()==0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getAt(int idx)
	{
		T result=null;
		if (idx<this.size())
		{
			ElementIterator iterator=new ElementIterator(this.getVertex(), this.getQuery(0, 0), 0, 1, this.size());
			result=(T) iterator.getAt(idx);
		}
		return result;
	}

	@Override
	public boolean contains(Object o)
	{
		boolean result=false;
		if (this.getVertex().hasId())
		{
			// iterating the collection could be intensive.
			if (ClassX.isKindOf(o.getClass(), this.getExpectedType()))
			{
				DataVertex vertex=(DataVertex) o;
				if (vertex.fetchId().isNotNullOrEmpty())
				{
					Edge edge=
						this.getVertex().getEdgeHelper()
						    .getEdge(this.getEdgeType(), vertex, this.getKey(), this.getDirection());
					result=(null!=edge);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("OverlyLongMethod")
	@Override
	public boolean remove(Object o)
	{
		boolean result=false;
		try
		{
			if ((null!=o) && ClassX.isKindOf(o.getClass(), DataVertex.class))
			{
				DataVertex other=(DataVertex) o;
				DataVertex from=(this.getDirection()==Common.Direction.OUT) ? this.getVertex() : other;
				DataVertex to=(this.getDirection()==Common.Direction.OUT) ? other : this.getVertex();

				Edge edge=from.getEdgeHelper().getEdge(this.getEdgeType(), to, this.getKey(), Common.Direction.OUT);

				if (null!=edge)
				{
					result=edge.delete();
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		this.setDirty(true);
		return result;
	}

	@Override
	public void clear()
	{
		try
		{
			// when removing from underlying the indexer will get confused and stop processing.
			// So put the items in another object.
			Collection<T> delete=new ArrayList<>();
			for (T t : this)
			{
				delete.add(t);
			}

			boolean del=false;
			for (T t : delete)
			{
				boolean d=this.remove(t);
				if (d)
				{
					del=true;
				}
			}
			if (del)
			{
				this.setDirty(true);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public String getSortKey()
	{
		String result=super.getSortKey();
		if (StringX.isBlank(result))
		{
			result="createdWhen";
		}
		else if (StringX.equalsIgnoreCase(result, "directional_ordinal"))
		{
			result=Endpoint.getDirectionalKey(this.getDirection());
		}
		return result;
	}

	@Override
	public Class<? extends Edge> getEdgeType()
	{
		return this.propertyAttributes.getEdgeType();
	}

	@Override
	public String toString()
	{
		return (
			(null!=this.getExpectedType()) && !StringX.isBlank(this.getKey())) ?
			String.format(
				"%s (type: %s | key: %s)",
				this.getClass().getSimpleName(), this.getExpectedType().getSimpleName(), this.getKey()
			) : this.getClass().getSimpleName();
	}

	public void validateType(Class<? extends Edge> edgeType)
		throws ApplicationException
	{
		if (!ClassX.isKindOf(this.getEdgeType(), edgeType))
		{
			throw new ApplicationException("The property \"edgeType\" in AtSchemaProperty must be of type %s.", edgeType.getName());
		}
	}

	public void clean()
	{
		Iterator<IQueryResult> items=this.getEntries(1000);
		if (null!=items)
		{
			while (items.hasNext())
			{
				try
				{
					IQueryResult queryResult=items.next();
					Edge edge=queryResult.getEdge();
					DataVertex from=((Endpoint)edge.fetchEndpointFrom().getValue()).getVertex();
					DataVertex to=((Endpoint)edge.fetchEndpointTo().getValue()).getVertex();
					if ((null==from) || (null==to))
					{
						edge.delete();
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	public Edge getEdge(T item)
	{
		return this.getVertex().getEdgeHelper().getEdge(this.getEdgeType(), item, this.getKey(), this.getDirection());
	}

// Getters and setters
	protected Class<? extends DataVertex> getStoreClass()
	{
		return (this.getDirection()==Common.Direction.OUT) ? this.getVertex().getClass() : this.getExpectedType();
	}
}