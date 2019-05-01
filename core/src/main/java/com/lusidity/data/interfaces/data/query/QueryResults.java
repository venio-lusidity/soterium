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


import com.lusidity.Environment;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.BaseExtendedData;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings({
	"NullableProblems",
	"OverlyComplexClass"
	,
	"StandardVariableNames"
})
public class QueryResults implements List<IQueryResult>
{
	@SuppressWarnings("EnumeratedClassNamingConvention")
	public enum Format
	{
		_default,
		discovery
	}
	List<IQueryResult> underlying=new ArrayList<>();
	private JsonData response= null;
	private boolean aggregated = false;
	private int next=0;
	private int limit=0;
	private Integer hits=null;
	private Integer took=null;
	private Double maxScore=null;
	@SuppressWarnings("CollectionDeclaredAsConcreteClass")
	private LinkedHashMap<String, DataVertex> vertices=new LinkedHashMap<>();
	private boolean scopingEnabled = false;
	private boolean returnEdge = false;
	private boolean includeEdge = false;
	private int uniqueCount = 0;
	private BaseQueryBuilder queryBuilder = null;

	// Constructors
	public QueryResults(BaseQueryBuilder queryBuilder)
	{
		super();
		this.queryBuilder = queryBuilder;
		this.scopingEnabled = (null!=ScopedConfiguration.getInstance()) && ScopedConfiguration.getInstance().isEnabled();
	}

	public QueryResults(BaseQueryBuilder queryBuilder, Double maxScore, Integer took, Integer hits, boolean aggregated)
	{
		super();
		this.maxScore=maxScore;
		this.took=took;
		this.hits=hits;
		this.aggregated = aggregated;
		this.scopingEnabled = (null!=ScopedConfiguration.getInstance()) && ScopedConfiguration.getInstance().isEnabled();
		this.queryBuilder = queryBuilder;
	}

	public QueryResults(BaseQueryBuilder queryBuilder, Double maxScore, Integer took, Integer hits, int start, int limit, boolean aggregated)
	{
		super();
		this.maxScore=maxScore;
		this.took=took;
		this.hits=hits;
		this.limit=limit;
		this.next=start+limit;
		this.aggregated = aggregated;
		this.scopingEnabled = (null!=ScopedConfiguration.getInstance()) && ScopedConfiguration.getInstance().isEnabled();
		this.queryBuilder = queryBuilder;
	}

	public QueryResults(BaseQueryBuilder queryBuilder, int start, int limit, boolean aggregated)
	{
		super();
		this.next=start;
		this.limit=limit;
		this.aggregated = aggregated;
		this.scopingEnabled = (null!=ScopedConfiguration.getInstance()) && ScopedConfiguration.getInstance().isEnabled();
		this.queryBuilder = queryBuilder;
	}

	public <T extends DataVertex> Collection<T> toCollection(String dataStoreId)
	{
		Collection<T> results=new ArrayList<>();
		for (IQueryResult queryResult : this)
		{
			T result=queryResult.getOtherEnd(dataStoreId);
			if (null!=result)
			{
				results.add(result);
			}
			else
			{
				this.hits-=1;
			}
		}
		return results;
	}

	public JsonData toJson(BaseQueryBuilder qb, String lid, QueryResults.Format format, IQueryResultHandler handler)
	{
		this.setReturnEdge(qb.returnEdge());
		this.setIncludeEdge(qb.includeEdge());
		return this.toJson(qb, lid, qb.getCredentials(), 0, 0, 0, format, handler);
	}

