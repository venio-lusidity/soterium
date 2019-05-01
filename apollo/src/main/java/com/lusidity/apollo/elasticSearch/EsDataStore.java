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
import com.lusidity.apollo.common.Operation;
import com.lusidity.data.DataVertex;
import com.lusidity.data.bulk.BulkItems;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.IDataThreadPool;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryFactory;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.organization.Organization;
import com.lusidity.domains.system.apollo.ApolloStatistics;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.interfaces.IIndexStore;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.Duration;

public class EsDataStore implements IDataStore
{
	// Fields
	public static final int DEFAULT_THREAD_POOL_SIZE=50;
	public static final int RETRY_MAX=300;
	private static EsDataStore INSTANCE=null;
	private boolean statisticsAvailable=false;
	private EsConfiguration config=null;
	private boolean opened=false;
	private boolean offline=false;

	private ApolloStatistics statistics=null;
	private IDataThreadPool dataPool=null;

	// Constructors
	public EsDataStore()
	{
		super();
		EsDataStore.INSTANCE=this;
	}

	// Overrides
	@Override
	public void close()
	{
		try
		{
			if (this.isStatisticsAvailable() && (null!=this.getStatistics(false)))
			{
				ApolloStatistics statistics=this.getStatistics(false);
				statistics.setDirty(true);
				statistics.save();
			}
		}
		catch (Exception ignored)
		{
		}
		this.opened=false;
	}

	@Override
	public boolean isOpened()
	{
		return this.opened;
	}

	@Override
	public boolean create(Class<? extends DataVertex> store, DataVertex dataVertex, int attempt)
		throws ApplicationException
	{
		return this.getIndexStore().create(store, dataVertex, null);
	}

	@Override
	public Object execute(String statement)
	{
		// Only used in OrientDb.
		throw new NotImplementedException("Not Implemented");
	}

	@Override
	public boolean execute(IOperation operation)
		throws Exception
	{
		return this.getIndexStore().execute(operation);
	}

	@Override
	public <T extends DataVertex> T getObjectById(Class<? extends DataVertex> store, Class<? extends DataVertex>
		partitionType, Object dataStoreId, boolean ignoreCache)
	{
		return this.getObjectById(store, partitionType, dataStoreId, ignoreCache, 0);
	}

	@Override
	public <T extends DataVertex> T getObjectById(Class<? extends DataVertex> store, Class<? extends DataVertex>
		partitionType, Object dataStoreId)
	{
		return this.getObjectById(store, partitionType, dataStoreId, false);
	}

	@Override
	public boolean update(DataVertex dataVertex, int attempt)
		throws ApplicationException
	{
		boolean result=this.getIndexStore().update(dataVertex.getClass(), dataVertex);

		if (result)
		{
			this.cachePut(dataVertex);
		}
		return result;
	}

	private <T extends DataVertex>
	void cachePut(T vertex)
	{
		if (null!=Environment.getInstance().getCache())
		{
			try
			{
				Environment.getInstance().getCache().put(vertex);
			}
			catch (Exception ignored)
			{
			}
		}
	}

	@Override
	public boolean delete(Class<? extends DataVertex> store, DataVertex vertex, int attempts)
	{
		boolean result=false;
		if ((null!=vertex) && (null!=vertex.fetchId().getValue()) && vertex.hasId())
		{
			try
			{
				Environment.getInstance().getCache().remove(store, vertex.getClass(), vertex);
			}
			catch (Exception ignored)
			{
			}
			result=this.getIndexStore().delete(store, vertex);
		}
		return result;
	}

	@Override
	public String formatDataStoreId(Object dataStoreId)
	{
		String result=null;
		if (dataStoreId instanceof String)
		{
			result=(String) dataStoreId;
			if (StringX.startsWith(result, "/domains"))
			{
				result=StringX.getLast(result, "/");
			}
		}
		return result;
	}

	@Override
	public JsonData report(JsonData options)
	{
		return this.getIndexStore().report(options);
	}

	@Override
	public Object getVertexId(DataVertex vertex)
	{
		return (null!=vertex.getVertexData()) ? vertex.getVertexData().getString(IDataStore.DATA_STORE_ID) : null;
	}

	@Override
	public void removeVertexId(JsonData dso)
	{
		// Do nothing here.
	}

	@Override
	public void updateSearchStatus(Duration duration)
	{
		// TODO: future feature.
	}

	@Override
	public synchronized void waitForConnection()
	{
		EsIndexStore.getInstance().waitForConnection();
	}

	@Override
	public boolean verifyConnection()
	{
		boolean result=EsIndexStore.getInstance().verifyConnection();
		this.setOffline(!result);
		return result;
	}

	@Override
	public void commitAll()
	{
		// will not be implement here
	}

	@Override
	public Integer count(Class<? extends DataVertex> cls)
	{
		BaseQueryBuilder qb=this.getIndexStore().getQueryBuilder(cls, cls, 0, 0);
		qb.setApi(BaseQueryBuilder.API._count);
		qb.matchAll();
		return qb.execute().getCount();
	}

	@Override
	public boolean drop(Class<? extends DataVertex> cls)
	{
		return this.getIndexStore().drop(cls, cls);
	}

	@Override
	public JsonData toJson()
	{
		return EsDataStoreStatus.getInstance().isOpened() ? EsDataStoreStatus.getInstance().toJson() : null;
	}

	@Override
	public boolean delete(Class<Organization> store, Class<Organization> partition, String dataStoreId)
	{
		return this.getIndexStore().delete(store, partition, dataStoreId);
	}

	@Override
	public Object getSchema(Class<? extends DataVertex> store)
	{
		return this.getIndexStore().getSchema(store);
	}

