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


import com.lusidity.collections.PropertyAttributes;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;

import java.util.Collection;

public interface IQueryFactory
{

	int count(BaseQueryBuilder queryBuilder);

	<T extends DataVertex> Collection<T> query(String statement, Class<? extends DataVertex> cls, int start, int limit);

	QueryResults getAllProperties(DataVertex vertex, int start, int limit);

	<T extends Edge> T getEdge(Class<? extends Edge> store, DataVertex from, DataVertex to, String key);

	<T extends Edge> T getEdge(Class<? extends Edge> store, IQueryResult from, IQueryResult to, String label);

	<T extends Edge> T getEdge(Class<? extends Edge> store, Class<? extends DataVertex> partitionType, String fromId, String toId, String label);

	QueryResults getEdges(Class<? extends Edge> store, DataVertex vertex, String key, ValueObjects valueObjects, SortObjects sortObjects, Common.Direction direction, int start, int limit);

	QueryResults getEdges(DataVertex vertex, PropertyAttributes propertyAttributes, ValueObjects valueObjects, SortObjects sortObjects, int start, int limit);

	BaseQueryBuilder getEdgeQuery(Class<? extends Edge> store, DataVertex vertex, String key, Common.Direction direction, int start, int limit);

	<T extends DataVertex> Collection<T> transformResults(Class<? extends DataVertex> cls, QueryResults queryResults);

	<T extends DataVertex> Collection<T> transformEdges(Class<? extends DataVertex> valueCls, Collection<? extends Edge> edges, Common.Direction direction, DataVertex vertex);

	int getEdgeCount(Class<? extends Edge> cls, DataVertex vertex, String key, Common.Direction direction);

	boolean delete(BaseQueryBuilder builder);

	<T extends DataVertex> Collection<T> getVerticesByType(Class<? extends Edge> store, DataVertex vertex, String key, BaseQueryBuilder.Sort sort, Common.Direction direction, Class valueCls, int
		start, int limit);

	int getVerticesByTypeCount(Class<? extends DataVertex> store, DataVertex vertex, String key, Common.Direction direction, Class<ApolloVertex> valueClass);

	long getLinkCount(Class<? extends Edge> store, DataVertex vertex);

	long getLinkCount(Class<? extends Edge> store, DataVertex vertex, String key, Common.Direction direction);

	long getLinkCount(Class<? extends Edge> store, DataVertex vertex, String key);

	<T extends DataVertex> Collection<T> hasProperty(int limit, Class<? extends DataVertex> cls, String... properties);

	QueryResults getVertices(Class<? extends DataVertex> store, SortObjects sortObjects, int start, int limit);

	QueryResults get(BaseQueryBuilder builder);

	IQueryResult byId(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, Object id);

	QueryResults byValue(Class<? extends DataVertex> store, Class<? extends DataVertex> expected, ValueObjects valueObjects, SortObjects sortObjects, int start, int limit);

	QueryResults matchAll(Class<? extends DataVertex> store, Class<? extends DataVertex> expected, SortObjects sortObjects, int start, int limit);

	QueryResults startsWith(Class<? extends DataVertex> store, Class<? extends DataVertex> expected, ValueObjects valueObjects, SortObjects sortObjects, int start, int limit);

	QueryResults search(Class<? extends DataVertex> store, String elementType, JsonData content, BaseQueryBuilder baseQueryBuilder, boolean includeDuplicates, int start, int limit, boolean isAggregated, String... params);

	JsonData request(HttpClientX.Methods method, Class<? extends DataVertex> store, String elementType, BaseQueryBuilder.API api, JsonData content, int start, int limit, String... params);

	QueryResults request(HttpClientX.Methods method, String endPoint, JsonData content, int start, int limit, String... params);

	// Getters and setters
	BaseQueryDateFormat getDateQueries();
}