	public JsonData toJson(String lid, BaseQueryBuilder.API api, UserCredentials credentials, QueryResults.Format format, IQueryResultHandler handler)
	{
		return this.toJson(lid, api, credentials, format, 0, 0, 0, handler);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	private JsonData toJson(String lid, BaseQueryBuilder.API api, UserCredentials credentials, QueryResults.Format format, int actual, int excluded, int elapsed, IQueryResultHandler handler)
	{
		JsonData result=JsonData.createObject();
		JsonData results=JsonData.createArray();

		Integer lNext=this.getNext();
		Integer lLimit=this.getLimit();
		Integer lHits=this.getHits();
		Integer lTook=this.getTook()+elapsed;
		Integer lAct=actual;
		Integer lExcluded=excluded;
		Integer lUnique = this.getUniqueCount();
		Double lMaxScore=this.getMaxScore();

		String objectId=Environment.getInstance().getDataStore().formatDataStoreId(lid);

		if (api!=BaseQueryBuilder.API._count)
		{
			try
			{
				for (IQueryResult queryResult : this)
				{
					try
					{
						DataVertex vertex=queryResult.getVertex();
						if (StringX.isBlank(objectId))
						{
							objectId=vertex.fetchId().getValue();
						}
						if ((null!=vertex) && ClassX.isKindOf(vertex, Edge.class))
						{
							Edge edge=((Edge) vertex);
							Endpoint endpoint=edge.getOther(objectId);
							if (this.returnEdge)
							{
								vertex=edge;
							}
							else
							{
								DataVertex other=endpoint.getVertex();
								if (null!=other)
								{
									try
									{
										other.fetchOrdinal().setValue(edge.getEndpoint(objectId).fetchOrdinal().getValue());
										if(this.includeEdge || this.queryBuilder.includeEdge()){
											other.setEdge(edge);
										}
									}
									catch (Exception ignored){}
								}

								if (!StringX.isBlank(objectId) && (null!=other))
								{
									vertex=other;
								}
							}
						}

						// TODO: if edge points to a scoped object then we probably shouldn't return the edge if we cannot return the vertex.
						if ((null!=vertex) && !this.returnEdge)
						{
							if (this.scopingEnabled && (null!=credentials) && !ClassX.isKindOf(credentials, SystemCredentials.class))
							{
								if (vertex.enforcePolicy() && !PolicyDecisionPoint.isInScope(vertex, credentials))
								{
									vertex=null;
									lExcluded++;
								}
							}
						}

						if ((null!=vertex))
						{
							this.add(vertex);
						}
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().warning(ex);
					}
				}
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().warning(ex);
			}

			results=this.handleResults(format, handler);
			lAct+=this.vertices.size();
		}

		result.put("next", lNext);
		result.put("limit", lLimit);
		result.put("hits", lHits);
		result.put("took", lTook);
		result.put("maxScore", lMaxScore);
		result.put("actual", lAct);
		result.put("excluded", lExcluded);
		if(this.isAggregated()){
			result.put("unique", lUnique);
			result.put("isAggregated", true);
		}
		result.put("results", results);
		return result;
	}

	private void add(DataVertex vertex)
	{
		String id=vertex.fetchId().getValue();
		if (!this.getVertices().containsKey(id))
		{
			this.getVertices().put(id, vertex);
		}
	}

	public Integer getNext()
	{
		return this.next;
	}

	/**
	 * The limit that was set against the original query.
	 *
	 * @return The result limit size.
	 */
	public int getLimit()
	{
		return this.limit;
	}

	/**
	 * How long in mills did it take for the query to return with results.
	 *
	 * @return time took in mills.
	 */
	public Integer getTook()
	{
		return (null==this.took) ? 0 : this.took;
	}

	/**
	 * The highest score matched.
	 *
	 * @return The highest score matche.
	 */
	public Double getMaxScore()
	{
		return (null==this.maxScore) ? 0 : this.maxScore;
	}

	public Map<String, DataVertex> getVertices()
	{
		return this.vertices;
	}

	private JsonData handleResults(QueryResults.Format format, IQueryResultHandler handler)
	{
		JsonData results=JsonData.createArray();

		UserCredentials credentials = (null==this.getQueryBuilder()) ? null : this.getQueryBuilder().getCredentials();
		Map<String, Object> params = ((null==this.getQueryBuilder()) ? new HashMap<>() : this.getQueryBuilder().getParams());

		for (Map.Entry<String, DataVertex> entry : this.getVertices().entrySet())
		{
			JsonData item=null;
			if ((null==format) || Objects.equals(format, QueryResults.Format._default))
			{
				DataVertex vertex = entry.getValue();
				item=vertex.toJson(false);
				Set<BaseExtendedData> beds = BaseExtendedData.getFor(vertex.getClass());
				if(null!=beds){
					for(BaseExtendedData bed: beds)
					{
						bed.getExtendedData(credentials, vertex, item, params);
					}
				}
				if(this.includeEdge  && (null!=vertex.getEdge())){
					item.update("_edge", vertex.getEdge().toJson(false));
				}
			}
			else if (Objects.equals(format, QueryResults.Format.discovery))
			{
				DiscoveryItem discoveryItem=entry.getValue().getDiscoveryItem("", credentials, null, null, false);
				if (null!=discoveryItem)
				{
					item=discoveryItem.toJson();
					if (null!=handler)
					{
						handler.handle(entry.getValue(), item);
					}
				}
			}

			if (null!=item)
			{
				results.put(item);
			}
		}

		return results;
	}

	public boolean isAggregated()
	{
		return this.aggregated;
	}

	public JsonData getResponse()
	{
		return this.response;
	}

	public void setNext(int next)
	{
		this.next=next;
	}

	@SuppressWarnings("unused")
	public <T extends DataVertex> Collection<T> toCollection()
	{
		Collection<T> results=new ArrayList<>();
		for (IQueryResult queryResult : this)
		{
			T result=queryResult.getVertex();
			if (null!=result)
			{
				results.add(result);
			}
		}
		return results;
	}

	public <T extends DataVertex> T getOther(DataVertex vertex)
		throws ApplicationException
	{
		return this.getOther(vertex.fetchId().getValue());
	}

	public <T extends DataVertex> T getOther(String vertexId)
	{
		T result=null;
		DataVertex e=this.getFirst();
		if (e instanceof Edge)
		{
			Edge edge=(Edge) e;
			Endpoint endpoint=edge.getOther(vertexId);
			if ((null!=endpoint) && (null!=endpoint.getVertex()))
			{
				//noinspection unchecked
				result=(T) endpoint.getVertex();
			}
		}
		return result;
	}

	@SuppressWarnings("UnusedDeclaration")
	public <T extends DataVertex> T getAt(int index, Class<? extends DataVertex> cls)
	{
		T result=null;
		if (!this.isEmpty() && (index<this.size()))
		{
			IQueryResult queryResult=CollectionX.get(this.underlying, index);
			if (null!=queryResult)
			{
				result=this.getVertex(queryResult, 0);
			}
		}
		return result;
	}

	private JsonData toJson(BaseQueryBuilder qb, String lid, UserCredentials credentials, int actual, int excluded, int elapsed, QueryResults.Format format, IQueryResultHandler handler)
	{
		this.setReturnEdge(qb.returnEdge());
		this.setIncludeEdge(qb.includeEdge());
		JsonData result=null;
		try
		{
			JsonData item=this.toJson(lid, qb.getApi(), credentials, format, actual, excluded, elapsed, handler);
			// Ensure that the items returned match the limit requested if possible.
			result=this.handleResults(item, qb, lid, credentials, format, handler);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	private JsonData handleResults(JsonData item, BaseQueryBuilder qb, String lid, UserCredentials credentials, QueryResults.Format format, IQueryResultHandler handler)
	{
		JsonData result;
		this.setReturnEdge(qb.returnEdge());
		this.setIncludeEdge(qb.includeEdge());
		Integer actual=item.getInteger("actual");
		Integer excluded=item.getInteger("excluded");
		Integer fHits=item.getInteger("hits");
		Integer fNext=item.getInteger("next");

		if (!qb.isAggregated() && (0<fHits) && (fNext<fHits) && (actual<qb.getLimit()))
		{
			try
			{
				Integer fTook=item.getInteger("took");
				qb.setStart(fNext);

				QueryResults queryResults=qb.execute();

				JsonData nd=queryResults.toJson(qb, lid, credentials, actual, excluded, fTook, format, handler);
				actual=nd.getInteger("actual");
				fHits=nd.getInteger("hits");
				fNext=nd.getInteger("next");
				fTook=nd.getInteger("took");
				excluded=nd.getInteger("excluded");
				Integer fMaxScore=item.getInteger("maxScore");

				JsonData item1=item.getFromPath("results");
				JsonData item2=nd.getFromPath("results");
				if ((null!=item1) && item1.isValid() && (null!=item2) && item2.isValid())
				{
					item1.merge(item2, true, false, false);

					result=JsonData.createObject();
					result.put("next", fNext);
					result.put("limit", this.getLimit());
					result.put("hits", fHits);
					result.put("took", fTook);
					result.put("maxScore", fMaxScore);
					result.put("actual", actual);
					result.put("excluded", excluded);
					result.put("results", item1);
				}
				else
				{
					result=item;
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
				result=item;
			}
		}
		else
		{
			result=item;
		}

		return result;
	}

	private <T extends DataVertex>
	T getVertex(IQueryResult queryResult, int tries)
	{
		T result=queryResult.getVertex();
		if (null==result)
		{
			if (tries<3)
			{
				try
				{
					//noinspection MagicNumber
					Thread.sleep(300);
				}
				catch (Exception ignored)
				{
				}
				this.getVertex(queryResult, tries+1);
			}
			else
			{
				Environment.getInstance().getReportHandler().severe("The data store appears to be down.");
			}
		}

		return result;
	}

// Getters and setters
	@SuppressWarnings("UnusedDeclaration")
	public Integer getNextPagination()
	{
		Integer result=(this.next+this.size());
		if (null!=this.getHits())
		{
			int hit=this.getHits()-1;

			if ((result>hit) && (this.next<hit))
			{
				for (int i=0; i<this.size(); i++)
				{
					result=this.next+i;
					if (result>hit)
					{
						result--;
						break;
					}
				}
			}
			if (result>this.getHits())
			{
				result=this.getHits();
			}
		}
		return result;
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
	public Iterator<IQueryResult> iterator()
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
		//noinspection SuspiciousToArrayCall
		return this.underlying.toArray(a);
	}

	@Override
	public boolean add(IQueryResult queryResult)
	{
		return this.underlying.add(queryResult);
	}

	@SuppressWarnings("OverlyComplexMethod")
	@Override
	public boolean remove(Object o)
	{
		boolean result=false;
		//noinspection ChainOfInstanceofChecks
		if (o instanceof IQueryResult)
		{
			result=this.underlying.remove(o);
		}
		else if (o instanceof DataVertex)
		{
			DataVertex vertex=(DataVertex) o;
			Environment.getInstance().getReportHandler().notImplemented();
			for (IQueryResult queryResult : this)
			{
				DataVertex item=queryResult.getVertex();
				if ((null!=item) && ClassX.isKindOf(item.getClass(), Edge.class))
				{
					Edge edge=(Edge) item;
					String fId= edge.fetchEndpointFrom().getValue().fetchRelatedId().getValue();
					String tId=edge.fetchEndpointTo().getValue().fetchRelatedId().getValue();
					if (StringX.equals(fId, vertex.fetchId().getValue()) || (StringX.equals(tId, vertex.fetchId().getValue())))
					{
						result=this.remove(queryResult);
						break;
					}
				}
				if (null!=item)
				{
					if (StringX.equalsIgnoreCase(item.fetchId().getValue(), vertex.fetchId().getValue()))
					{
						result=this.remove(queryResult);
						break;
					}
				}
			}
		}
		if (result)
		{
			this.hits--;
			this.hits=(this.hits<0) ? 0 : this.hits;
		}
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.underlying.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends IQueryResult> c)
	{
		return this.underlying.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends IQueryResult> c)
	{
		return this.underlying.addAll(index, c);
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
	public IQueryResult get(int index)
	{
		return this.underlying.get(index);
	}

	@Override
	public IQueryResult set(int index, IQueryResult element)
	{
		return this.underlying.set(index, element);
	}

	@Override
	public void add(int index, IQueryResult element)
	{
		this.underlying.add(index, element);
	}

	@Override
	public IQueryResult remove(int index)
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
	public @NotNull
	ListIterator<IQueryResult> listIterator()
	{
		return this.underlying.listIterator();
	}

	@Override
	public @NotNull
	ListIterator<IQueryResult> listIterator(int index)
	{
		return this.underlying.listIterator(index);
	}

	@Override
	public @NotNull
	List<IQueryResult> subList(int fromIndex, int toIndex)
	{
		return this.underlying.subList(fromIndex, toIndex);
	}

	/**
	 * The total hits which may be more then the size.
	 *
	 * @return The total hits.
	 */
	public Integer getHits()
	{
		return (null==this.hits) ? 0 : this.hits;
	}

	public void setHits(int hits)
	{
		this.hits=hits;
	}

	public int getCount()
	{
		return (null!=this.hits) ? this.hits : 0;
	}

	public <T extends DataVertex> T getFirst()
	{
		T result=null;
		if (!this.isEmpty())
		{
			IQueryResult queryResult=CollectionX.getFirst(this.underlying);
			if (null!=queryResult)
			{
				result=this.getVertex(queryResult, 0);
			}
		}
		return result;
	}

	public <T extends DataVertex> T getSingleOrNull()
	{
		T result=null;
		if (!this.isEmpty())
		{
			IQueryResult queryResult=CollectionX.getSingleOrNull(this.underlying);
			if (null!=queryResult)
			{
				result=this.getVertex(queryResult, 0);
			}
		}
		return result;
	}

	public void setScopingEnabled(boolean enabled)
	{
		this.scopingEnabled=enabled;
	}

	public void setAggregated(boolean aggregated)
	{
		this.aggregated=aggregated;
	}

	public void setReturnEdge(boolean returnEdge)
	{
		this.returnEdge=returnEdge;
	}

	public void setIncludeEdge(boolean includeEdge)
	{
		this.includeEdge=includeEdge;
	}

	/**
	 * Set the unique count value of these query results.
	 * @param count An int.
	 */
	public void setUniqueCount(int count){
		this.uniqueCount = count;
	}

	/**
	 * Unique count value usually set by aggregated data.
	 * @return int.
	 */
	public int getUniqueCount(){
		return this.uniqueCount;
	}

	public void setResponse(JsonData response)
	{
		this.response=response;
	}

	public void setQueryBuilder(BaseQueryBuilder queryBuilder)
	{
		this.queryBuilder=queryBuilder;
	}

	public BaseQueryBuilder getQueryBuilder(){
		return this.queryBuilder;
	}
}