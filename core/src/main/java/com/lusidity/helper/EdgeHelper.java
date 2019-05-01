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

package com.lusidity.helper;

import com.lusidity.Environment;
import com.lusidity.cache.BaseCache;
import com.lusidity.collections.VertexIterator;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class EdgeHelper
{
	private final DataVertex vertex;

	// Constructors
	public EdgeHelper(DataVertex vertex)
	{
		super();
		this.vertex=vertex;
	}

	@SuppressWarnings("unused")
	public boolean isChild(Class<? extends Edge> store, String key)
	{
		long count=Environment.getInstance().getQueryFactory().getLinkCount(store, this.vertex, key, Common.Direction.IN);
		return count>0;
	}

	public boolean isLinked(Class<? extends Edge> store, String key)
	{
		long count=Environment.getInstance().getQueryFactory().getLinkCount(store, this.vertex, key);
		return count>0;
	}

	public void cachePutEdge(Edge edge)
	{
		try
		{
			Environment.getInstance().getCache().put(edge);
		}
		catch (Exception ignored)
		{
		}
	}

	public QueryResults getCacheableEdges(Class<? extends Edge> store, String key, Common.Direction direction, int start, int limit, Class<? extends DataVertex> partitionType)
	{
		return Environment.getInstance().getQueryFactory().getEdges(store, this.vertex, key, null, null, direction, start, limit);
	}

	public void cacheRemoveEdge(Class<? extends Edge> edgeType, DataVertex other, String key, Common.Direction direction)
	{
		if (null!=other)
		{
			DataVertex from=(direction==Common.Direction.OUT) ? this.vertex : other;
			DataVertex to=(direction==Common.Direction.OUT) ? other : this.vertex;
			String workingKey=BaseCache.getEdgeCompositeKey(key, from, to);
			try
			{
				Environment.getInstance().getCache().remove(edgeType, from.getClass(), workingKey);
			}
			catch (ApplicationException e)
			{
				Environment.getInstance().getReportHandler().severe(e);
			}
		}
	}

	public void deprecateEdges(boolean deprecate)
	{
		Set<Class<? extends Edge>> subTypesOf=Environment.getInstance().getReflections().getSubTypesOf(Edge.class);
		for (Class<? extends Edge> subType : subTypesOf)
		{
			this.deprecateAllEdges(subType, deprecate);
		}
	}

	private void deprecateAllEdges(Class<? extends Edge> store, boolean isDeprecated)
	{
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, null, 0, 0);
		qb.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_FROM_EP_ID, BaseQueryBuilder.StringTypes.raw, this.vertex.fetchId().getValue());
		qb.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_TO_EP_ID, BaseQueryBuilder.StringTypes.raw, this.vertex.fetchId().getValue());
		VertexIterator iterator = new VertexIterator(store, qb, 0, 0);
		iterator.iterate(new EdgeDeprecationHandler(isDeprecated), new ProcessStatus(), 1);
	}

	public <T extends Edge> T addEdge(DataVertex fromVertex, DataVertex toVertex, EdgeData edgeData, boolean immediate)
	{
		if (!toVertex.hasId())
		{
			try
			{
				toVertex.save();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}

		if (!fromVertex.hasId())
		{
			try
			{
				fromVertex.save();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}

		T result=null;
		if (fromVertex.hasId() && toVertex.hasId())
		{
			boolean isFrom=(edgeData.getDirection()==Common.Direction.OUT);

			DataVertex from=isFrom ? fromVertex : toVertex;
			DataVertex to=isFrom ? toVertex : fromVertex;

			UserCredentials userCredentials = (null!=from.getCredentials()) ? from.getCredentials() : to.getCredentials();
			from.setCredentials(userCredentials);

			if(StringX.isBlank(edgeData.getFromLabel()) && ClassX.isKindOf(from, ApolloVertex.class)){
				String title = ((ApolloVertex)from).fetchTitle().getValue();
				if(!StringX.isBlank(title)){
					edgeData.setFromLabel(title);
				}
			}

			if(StringX.isBlank(edgeData.getToLabel()) && ClassX.isKindOf(to, ApolloVertex.class)){
				String title = ((ApolloVertex)to).fetchTitle().getValue();
				if(!StringX.isBlank(title)){
					edgeData.setToLabel(title);
				}
			}

			Endpoint fromEndPoint=new Endpoint(from, edgeData.getFromLabel(), edgeData.getFromOrdinal());
			fromEndPoint.setCredentials(userCredentials);
			Endpoint toEndPoint=new Endpoint(to, edgeData.getToLabel(), edgeData.getToOrdinal());
			to.setCredentials(userCredentials);
			toEndPoint.setCredentials(userCredentials);

			//noinspection unchecked
			result=Edge.create(edgeData, fromEndPoint, toEndPoint);

			try
			{
				result.setCredentials(userCredentials);
				result.setImmediate(immediate);
				if(null!=edgeData.getBulkItems()){
					edgeData.getBulkItems().add(result);
				}
				else
				{
					result.save();
				}
				result.setCredentials(userCredentials);
				edgeData.setEdge(result);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		return result;
	}

	public boolean removeAllEdges(Class<? extends Edge> store, boolean immediate, int start)
	{
		boolean result = false;

		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, null, start, 0);
		qb.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_FROM_EP_ID, BaseQueryBuilder.StringTypes.raw, this.vertex.fetchId().getValue());
		qb.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_TO_EP_ID, BaseQueryBuilder.StringTypes.raw, this.vertex.fetchId().getValue());
		qb.setApi(BaseQueryBuilder.API._count);
		int count = qb.execute().getCount();
		if(count>0)
		{
			qb.setApi(BaseQueryBuilder.API._delete_by_query);
			qb.execute();
			result = true;
			/*
			qb.setApi(BaseQueryBuilder.API._count);
			Environment.getInstance().getIndexStore().makeAvailable(store, true);
			count = qb.execute(null).getCount();
			result = (count==0);
			if(!result){
				Environment.getInstance().getReportHandler().warning("Could not delete edges, %s, for %s.", store.getName(), this.vertex.getUri().toString());
			}
			*/
		}

		return result;
	}

	public boolean removeEdges(Class<? extends Edge> store, DataVertex other, String key, Common.Direction direction)
	{
		DataVertex from=(direction==Common.Direction.OUT) ? this.vertex : other;
		DataVertex to=(direction==Common.Direction.IN) ? this.vertex : other;
		Edge edge=from.getEdgeHelper().getEdge(store, to, key, Common.Direction.OUT);
		boolean result=false;
		if (null!=edge)
		{
			result=edge.delete();
		}
		return result;
	}

	/**
	 * The edge that was used to get the vertex specified.
	 *
	 * @return An edge or null.
	 */
	public <T extends Edge> T getEdge(Class<? extends Edge> store, DataVertex other, String key, Common.Direction direction)
	{
		Class<? extends DataVertex> partition = null;
		if(direction==Common.Direction.OUT){
			partition = this.vertex.getClass();
		}
		else if(direction==Common.Direction.IN){
			partition = other.getClass();
		}
		return this.getEdge(store, partition, other, key, direction, false);
	}

	/**
	 * The edge that was used to get the vertex specidifed.
	 *
	 * @return An edge or null.
	 */
	public <T extends Edge> T getEdge(Class<? extends Edge> store, Class<? extends DataVertex> partition, DataVertex other, String key, Common.Direction direction, boolean onlyCache)
	{
		T result=null;
		if (null!=other)
		{
			DataVertex from=(direction==Common.Direction.OUT) ? this.vertex : other;
			DataVertex to=(direction==Common.Direction.OUT) ? other : this.vertex;
			String workingKey=BaseCache.getEdgeCompositeKey(key, from, to);
			try
			{
				result=Environment.getInstance().getCache().get(store, from.getClass(), workingKey);
			}
			catch (ApplicationException e)
			{
				Environment.getInstance().getReportHandler().severe(e);
			}

			if (!onlyCache && (null==result))
			{
				if(null!=partition){
					result = this.getEdgeFromStore(store, partition, from, to, key);
				}
				else
				{
					result=from.getEdgeHelper().getEdgeFromStore(store, to, key, direction);
				}
				// TODO: should we cache it here?
			}

			if ((null!=result) && ClassX.isKindOf(result, Edge.class))
			{
				// Clear the endpoints so that they can be reloaded.
				result.nullifyEndpoints();
			}
		}
		return result;
	}

	private <T extends Edge> T getEdgeFromStore(Class<? extends Edge> store, Class<? extends DataVertex> partition, DataVertex from, DataVertex to, String key)
	{
		return Environment.getInstance().getQueryFactory().getEdge(store, partition, from.fetchId().getValue(), to.fetchId().getValue(), key);
	}

	private <T extends Edge> T getEdgeFromStore(Class<? extends Edge> store, DataVertex other, String key, Common.Direction direction)
	{
		T result;
		if (direction==Common.Direction.BOTH)
		{
			result=this.getEdgeFromStore(store, this.vertex, other, key);
			if (null==result)
			{
				result=this.getEdgeFromStore(store, other, this.vertex, key);
			}
		}
		else
		{
			DataVertex from=(direction==Common.Direction.OUT) ? this.vertex : other;
			DataVertex to=(direction==Common.Direction.IN) ? this.vertex : other;
			result=this.getEdgeFromStore(store, from, to, key);
		}

		return result;
	}

	private <T extends Edge> T getEdgeFromStore(Class<? extends Edge> store, DataVertex from, DataVertex to, String key)
	{
		return Environment.getInstance().getQueryFactory().getEdge(store, from, to, key);
	}

	public boolean hasRelationshipWith(Class<? extends Edge> store, DataVertex other, String key, Common.Direction direction)
	{
		Edge edge=this.getEdge(store, other, key, direction);
		return (null!=edge);
	}

	private Collection<Edge> getAllEdges(Class<? extends Edge> store, int start, int limit)
	{
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, null, start, 0);
		qb.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_FROM_EP_ID, BaseQueryBuilder.StringTypes.raw, this.vertex.fetchId().getValue());
		qb.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_TO_EP_ID, BaseQueryBuilder.StringTypes.raw, this.vertex.fetchId().getValue());

		int hits=Environment.getInstance().getIndexStore().getQueryFactory().count(qb);
		Collection<Edge> results=new ArrayList<>();
		if (hits>0)
		{
			for (int i=0; i<hits; i+=limit)
			{
				qb.setStart(i);
				qb.setLimit(limit);
				QueryResults queryResults=qb.execute();
				if (queryResults.isEmpty())
				{
					break;
				}
				for (IQueryResult queryResult : queryResults)
				{
					Edge edge=queryResult.getEdge();
					if ((null!=edge) && !results.contains(edge))
					{
						results.add(edge);
					}
				}
			}
		}

		return results;
	}

	// Getters and setters
	public boolean isLinked()
	{
		Set<Class<? extends Edge>> edges=Environment.getInstance().getReflections().getSubTypesOf(Edge.class);
		edges.add(Edge.class);
		boolean result=false;
		for (Class<? extends Edge> edge : edges)
		{
			long count=Environment.getInstance().getQueryFactory().getLinkCount(edge, this.vertex);
			result=count>0;
			if (result)
			{
				break;
			}
		}
		return result;
	}
}
