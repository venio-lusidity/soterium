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

package com.lusidity.index.interfaces;

import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.BaseQueryDateFormat;
import com.lusidity.data.interfaces.data.query.IQueryFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.server.IServer;
import org.joda.time.DateTime;

public interface IIndexStore extends IServer
{

	boolean create(Class<? extends DataVertex> store, DataVertex dataVertex, Object dataStoreId);

	boolean delete(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, String indexId);

	boolean delete(Class<? extends DataVertex> store, DataVertex dataVertex);

	boolean delete(String store, String partition, String indexId);

	boolean update(Class<? extends DataVertex> store, DataVertex dataVertex);

	JsonData toJson();

	JsonData report(JsonData options);

	void makeAvailable(Class<? extends DataVertex> store, boolean enforce);

	boolean isValidResponse(JsonData response);

	BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, int start, int limit);

	BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, BaseQueryBuilder.API api, int start, int limit);

	BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, JsonData query, BaseQueryBuilder.API api, int start, int limit);

	BaseQueryDateFormat getQueryDateFormat(DateTime dateTime);
	BaseQueryDateFormat getQueryDateFormat(DateTime from, DateTime to);

	IReIndexer getReIndexer(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, JsonData map)
		throws ApplicationException;

	boolean drop(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType);

	boolean exists(Class<? extends DataVertex> StoreType, Class<? extends DataVertex> partitionType);

	void waitForConnection();

	boolean verifyConnection();

	void reset(Class<? extends DataVertex> store);

	boolean execute(IOperation operation)
		throws Exception;

	BaseQueryBuilder getQueryBuilder(String store, String partition, int start, int limit);

	IOperation getOperation(IOperation.Type type, DataVertex vertex);

	IOperation getOperation(Class<? extends DataVertex> storeType, IOperation.Type type, DataVertex vertex);

	IOperation getOperation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, IOperation.Type type, DataVertex vertex);

	Object getSchema(Class<? extends DataVertex> store);

	// Getters and setters
	IIndexEngine getEngine();

	IQueryFactory getQueryFactory();

	boolean isConnected();

	boolean isBatchMode();
}