	@Override
	public synchronized IDataThreadPool getDataPool()
	{
		/*
		if (null==this.dataPool)
		{
			this.dataPool=DataStorePool.begin(this.config.getThreadPoolSize(EsDataStore.DEFAULT_THREAD_POOL_SIZE));
		}
		*/
		return null;
	}

	@Override
	public String getDataStoreIdKey()
	{
		return IDataStore.DATA_STORE_ID;
	}

	@Override
	public IIndexStore getIndexStore()
	{
		return Environment.getInstance().getIndexStore();
	}

	@Override
	public IQueryFactory getQueries()
	{
		return this.getIndexStore().getQueryFactory();
	}

	@Override
	public IOperation getOperation(IOperation.Type type, DataVertex vertex)
	{
		return new Operation(type, vertex);
	}

	@Override
	public IOperation getOperation(Class<? extends DataVertex> storeType, IOperation.Type type, DataVertex vertex)
	{
		return new Operation(storeType, type, vertex);
	}

	@Override
	public IOperation getOperation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partition, IOperation.Type type, BulkItems<? extends DataVertex> bulkItems)
	{
		return new Operation(storeType, partition, type, bulkItems);
	}

	@Override
	public IOperation getOperation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, IOperation.Type type, DataVertex vertex)
	{
		return new Operation(storeType, partitionType, type, vertex);
	}

	@Override
	public synchronized ApolloStatistics getStatistics(boolean start)
	{
		if (start && (null==this.statistics))
		{
			this.statistics=ApolloStatistics.Queries.get();
			if (null==this.statistics)
			{
				try
				{
					this.statistics=new ApolloStatistics();
					this.statistics.setDirty(true);
					this.statistics.save();
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
			this.setStatisticsAvailable(null!=this.statistics);
		}
		return this.statistics;
	}

	@Override
	public synchronized boolean isStatisticsAvailable()
	{
		return this.statisticsAvailable;
	}

	public synchronized void setStatisticsAvailable(boolean available)
	{
		this.statisticsAvailable=available;
	}

	@Override
	public boolean start(Object... params)
		throws Exception
	{
		for (Object param : params)
		{
			if (param instanceof EsConfiguration)
			{
				this.config=(EsConfiguration) param;
				EsDataStoreStatus.start();
				this.opened=true;
				break;
			}
		}
		return this.isOpened();
	}

	@Override
	public boolean restart()
	{
		return this.getIndexStore().restart();
	}

	@Override
	public boolean credentialsRequired()
	{
		return this.getIndexStore().credentialsRequired();
	}

	@Override
	public boolean isOffline()
	{
		return this.offline;
	}

	@Override
	public
	synchronized void setOffline(boolean offline)
	{
		this.offline=offline;
	}

	@Override
	public int getPort()
	{
		return this.getIndexStore().getPort();
	}

	private static Class<? extends DataVertex> getPartitionType(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex dataVertex)
	{
		Class<? extends DataVertex> result=partitionType;
		if (ClassX.isKindOf(store, Edge.class))
		{
			Edge edge=(Edge) dataVertex;
			Class cls=edge.fetchEndpointFrom().getValue().getRelatedClass();
			if (ClassX.isKindOf(cls, DataVertex.class))
			{
				//noinspection unchecked
				result=(Class<? extends DataVertex>) cls;
			}
		}
		return result;
	}

	public <T extends DataVertex> T getObjectById(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, Object dataStoreId, boolean ignoreCache, int retries)
	{
		Class<? extends DataVertex> partition=partitionType;
		if (null==partition)
		{
			partition=store;
		}

		// We do not store edges as there data comes back in every related query, but if we do the below method may prevent edges to be returned from cache.
		// As the store and partition may not match what is actually used as a key in the cache.
		@SuppressWarnings("NestedConditionalExpression")
		T result=((null!=store) && !ignoreCache) ? this.cacheGet(store, partition, dataStoreId, ignoreCache) : null;

		if ((null==result) && (null!=dataStoreId))
		{
			try
			{
				result=VertexFactory.getInstance().getById(store, partition, (String) dataStoreId);

				if (null!=result)
				{
					this.cachePut(result);
				}
			}
			catch (Exception ex)
			{
				int tries=retries+1;
				Environment.getInstance().getReportHandler().info("Could not fined vertex: store: %s partition: %s, id: %s ignore cache: %s retries: %d",
					(null==store) ? "unknown store" : store.getName(),
					(null==partition) ? "unknown partition" : partition.getName(),
					dataStoreId.toString(),
					ignoreCache,
					tries
				);
				if ((retries<3))
				{
					Environment.getInstance().getIndexStore().getEngine().makeAvailable(store);
					try
					{
						Thread.sleep(EsDataStore.RETRY_MAX);
					}
					catch (Exception ignored)
					{
					}
					result=this.getObjectById(store, partition, dataStoreId, ignoreCache, tries);
				}
			}
		}
		return result;
	}

	public <T extends DataVertex>
	T cacheGet(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, Object dataStoreId, boolean ignoreCache)
	{
		T result=null;
		if (!ignoreCache && (null!=dataStoreId))
		{
			try
			{
				result=Environment.getInstance().getCache().get(store, partitionType, dataStoreId.toString());
			}
			catch (ApplicationException e)
			{
				Environment.getInstance().getReportHandler().severe(e);
			}
		}
		return result;
	}

	private Class<? extends DataVertex> getFinalClass(JsonData item, Class<? extends DataVertex> cls)
	{
		Class<? extends DataVertex> result=cls;
		String vertexType=item.getString("vertexType");
		if (!StringX.isBlank(vertexType))
		{
			result=Environment.getInstance().getApolloVertexType(vertexType);
		}
		return result;
	}

	// Getters and setters
	protected static EsDataStore getInstance()
	{
		return EsDataStore.INSTANCE;
	}
}
