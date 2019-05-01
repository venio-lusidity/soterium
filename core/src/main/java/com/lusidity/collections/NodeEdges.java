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
import com.lusidity.data.interfaces.data.query.QueryResults;
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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings({
	"NullableProblems",
	"OverlyComplexClass"
})
public class NodeEdges<T extends DataVertex> extends ElementAttributes<T>
{
	private boolean loaded=false;

	// Constructors
	public NodeEdges(PropertyAttributes propertyAttributes)
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
		return new NodeIterator(this.getVertex(), this.getEdgeType(), this.getPartitionType(), this.getKey(), this.getDirection(), 0, limit, this.size());
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
		return new NodeIterator(this.getVertex(), this.getEdgeType(), this.getPartitionType(), this.getKey(), this.getDirection(), 0, this.getLimit(), this.size());
	}

	@SuppressWarnings({
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	@Override
	public boolean add(T value)
	{
		boolean result=false;
		try
		{
			Constructor constructor=this.getEdgeType().getConstructor();
			Edge edge=(Edge) constructor.newInstance();
			edge.setCredentials(this.vertex.getCredentials());
			Class<? extends EdgeData> edt=edge.getEdgeDataType();
			constructor=edt.getConstructor();
			EdgeData edgeData=(EdgeData) constructor.newInstance();

			result=this.add(value, edgeData, new ProcessStatus());
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
	public void add(int index, T element)
	{
		try
		{
			Constructor constructor=this.getEdgeType().getConstructor();
			Edge edge=(Edge) constructor.newInstance();
			edge.setCredentials(this.vertex.getCredentials());
			Class<? extends EdgeData> edt=edge.getEdgeDataType();
			constructor=edt.getConstructor();
			EdgeData edgeData=(EdgeData) constructor.newInstance();
			edgeData.setFromOrdinal((long) this.size());

			boolean result=this.add(element, edgeData, new ProcessStatus());
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
		throws ApplicationException
	{
		return this.add(value, edgeData, null, (null==processStatus) ? new ProcessStatus() : processStatus);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod"
	})
	@Override
	public boolean add(T value, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args)
		throws ApplicationException
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
			throw new ApplicationException("This collections main vertex has not been saved.");
		}
		try
		{
			boolean delayed = !this.getVertex().isImmediate() || !value.isImmediate() || !edgeData.isImmediate();
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
							Environment.getInstance().getIndexStore().makeAvailable(this.getEdgeType(), true);
						}
					}
					//  Write JSON representation of value to a new node
					if (this.getVertex().fetchId().isNotNullOrEmpty())
					{
						edgeData.setEdgeType(this.getEdgeType());
						edgeData.setKey(this.getKey());
						edgeData.setDirection(this.getDirection());
						edgeData.setCredentials(this.getVertex().getCredentials());

						Edge edge=this.getVertex().getEdgeHelper().addEdge(this.getVertex(), value, edgeData, !delayed);
						if (edge.hasId())
						{
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
		boolean result=false;
		try
		{
			if (((null!=data) || ((null!=args) && (args.length>0))))
			{
				// Ensure we have the right edge..
				DataVertex from=(this.getDirection()==Common.Direction.OUT) ? this.getVertex() : value;
				DataVertex to=(this.getDirection()==Common.Direction.IN) ? this.getVertex() : value;
				Edge edge=from.getEdgeHelper().getEdge(this.getEdgeType(),
					to,
					this.getKey(),
					Common.Direction.OUT
				);
				result=((null!=edge) && edge.hasId() && (null!=edge.getHandler()));
				if (result)
				{
					result=edge.getHandler().handle(this.getVertex(), value, edgeData, data, processStatus, args);
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
		int fLimit=limit;
		if (limit==0)
		{
			fLimit=this.getLimit();
		}
		return new ElementEntries<>(this.getVertex(), this.getQuery(0, 0), 0, fLimit, this.size());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getAt(int idx)
	{
		T result=null;
		int size = this.size();
		if (idx<size)
		{
			NodeIterator iterator = new NodeIterator(this.getVertex(), this.getEdgeType(), this.getPartitionType(), this.getKey(), this.getDirection(), idx, 1, 1);
			result=(T) iterator.getAt(0);
		}
		return result;
	}

	@Override
	public boolean isEmpty()
	{
		return (this.size()==0);
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
				DataVertex fVertex=(DataVertex) o;
				if (fVertex.fetchId().isNotNullOrEmpty())
				{
					Edge edge=this.getVertex().getEdgeHelper()
						    .getEdge(this.getEdgeType(), fVertex, this.getKey(), this.getDirection());
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
					edge.setCredentials(this.vertex.getCredentials());
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
	public String getSortKey()
	{
		String result=super.getSortKey();
		if (StringX.isBlank(result))
		{
			result="createdWhen";
		}
		else if (StringX.equalsIgnoreCase(result, "directional_ordinal"))
		{
			result= String.format("%s.ordinal", ((this.getDirection()==Common.Direction.OUT) ? "object_endpoint_endpointFrom" : "object_endpoint_endpointTo"));
		}
		return result;
	}

	@Override
	public void clear()
	{
		try
		{
			String endpointLabel=Endpoint.getDirectionalKey(this.getDirection());
			Class<? extends DataVertex> partitionType=(this.getDirection()==Common.Direction.OUT) ? this.getPartitionType() : null;
			BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(this.getEdgeType(),
				partitionType, 0, 10000000
			);
			qb.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, this.getKey());

			if(null!=this.getVertex())
			{
				qb.filter(BaseQueryBuilder.Operators.must, endpointLabel, BaseQueryBuilder.StringTypes.raw, this.getVertex().fetchId().getValue());
			}
			qb.setApi(BaseQueryBuilder.API._delete_by_query);
			QueryResults qr = qb.execute();
			this.setDirty(true);
		}
		catch (Exception ignored){}
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
					DataVertex from=edge.fetchEndpointFrom().getValue().getVertex();
					DataVertex to=edge.fetchEndpointTo().getValue().getVertex();
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
	@SuppressWarnings("unused")
	protected Class<? extends DataVertex> getStoreClass()
	{
		return (this.getDirection()==Common.Direction.OUT) ? this.getVertex().getClass() : this.getExpectedType();
	}
}