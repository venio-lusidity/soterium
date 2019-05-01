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

package com.lusidity.data.interfaces.data;


import com.lusidity.data.DataVertex;
import com.lusidity.data.bulk.BulkItems;
import com.lusidity.data.interfaces.data.query.IQueryFactory;
import com.lusidity.domains.organization.Organization;
import com.lusidity.domains.system.apollo.ApolloStatistics;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.index.interfaces.IIndexStore;
import com.lusidity.server.IServer;
import org.joda.time.Duration;

public interface IDataStore extends IServer
{
	enum Operations
	{
		create,
		delete,
		disconnect,
		query,
		response,
		update,
		ping,
		error,
		testing,
		batch;

		// Methods
		public static IDataStore.Operations get(JsonData request)
		{
			return request.getEnum(IDataStore.Operations.class, "operation");
		}
	}

	enum QueryTypes
	{
		stored,
		stated
	}

	enum ObjectTypes
	{
		entity,
		relationship,
		label,
		index,
		batch
	}

	// Fields
	String DATA_STORE_ID="lid";
	String KEY_DB_CONTENT="vt_c";

	// Overrides
	void close();

	boolean isOpened();

	boolean create(Class<? extends DataVertex> store, DataVertex dataVertex,
	               int attempt)
		throws ApplicationException;

	Object execute(String statement);

	boolean execute(IOperation operation)
		throws Exception;

	<T extends DataVertex> T getObjectById(Class<? extends DataVertex> store, Class<? extends DataVertex>
		partitionType, Object dataStoreId, boolean ignoreCache);

	<T extends DataVertex> T getObjectById(Class<? extends DataVertex> store,
	                                       Class<? extends DataVertex> partitionType, Object dataStoreId);

	boolean update(DataVertex dataVertex,
	               int attempt)
		throws ApplicationException;

	boolean delete(Class<? extends DataVertex> store, DataVertex dataVertex, int attempts);

	String formatDataStoreId(Object dataStoreId);

	JsonData report(JsonData options);

	Object getVertexId(DataVertex vertex);

	void removeVertexId(JsonData dso);

	void updateSearchStatus(Duration duration);

	void waitForConnection();

	/**
	 * Can this currently connect to the data store or is the data store offline?
	 * If not relevant default to true.
	 */
	boolean verifyConnection();

	void commitAll();

	Integer count(Class<? extends DataVertex> cls);

	boolean drop(Class<? extends DataVertex> cls);

	JsonData toJson();

	boolean delete(Class<Organization> store, Class<Organization> partition, String dataStoreId);

	Object getSchema(Class<? extends DataVertex> store);

	IDataThreadPool getDataPool();

	// Getters and setters
	String getDataStoreIdKey();

	IIndexStore getIndexStore();

	IQueryFactory getQueries();

	IOperation getOperation(IOperation.Type type, DataVertex vertex);
	IOperation getOperation(Class<? extends DataVertex> storeType, IOperation.Type type, DataVertex vertex);
	IOperation getOperation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partition, IOperation.Type type, BulkItems<? extends DataVertex> bulkItems);
	IOperation getOperation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, IOperation.Type type, DataVertex vertex);

	ApolloStatistics getStatistics(boolean start);
	boolean isStatisticsAvailable();
}
