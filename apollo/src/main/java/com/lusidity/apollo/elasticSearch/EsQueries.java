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

package com.lusidity.apollo.elasticSearch;


import com.lusidity.Environment;
import com.lusidity.collections.PropertyAttributes;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.*;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.index.interfaces.IIndexEngine;
import org.apache.commons.lang.NotImplementedException;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.search.SearchParseException;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;

import static com.lusidity.data.interfaces.data.query.BaseQueryBuilder.Operators.must;

public class EsQueries implements IQueryFactory
{
// Fields
	public static final int INTERNAL_SERVER_ERROR=500;
	@SuppressWarnings("unused")
	private static final int DEFAULT_LIMIT=1000;
	@SuppressWarnings("unused")
	private static final long SCROLL_CACHE_TIMEOUT=60000;
	private final EsQueryDateFormat esDateQuery=new EsQueryDateFormat();

	@SuppressWarnings("unused")
	public QueryResults getFromHttp(BaseQueryBuilder builder)
	{
		return this.search(builder.getStore(), ClassHelper.getIndexKey(builder.getPartition()), builder.getJson(),
			builder, builder.isIncludeDuplicates(), builder.getStart(), builder.getLimit(),
			builder.isAggregated()
		);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	public QueryResults getFromClient(BaseQueryBuilder builder, int retries)
	{
		QueryResults results=new QueryResults(builder);
		if ((null==EsIndexStore.getInstance()) || (null==EsIndexStore.getInstance().getConfig()))
		{
			return results;
		}
		results.setAggregated(builder.isAggregated());
		results.setQueryBuilder(builder);
		try
		{
			BaseClient client;
			if (EsIndexStore.getInstance().getConfig().clientEnabled())
			{
				client=EsClientNode.getInstance();
			}
			else
			{
				client=EsClientTransport.getInstance();
			}

			assert client!=null;
			SearchRequestBuilder request=client.prepareSearch();

			if (builder.getApi()==BaseQueryBuilder.API._count)
			{
				builder.setStart(0);
				builder.setLimit(1);
			}

			if (null!=builder.getStore())
			{
				String store=ClassHelper.getIndexKey(builder.getStore());
				request.setIndices(store);
			}

			if (null!=builder.getPartition())
			{
				String partition=ClassHelper.getIndexKey(builder.getPartition());
				request=request.setTypes(partition);
			}

			JsonData q=(JsonData) builder.getQuery();
			String query=null;
			if (null!=q)
			{
				JsonData inner=q.getFromPath("query");
				if (null!=inner)
				{
					query=inner.toString();
				}

				JsonData sorts=q.getFromPath("sort");
				if (((null==sorts) || sorts.isEmpty()) && (builder.getApi()!=BaseQueryBuilder.API._count))
				{
					sorts=builder.getSort();
				}
				if ((null!=sorts) && sorts.isJSONArray())
				{
					for (Object o : sorts)
					{
						if (o instanceof JSONObject)
						{
							JsonData sort=new JsonData(o);
							String key=null;
							String order=null;
							for (String k : sort.keys())
							{
								if (StringX.equals(k, "order"))
								{
									order=k;
								}
								else
								{
									key=k;
								}
							}
							if (!StringX.isBlank(key))
							{
								FieldSortBuilder fsb=SortBuilders.fieldSort(key);
								fsb.ignoreUnmapped(true);
								JsonData config=sort.getFromPath(key);
								if ((null!=config) && config.isJSONObject())
								{
									for (String str : config.keys())
									{
										//noinspection SwitchStatementWithoutDefaultBranch
										switch (str)
										{
											case "missing":
												fsb.missing(config.getString(str));
												break;
											case "ignore_unmapped":
												break;
											case "order":
												order=config.getString("order");
												break;
										}
									}
								}

								if (null!=order)
								{
									fsb.order(StringX.equalsIgnoreCase(order, "asc") ? SortOrder.ASC : SortOrder.DESC);
								}
								request.addSort(fsb);
							}
						}
					}
				}

				if (builder.isAggregated())
				{
					String field=q.getString("aggs", "agg_result", "terms", "field");
					TermsBuilder tBuilder=AggregationBuilders.terms(field);
					request.addAggregation(tBuilder);
				}
			}

			request.setQuery(query);

			if (builder.getLimit()>0)
			{
				request.setSize(builder.getLimit());
			}
			if (builder.getStart()>0)
			{
				request.setFrom(builder.getStart());
			}

			SearchResponse searchResponse=request.execute().actionGet();
			int status=searchResponse.status().getStatus();
			String responseStr=searchResponse.toString();
			JsonData response=new JsonData(responseStr);
			int limit=builder.getLimit();
			int start=builder.getStart();
			boolean isAggregated=builder.isAggregated();

			results=this.getQueryResults(builder, status, response, limit, start, isAggregated);
			Environment.getInstance().getDataStore().setOffline(false);
		}
		catch (IndexNotFoundException ignored)
		{
		}
		catch (Exception ex)
		{
			results=this.handle(builder, retries, ex);
		}

		return results;
	}

	private QueryResults handle(BaseQueryBuilder builder, int retries, Exception ex)
	{
		QueryResults results=new QueryResults(builder);
		if (!Environment.getInstance().getDataStore().isOffline())
		{
			if (ClassX.isKindOf(ex, NoNodeAvailableException.class))
			{
				if (!ClassX.isKindOf(builder.getStore(), Identity.class) && (retries<Environment.getInstance().getConfig().getOfflineRetries()))
				{
					try
					{
						Thread.sleep(Environment.getInstance().getConfig().getOfflineWaitInterval());
					}
					catch (Exception ignored)
					{
					}
					results=this.getFromClient(builder, (retries+1));
				}
				else
				{
					Environment.getInstance().getDataStore().verifyConnection();
				}
			}
			else if (ClassX.isKindOf(ex, SearchParseException.class))
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
			else if (!ClassX.isKindOf(ex, IndexNotFoundException.class) && !ClassX.isKindOf(ex, org.elasticsearch.index.IndexNotFoundException.class)
			         && !ClassX.isKindOf(ex, SearchPhaseExecutionException.class))
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
			else
			{
				if (EsConfiguration.getInstance().isDeadLockEnabled())
				{
					try
					{
						Thread.sleep(Environment.getInstance().getConfig().getOfflineWaitInterval());
					}
					catch (Exception ignored)
					{
					}
					results=this.getFromClient(builder, 0);
				}
				else
				{
					//Environment.getInstance().getReportHandler().severe(ex);
				}
			}
		}
		else
		{
			if (EsConfiguration.getInstance().isDeadLockEnabled())
			{
				try
				{
					Thread.sleep(Environment.getInstance().getConfig().getOfflineWaitInterval());
				}
				catch (Exception ignored)
				{
				}
				results=this.getFromClient(builder, 0);
			}
			else
			{
				//Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return results;
	}	public @NotNull QueryResults getQueryResults(BaseQueryBuilder builder, int status, JsonData response, int limit, int start, boolean isAggregated)
	{
		QueryResults results=new QueryResults(builder);
		if ((status>=200) && (status<300))
		{
			Double maxScore=response.getDouble("hits::max_score");
			Integer took=response.getInteger("took");
			Integer total=response.getInteger("hits::total");
			Integer count=response.getInteger("count");
			if ((null!=count) && ((null==total) || (count>total)))
			{
				total=count;
			}

			results=new QueryResults(builder, maxScore, took, total, start, limit, isAggregated);
			results.setResponse(response);

			JsonData items=response.getFromPath("hits::hits");

			if ((null!=items) && items.isJSONArray())
			{
				for (Object o : items)
				{
					if (o instanceof JSONObject)
					{
						JsonData item=new JsonData(o);
						String id=item.getString(String.format("_source::%s", IDataStore.DATA_STORE_ID));
						Double score=item.getDouble("_score");
						EsQueryResult queryResult=new EsQueryResult(id, 0, score, item, (null==builder) ? null : builder.getStore(), (null==builder) ? null : builder.getPartition());
						results.add(queryResult);
					}
				}
			}
		}
		else
		{
			results.setResponse(response);
		}
		return results;
	}

	@Override
	public JsonData request(HttpClientX.Methods method, Class<? extends DataVertex> store, String elementType, BaseQueryBuilder.API api, JsonData content, int start, int limit, String... params)
	{
		Collection<String> items=new ArrayList<>();
		if (limit>0)
		{
			if ((null==content) || (api==BaseQueryBuilder.API._count))
			{
				items.add("size");
				items.add(String.format("%d", limit));
			}
			else if (!content.hasKey("size"))
			{
				content.put("size", limit);
			}
		}
		if ((start>0))
		{
			if ((null==content) || (api==BaseQueryBuilder.API._count))
			{
				items.add("from");
				items.add(String.format("%d", start));
			}
			else if (!content.hasKey("from"))
			{
				content.put("from", start);
			}

		}
		if (null!=params)
		{
			Collections.addAll(items, params);
		}
		String[] queryParams=new String[items.size()];
		int on=0;
		for (String item : items)
		{
			queryParams[on]=item;
			on++;
		}
		String finalParams=this.buildQueryParams(queryParams);
		if (!StringX.isBlank(finalParams))
		{
			finalParams=String.format("?%s", finalParams);
		}

		URL url=Environment.getInstance().getIndexStore().getEngine().getIndexUrl(store, elementType,
			((api==BaseQueryBuilder.API._delete_by_query) ? BaseQueryBuilder.API._query : api), finalParams
		);

		return Environment.getInstance().getIndexStore().getEngine().getResponse(url, method, content);
	}

	@Override
	public QueryResults request(HttpClientX.Methods method, String endPoint, JsonData content, int start, int limit, String... params)
	{
		String finalParams="";
		if (null!=params)
		{
			this.buildQueryParams(params);
			if (!StringX.isBlank(finalParams))
			{
				finalParams=String.format("?%s", finalParams);
			}
		}

		URL url=Environment.getInstance().getIndexStore().getEngine().getIndexUrl(endPoint, finalParams);

		JsonData response=Environment.getInstance().getIndexStore().getEngine().getResponse(url, method, content);
		int status=(null!=response) ? response.getInteger(EsQueries.INTERNAL_SERVER_ERROR, "http_response_status") : EsQueries.INTERNAL_SERVER_ERROR;
		return this.getQueryResults(null, status, response, start, limit, false);
	}



	private String buildQueryParams(String... params)
	{
		@SuppressWarnings("NonConstantStringShouldBeStringBuffer")
		String result="";
		if ((null!=params) && (params.length>0))
		{
			int nParams=params.length;
			if ((nParams%2)!=0)
			{
				throw new IllegalArgumentException("Params must be key/value pairs expressed as String pairs.");
			}
			for (int keyIndex=0; keyIndex<(nParams-1); keyIndex++)
			{
				int valueIndex=keyIndex+1;
				String key=params[keyIndex];
				String value=params[valueIndex];
				//noinspection StringConcatenationInLoop
				result=String.format("%s%s%s=%s", result, StringX.isBlank(result) ? "" : "&", key, value);
				//noinspection AssignmentToForLoopParameter
				keyIndex++;
			}
		}
		return result;
	}


	@Override
	public QueryResults get(BaseQueryBuilder builder)
	{
		if (Environment.getInstance().getDataStore().isOffline() && !ClassX.isKindOf(builder.getStore(), Identity.class))
		{
			Environment.getInstance().getDataStore().waitForConnection();
		}

		QueryResults results;
		if (builder.isHttpRequest() || (builder.getApi()==BaseQueryBuilder.API._delete_by_query))
		{
			results=this.getFromHttp(builder);
		}
		else
		{
			results=this.getFromClient(builder, 0);
		}
		results.setAggregated(builder.isAggregated());
		if (EsDataStore.getInstance().isStatisticsAvailable())
		{
			EsDataStore.getInstance().getStatistics(false).fetchQueried().getValue().increment();
		}
		return results;
	}

	@Override
	public QueryResults byValue(Class<? extends DataVertex> store, Class<? extends DataVertex> expected,
	                            ValueObjects valueObjects, SortObjects sortObjects, int start, int limit)
	{
		QueryResults results=new QueryResults(null);
		try
		{
			if (null!=valueObjects)
			{
				BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, expected, start, limit);
				if (null!=sortObjects)
				{
					for (SortObject sortObject : sortObjects)
					{
						qb.sort(sortObject.getKey(), sortObject.getSort());
					}
				}
				for (ValueObject valueObject : valueObjects)
				{
					qb.filter(must, valueObject.getKey(), BaseQueryBuilder.StringTypes.folded, valueObject.getValue());
				}
				results=qb.execute();
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return results;
	}

	@Override
	public QueryResults matchAll(Class<? extends DataVertex> store, Class<? extends DataVertex> expected, SortObjects sortObjects, int start, int limit)
	{
		QueryResults results=new QueryResults(null);
		try
		{
			BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, expected, start, limit);
			if (null!=sortObjects)
			{
				for (SortObject sortObject : sortObjects)
				{
					qb.sort(sortObject.getKey(), sortObject.getSort());
				}
			}
			qb.matchAll();
			results=qb.execute();
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return results;
	}

	@Override
	public QueryResults startsWith(Class<? extends DataVertex> store, Class<? extends DataVertex> expected, ValueObjects valueObjects, SortObjects sortObjects, int start, int limit)
	{
		QueryResults results=new QueryResults(null);
		try
		{
			if (null!=valueObjects)
			{
				BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, expected, start, limit);
				if (null!=sortObjects)
				{
					for (SortObject sortObject : sortObjects)
					{
						qb.sort(sortObject.getKey(), sortObject.getSort());
					}
				}
				for (ValueObject valueObject : valueObjects)
				{
					qb.filter(must, valueObject.getKey(), BaseQueryBuilder.StringTypes.starts_with, valueObject.getValue());
				}
				results=qb.execute();
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return results;
	}

	@Override
	public int count(BaseQueryBuilder queryBuilder)
	{
		queryBuilder.setApi(BaseQueryBuilder.API._count);
		QueryResults queryResults=queryBuilder.execute();
		queryBuilder.setApi(BaseQueryBuilder.API._search);
		return queryResults.getCount();
	}

	@Override
	public <T extends DataVertex> Collection<T> query(String statement, Class<? extends DataVertex> cls, int start, int limit)
	{
		throw new NotImplementedException("Not Implemented.");
	}


	@Override
	public <T extends DataVertex> Collection<T> getVerticesByType(Class<? extends Edge> store, DataVertex vertex, String key, BaseQueryBuilder.Sort sort, Common.Direction direction, Class valueCls,
	                                                              int start, int limit)
	{
		Collection<T> results=new ArrayList<>();

		QueryResults queryResults=this.getEdges(store, vertex, key, null, null, direction, start, limit);
		if (!queryResults.isEmpty())
		{
			Collection<Edge> edges=this.transformResults(Edge.class, queryResults);
			if (!edges.isEmpty())
			{
				//noinspection unchecked
				results=this.transformEdges(valueCls, edges, direction, vertex);
			}
		}

		return results;
	}

	@Override
	public int getVerticesByTypeCount(Class<? extends DataVertex> store, DataVertex vertex, String key, Common.Direction direction, Class<ApolloVertex> valueClass)
	{
		return this.getEdgeCount(Edge.class, vertex, key, direction);
	}

	@Override
	public <T extends Edge> T getEdge(Class<? extends Edge> store, DataVertex from, DataVertex to, String label)
	{
		T result=null;
		if ((null!=from) && (null!=from.fetchId().getValue()) && (null!=to) && (null!=to.fetchId().getValue()) && !StringX.isBlank(label))
		{
			result=this.getEdge(store, from.getClass(), from.fetchId().getValue(), to.fetchId().getValue(), label);
		}
		return result;
	}

	@Override
	public <T extends Edge> T getEdge(Class<? extends Edge> store, IQueryResult from, IQueryResult to, String label)
	{
		T result=null;
		if ((null!=from) && (null!=from.getId()) && (null!=to) && (null!=to.getId()) && !StringX.isBlank(label))
		{
			result=this.getEdge(store, null, from.getId(), to.getId(), label);
		}
		return result;
	}

	@Override
	public <T extends Edge> T getEdge(Class<? extends Edge> store, Class<? extends DataVertex> partitionType, String fromId, String toId, String label)
	{
		T result=null;
		ValueObjects valueObjects=ValueObjects.create(Endpoint.KEY_FROM_EP_ID, fromId).add(Endpoint.KEY_TO_EP_ID, toId).add("label", label);

		QueryResults queryResults=this.byValue(store, partitionType, valueObjects, null, 0, 0);

		if (!queryResults.isEmpty())
		{
			if (queryResults.size()>1)
			{
				// enforce single edge between two vertices.
				for (IQueryResult queryResult : queryResults)
				{
					Edge edge=queryResult.getVertex();
					// only use the most current modified.
					//noinspection OverlyStrongTypeCast
					if ((null==result) || edge.fetchModifiedWhen().getValue().isAfter(result.fetchModifiedWhen().getValue()))
					{
						//noinspection unchecked
						result=(T) edge;
						break;
					}
				}
			}
			else
			{
				result=queryResults.getFirst();
			}
		}
		return result;
	}

	@Override
	public QueryResults getEdges(Class<? extends Edge> store, DataVertex vertex, String key, ValueObjects valueObjects, SortObjects sortObjects, Common.Direction direction, int start, int limit)
	{
		BaseQueryBuilder qb=this.getEdgeQuery(store, vertex, key, direction, start, limit);
		if (null!=valueObjects)
		{
			for (ValueObject vo : valueObjects)
			{
				qb.filter(must, vo.getKey(), BaseQueryBuilder.StringTypes.raw, vo.getValue());
			}
		}
		if (null!=sortObjects)
		{
			for (SortObject so : sortObjects)
			{
				String field=so.getKey();
				if (field.equals("ordinal"))
				{
					field=String.format("%s_%s", field, (direction==Common.Direction.OUT) ? "from" : "to");
				}
				qb.sort(field, so.getSort());
			}
		}
		return qb.execute();
	}

	@Override
	public QueryResults getEdges(DataVertex vertex, PropertyAttributes propertyAttributes, ValueObjects valueObjects, SortObjects sortObjects, int start, int limit)
	{
		BaseQueryBuilder qb=this.getEdgeQuery(propertyAttributes.getEdgeType(), vertex, propertyAttributes.getKey(), propertyAttributes.getDirection(), start, limit);
		return qb.execute();
	}

	@Override
	public <T extends DataVertex> Collection<T> transformResults(Class<? extends DataVertex> cls, QueryResults queryResults)
	{
		Collection<T> results=new ArrayList<>();
		if (null!=queryResults)
		{
			for (IQueryResult queryResult : queryResults)
			{
				@SuppressWarnings("unchecked")
				T result=queryResult.getVertex();
				if (null!=result)
				{
					result.setIndexId(queryResult.getIndexId());
					results.add(result);
				}
			}
		}
		return results;
	}

	@Override
	public <T extends DataVertex> Collection<T> transformEdges(Class<? extends DataVertex> valueCls, Collection<? extends Edge> edges, Common.Direction direction, DataVertex vertex)
	{
		Collection<T> results=new ArrayList<>();
		if (!edges.isEmpty())
		{
			for (Edge edge : edges)
			{
				Endpoint working=edge.getOther(vertex.fetchId().getValue());
				@SuppressWarnings({
					"unchecked",
					"RedundantCast"
				})
				T result=(T) Environment.getInstance().getDataStore().getObjectById(valueCls, valueCls, working.fetchRelatedId().getValue());
				//noinspection StatementWithEmptyBody
				if (null!=result)
				{
					Class<? extends DataVertex> actual=result.getClass();
					if (ClassX.isKindOf(actual, valueCls))
					{
						if (!Objects.equals(actual, valueCls))
						{
							//noinspection unchecked
							result=(T) ClassHelper.as(vertex, actual);
						}
						result.fetchOrdinal().setValue(working.fetchOrdinal().getValue());
						results.add(result);
					}
				}
				else
				{
					// TODO: Delete the edge and indexes.
				}
			}
		}

		return results;
	}

	@Override
	public int getEdgeCount(Class<? extends Edge> cls, DataVertex vertex, String key, Common.Direction direction)
	{
		BaseQueryBuilder qb=this.getEdgeQuery(cls, vertex, key, direction, 0, 1);
		return this.count(qb);
	}

	@Override
	public boolean delete(BaseQueryBuilder builder)
	{
		JsonData response=this.request(HttpClientX.Methods.DELETE, builder.getStore(), null, BaseQueryBuilder.API._query, builder.getJson(), builder.getStart(), builder.getLimit(), null);
		return ((null!=response) && response.isValid());
	}

	@Override
	public BaseQueryBuilder getEdgeQuery(Class<? extends Edge> store, DataVertex vertex, String key, Common.Direction direction, int start, int limit)
	{
		Class<? extends DataVertex> partitionType=(direction==Common.Direction.OUT) ? vertex.getClass() : null;

		String sortLabel=(direction==Common.Direction.OUT) ? Endpoint.KEY_FROM_EP_ORDINAL : Endpoint.KEY_TO_EP_ORDINAL;
		String endpointLabel=(direction==Common.Direction.OUT) ? Endpoint.KEY_FROM_EP_ID : Endpoint.KEY_TO_EP_ID;

		BaseQueryBuilder result=Environment.getInstance().getIndexStore().getQueryBuilder(store, partitionType, start, limit);

		if (!StringX.isBlank(key))
		{
			result.filter(must, "label", BaseQueryBuilder.StringTypes.raw, key);
		}

		if (direction==Common.Direction.BOTH)
		{
			result.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_FROM_EP_ID, BaseQueryBuilder.StringTypes.raw, vertex.fetchId().getValue());
			result.filter(BaseQueryBuilder.Operators.should, Endpoint.KEY_TO_EP_ID, BaseQueryBuilder.StringTypes.raw, vertex.fetchId().getValue());
		}
		else
		{
			result.filter(must, endpointLabel, BaseQueryBuilder.StringTypes.raw, vertex.fetchId().getValue());
		}

		result.sort(sortLabel, BaseQueryBuilder.Sort.asc);

		return result;
	}

	@Override
	public BaseQueryDateFormat getDateQueries()
	{
		return this.esDateQuery;
	}

	@Override
	public QueryResults getAllProperties(DataVertex vertex, int start, int limit)
	{
		QueryResults results=new QueryResults(null);
		Set<Class<? extends Edge>> types=Environment.getInstance().getReflections().getSubTypesOf(Edge.class);
		types.add(Edge.class);
		for (Class<? extends Edge> type : types)
		{
			BaseQueryBuilder qb=this.getEdgeQuery(type, vertex, null, Common.Direction.BOTH, start, limit);
			QueryResults qrs=qb.execute();
			for (IQueryResult qr : qrs)
			{
				results.add(qr);
			}
		}
		return results;
	}

	@Override
	public long getLinkCount(Class<? extends Edge> store, DataVertex vertex)
	{
		return this.getLinkCount(store, vertex, null, Common.Direction.BOTH);
	}

	@Override
	public long getLinkCount(Class<? extends Edge> store, DataVertex vertex, String key, Common.Direction direction)
	{
		BaseQueryBuilder qb=this.getEdgeQuery(store, vertex, key, direction, 0, 1);
		return this.count(qb);
	}

	@Override
	public long getLinkCount(Class<? extends Edge> store, DataVertex vertex, String key)
	{
		return this.getLinkCount(store, vertex, key, Common.Direction.BOTH);
	}

	@Override
	public <T extends DataVertex> Collection<T> hasProperty(int limit, Class<? extends DataVertex> cls, String... properties)
	{
		throw new NotImplementedException("Not Implemented.");
	}

	@Override
	public QueryResults getVertices(Class<? extends DataVertex> store, SortObjects sortObjects, int start, int limit)
	{
		QueryResults results=new QueryResults(null);
		try
		{
			BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, start, limit);
			if (null!=sortObjects)
			{
				for (SortObject sortObject : sortObjects)
				{
					qb.sort(sortObject.getKey(), sortObject.getSort());
				}
			}
			qb.filter(BaseQueryBuilder.Operators.matchAll, null, BaseQueryBuilder.StringTypes.na, null);
			results=qb.execute();
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return results;
	}

	@Override
	public IQueryResult byId(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, Object id)
	{
		QueryResults queryResults=this.byValue(store, partitionType, ValueObjects.create(IDataStore.DATA_STORE_ID, id), null, 0, 0);
		return CollectionX.getFirst(queryResults);
	}

	@SuppressWarnings("OverlyComplexMethod")
	@Override
	public QueryResults search(Class<? extends DataVertex> store, String elementType, JsonData content, BaseQueryBuilder baseQueryBuilder, boolean includeDuplicates, int start, int limit, boolean
		isAggregated, String... params)
	{
		IIndexEngine indexPartition=Environment.getInstance().getIndexStore().getEngine();
		final JsonData sorted=JsonData.createArray();
		if (isAggregated && content.hasKey("sorted"))
		{
			JsonData temp;
			JsonData sorts=content.getFromPath("sorted");
			if (sorts.isJSONObject())
			{
				temp=JsonData.createArray();
				temp.put(sorts);
			}
			else if (sorts.isJSONArray())
			{
				temp=sorts;
			}
			else
			{
				temp=null;
			}
			if (null!=temp)
			{
				for (Object o : temp)
				{
					JsonData data=JsonData.create(o);
					String key=data.getString("key");
					Boolean asc=data.getBoolean(true, "asc");
					String type="string";
					if (StringX.contains(key, ":"))
					{
						String[] parts=key.split(":");
						key=parts[0];
						type=parts[1];
					}
					if (StringX.equals(key, "_aggHits"))
					{
						type="numeric";
					}
					sorted.put(JsonData.createObject().put("key", key).put("asc", asc).put("type", type));
				}
			}

			content.remove("sorted");
		}

		JsonData response=this.request(
			(baseQueryBuilder.getApi()==BaseQueryBuilder.API._delete_by_query) ? HttpClientX.Methods.DELETE : HttpClientX.Methods.GET,
			store, elementType, baseQueryBuilder.getApi(), content, start, limit, params
		);

		QueryResults results=new QueryResults(null);

		if (indexPartition.isValidResponse(response))
		{
			Double maxScore=response.getDouble("hits::max_score");
			Integer took=response.getInteger("took");
			Integer total=isAggregated ? 0 : response.getInteger(0, "hits::total");
			Integer count=isAggregated ? 0 : response.getInteger(0, "count");
			if (baseQueryBuilder.getApi()==BaseQueryBuilder.API._count)
			{
				total=count;
			}

			results=new QueryResults(baseQueryBuilder, maxScore, took, total, start, limit, false);
			results.setAggregated(isAggregated);
			results.setResponse(response);

			JsonData items=response.getFromPath(isAggregated ? "aggregations::agg_result::buckets" : "hits::hits");

			if ((null!=items) && items.isJSONArray())
			{
				List<EsQueryResult> aggregations=new ArrayList<>();
				for (Object o : items)
				{
					if (o instanceof JSONObject)
					{
						JsonData item=new JsonData(o);
						String id=null;
						count++;
						Double score=0.0;
						int aggHits=0;
						String key=null;
						if (isAggregated)
						{
							aggHits=item.getInteger(0, "doc_count");
							total+=aggHits;

							key=item.getString("key");
							JsonData data=item.getFromPath("only_one_post", "hits", "hits");
							if ((null!=data) && data.isJSONArray() && !data.isEmpty())
							{
								item=JsonData.create(data.at(0));
								JsonData source=item.getFromPath("_source");
								source.put("_aggHits", aggHits, true);
								id=source.getString(IDataStore.DATA_STORE_ID);
								score=data.getDouble("_score");
							}
						}
						else
						{
							id=item.getString(String.format("_source::%s", IDataStore.DATA_STORE_ID));
							score=item.getDouble("_score");
						}

						if (null!=id)
						{
							EsQueryResult queryResult=new EsQueryResult(id, aggHits, score, item, store, store);
							queryResult.setKey(key);
							if (isAggregated && (limit>0))
							{
								aggregations.add(queryResult);
							}
							else
							{
								results.add(queryResult);
							}
						}
					}
				}
				if (isAggregated)
				{
					int next=start+limit;
					next=(next>count) ? count : next;
					results.setNext(next);
					results.setHits(total);
					results.setUniqueCount(count);

					if (!aggregations.isEmpty())
					{
						if (!sorted.isEmpty())
						{
							aggregations.sort(new Comparator<EsQueryResult>()
							{
								// Overrides
								@Override
								public int compare(EsQueryResult a, EsQueryResult b)
								{
									int result=0;
									int chain=-1;
									for (Object o : sorted)
									{
										JsonData sort=JsonData.create(o);
										String key=sort.getString("key");
										boolean asc=sort.getBoolean(true, "asc");
										String type=sort.getString("type");
										try
										{
											if (!StringX.isBlank(key))
											{
												Object ao=StringX.equals(key, "_aggHits") ? a.getAggHits() : a.getIndexData().getString("_source", key);
												Object bo=StringX.equals(key, "_aggHits") ? b.getAggHits() : b.getIndexData().getString("_source", key);
												switch (type)
												{
													case "datetime":
													{
														DateTime val1=(null!=ao) ? DateTimeX.parse(ao.toString()) : null;
														DateTime val2=(null!=bo) ? DateTimeX.parse(bo.toString()) : null;
														chain=DateTimeX.compare(val1, val2);
														break;
													}
													case "boolean":
													{
														Boolean val1=(null!=ao) ? Boolean.parseBoolean(ao.toString()) : null;
														Boolean val2=(null!=bo) ? Boolean.parseBoolean(bo.toString()) : null;
														chain=Boolean.compare(val1, val2);
														break;
													}
													case "numeric":
													{
														Float val1=(null!=ao) ? Float.parseFloat(ao.toString()) : null;
														Float val2=(null!=bo) ? Float.parseFloat(bo.toString()) : null;
														chain=Float.compare(val1, val2);
														break;
													}
													case "string":
													default:
													{
														String val1=(null!=ao) ? ao.toString() : null;
														String val2=(null!=bo) ? bo.toString() : null;
														chain=StringX.compare(val1, val2);
														break;
													}
												}
											}
										}
										catch (Exception ignored)
										{
											chain=0;
										}

										if (chain!=0)
										{
											if (!asc)
											{
												chain*=-1;
											}
											break;
										}
									}
									result=chain;
									return result;
								}
							});
						}

						int pass=0;
						int on=0;
						for (EsQueryResult qr : aggregations)
						{
							if (pass<start)
							{
								pass++;
								continue;
							}
							pass++;
							if (on>limit)
							{
								break;
							}
							on++;
							results.add(qr);
						}
					}
				}
			}
		}
		else
		{
			results.setAggregated(isAggregated);
			results.setResponse(response);
		}

		return results;
	}
}
