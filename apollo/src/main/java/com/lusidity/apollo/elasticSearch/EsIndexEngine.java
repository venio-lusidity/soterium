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

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.lusidity.Environment;
import com.lusidity.apollo.common.Operation;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.domains.system.apollo.ApolloStatistics;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.internet.http.HttpClientXPool;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.interfaces.IIndexEngine;
import org.apache.http.HttpHost;
import org.joda.time.DateTime;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({
	"RedundantFieldInitialization",
	"ThrowCaughtLocally"
})
public class EsIndexEngine implements IIndexEngine, Closeable
{
	protected static String PROTOCOL=null;
	private static HttpClientXPool HTTP_CLIENT=null;
	private static Map<String, Collection<String>> CACHE=new HashMap<>();
	private static Map<Class<? extends DataVertex>, DateTime> transactions=new HashMap<>();
	private final IndexConfiguration config;
	private boolean dataStoreEnabled=false;
	private String version=null;

	// Constructors
	public EsIndexEngine(IndexConfiguration config)
		throws Exception
	{
		super();
		this.config=config;
		this.load();
	}

	private void load()
		throws Exception
	{
		if ((null!=this.config.getConfig()) && this.config.getConfig().isOpened())
		{
			if ((this.config.getConfig().getThreadPoolSize(EsIndexStore.DEFAULT_THREAD_POOL_SIZE)>0) && (null==EsIndexEngine.getHttpClient()))
			{
				this.version=this.config.getConfig().getVersion();
				EsIndexEngine.HTTP_CLIENT=new HttpClientXPool(this.config.getConfig().getThreadPoolSize(EsIndexStore.DEFAULT_THREAD_POOL_SIZE),
					this.config.getConfig().getThreadsProcessed(),
					new HttpHost(this.config.getConfig().getHttpHost(),
						this.config.getConfig().getHttpPort()
					),
					Environment.getInstance().getKeyStoreManagerApollo()
				);

			}
			EsIndexEngine.PROTOCOL=String.valueOf(this.config.getConfig().getProtocolString());
		}
		this.dataStoreEnabled=((null!=Environment.getInstance().getDataStore())
		                       && (Environment.getInstance().getDataStore() instanceof EsDataStore));
	}

	public static HttpClientXPool getHttpClient()
	{
		return EsIndexEngine.HTTP_CLIENT;
	}

	// Overrides
	@Override
	public void close()
		throws IOException
	{
		try
		{

			IDataStore ds=Environment.getInstance().getDataStore();
			if ((null!=ds) && ds.isOpened() && ds.isStatisticsAvailable() && (null!=ds.getStatistics(false)))
			{
				ds.getStatistics(false).save();
			}
		}
		catch (Exception ignored)
		{
		}
		if (null!=EsIndexEngine.getHttpClient())
		{
			EsIndexEngine.getHttpClient().close();
		}
	}

	// Methods
	public static synchronized void setTransaction(IOperation operation)
	{
		Operation op=(Operation) operation;

		if (EsIndexStore.getInstance().getConfig().isTransactionCheckEnabled())
		{
			if (operation.getType()==IOperation.Type.none)
			{
				EsIndexEngine.transactions.remove(op.getStoreType());
			}
			else
			{
				EsIndexEngine.transactions.put(operation.getStoreType(), DateTime.now());
			}
		}
	}

	public static synchronized DateTime getLastTransaction(Class<? extends DataVertex> store)
	{
		return EsIndexEngine.transactions.get(store);
	}

	public boolean delete(String store, String partitionType, String indexId)
	{
		boolean result=false;
		try
		{
			EsActionDelete task=new EsActionDelete(Operation.createEmpty());
			result=task.execute(store, partitionType, indexId);
			if (result)
			{
				if (EsDataStore.getInstance().isStatisticsAvailable())
				{
					EsDataStore.getInstance().getStatistics(false).fetchDeleted().getValue().increment();
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	public boolean dropIndex(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType)
	{
		boolean result=true;
		try
		{
			String indexName=ClassHelper.getIndexKey(store);
			//String partitionTypeName = (null != partitionType) ? ClassHelper.getIndexKey(partitionType) : null;
			URL url=this.getIndexUrl(indexName);
			JsonData response=this.getResponse(url, HttpClientX.Methods.DELETE, null);
			result=this.isValidResponse(response);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		if (result)
		{
			EsIndexEngine.getCache().remove(store.getName());
		}
		return result;
	}

	@Override
	public URL getIndexUrl(Object... parts)
	{
		return EsIndexBuilder.getIndexUrl(parts);
	}

	protected static Map<String, Collection<String>> getCache()
	{
		return EsIndexEngine.CACHE;
	}

	@Override
	public boolean execute(IOperation operation)
		throws Exception
	{
		EsIndexBuilder.exists(operation, this);
		switch (operation.getType())
		{
			case create:
				EsActionInsert.execute(operation);
				if (operation.isSuccessful())
				{
					if (EsDataStore.getInstance().isStatisticsAvailable())
					{
						EsDataStore.getInstance().getStatistics(false).fetchCreated().getValue().increment();
					}
					Environment.getInstance().getCache().put(operation.getVertex());
				}
				break;
			case delete:
				EsActionDelete.execute(operation);
				if (operation.isSuccessful())
				{
					if (EsDataStore.getInstance().isStatisticsAvailable())
					{
						EsDataStore.getInstance().getStatistics(false).fetchDeleted().getValue().increment();
					}
					Environment.getInstance().getCache().remove(operation.getStoreType(), operation.getPartitionType(), operation.getVertex());
				}
				break;
			case update:
				EsActionUpdate.execute(operation);
				if (operation.isSuccessful())
				{
					if (EsDataStore.getInstance().isStatisticsAvailable())
					{
						EsDataStore.getInstance().getStatistics(false).fetchUpdated().getValue().increment();
					}
					if (null!=operation.getVertex())
					{
						Environment.getInstance().getCache().put(operation.getVertex());
					}
				}
				break;
			case none:
			case query:
			default:
				throw new ApplicationException("Unsupported operation, %s.", operation.getType());
		}
		return operation.isSuccessful();
	}

	@Override
	public boolean create(Class<? extends DataVertex> store, DataVertex vertex, Object dataStoreId, int attempt)
	{
		Operation operation=new Operation(store, IOperation.Type.create, vertex);
		boolean result=false;
		try
		{
			result=this.execute(operation);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	@Override
	public boolean delete(Class<? extends DataVertex> store, DataVertex vertex)
	{
		return false;
	}

	@Override
	public boolean makeAvailable(Class<? extends DataVertex> store)
	{
		boolean result=false;
		try
		{
			URL refreshUrl=new URL(String.format("%s/_refresh",
				this.getIndexUrl(store).toString()
			));
			JsonData response=this.getResponse(refreshUrl, HttpClientX.Methods.GET, null);
			result=this.isValidResponse(response);
			if (!result)
			{
				Environment.getInstance().getReportHandler().warning("Index store for %s might not be ready.", store.getSimpleName());
			}
		}
		catch (Exception ignored)
		{
			Environment.getInstance().getReportHandler().warning("Index store for %s might not be ready.", store.getSimpleName());
		}
		return result;
	}

	@Override
	public boolean flush(Class<? extends DataVertex> store)
	{
		boolean result=false;
		try
		{
			URL refreshUrl=new URL(String.format("%s/_flush",
				this.getIndexUrl(store).toString()
			));
			JsonData response=this.getResponse(refreshUrl, HttpClientX.Methods.POST, null);
			result=this.isValidResponse(response);
			if (!result)
			{
				Environment.getInstance().getReportHandler().warning("Index store for %s might not be ready.", store.getSimpleName());
			}

		}
		catch (Exception ignored)
		{
			Environment.getInstance().getReportHandler().warning("Index store for %s might not be ready.", store.getSimpleName());
		}
		return result;
	}

	@Override
	public boolean update(Class<? extends DataVertex> store, DataVertex vertex, int attempt)
	{
		return false;
	}

	@Override
	public boolean isValidResponse(JsonData response)
	{
		return HttpClientX.isValidResponse(response);
	}

	@Override
	public JsonData getResponse(URL url, HttpClientX.Methods method, Object content, String... headers)
	{
		JsonData result=null;
		try
		{

			if (this.config.getConfig().getThreadPoolSize(EsIndexStore.DEFAULT_THREAD_POOL_SIZE)>0)
			{
				result=EsIndexEngine.getHttpClient().getResponse(url, method, (null!=content) ? content.toString() : null, Environment.DEFAULT_HOST, headers);
			}
			else
			{
				HttpContent c=null;
				if (null!=content)
				{
					c=new ByteArrayContent("application/json", content.toString().getBytes());
				}
				result=HttpClientX.getResponse(url, method, c, Environment.DEFAULT_HOST, headers);
				if ((null!=result) && !result.isValid())
				{
					Environment.getInstance().getReportHandler().severe(result.getString("http_response_message"));
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
	public String getTypeName(Class<? extends DataVertex> cls)
	{
		return EsIndexBuilder.getTypeName(cls);
	}

	@Override
	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();
		if (EsDataStore.getInstance().isStatisticsAvailable())
		{
			result=EsDataStore.getInstance().getStatistics(false).toJson(false);
		}
		else
		{
			result=(new ApolloStatistics()).toJson(false);
			result.update("available", false);
		}
		return result;
	}

	@Override
	public boolean isDataStoreEnabled()
	{
		return this.dataStoreEnabled;
	}

	@Override
	public URL getServerUrl()
	{
		return EsIndexBuilder.getServerUrl();
	}

	@SuppressWarnings("unused")
	public boolean makePersistent()
	{
		URL url=this.getIndexUrl("_cluster", "settings");
		JsonData content=JsonData.createObject().put("persistent", JsonData.createObject().put("discovery.zen.minimum_master_nodes", 1));
		JsonData response=this.getResponse(url, HttpClientX.Methods.PUT, content);
		return this.isValidResponse(response);
	}

	@SuppressWarnings("UnusedDeclaration")
	protected boolean exists(URL url)
	{
		JsonData response=this.getResponse(url, HttpClientX.Methods.GET, null);
		boolean result=this.isValidResponse(response);
		if (!result && response.hasKey("http_response_message"))
		{
			String message=response.getString("http_response_message");
			result=!StringX.isBlank(message) && StringX.containsIgnoreCase(message, "already exists");
		}
		return result;
	}

	@SuppressWarnings("UnusedDeclaration")
	/**
	 * Can be used to shutdown a class.
	 */
	private boolean shutdown(Class<? extends DataVertex> store)
	{
		boolean result=false;
		try
		{
			// TODO: this is not thread safe and should be safely handled.
			URL url=this.getIndexUrl(store, "_close");
			JsonData response=this.getResponse(url, HttpClientX.Methods.POST, null);
			result=this.isValidResponse(response);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	@SuppressWarnings("UnusedDeclaration")
	/**
	 * Can be used to restart a class.
	 */
	private boolean restart(Class<? extends DataVertex> store)
	{
		boolean result=false;
		try
		{

			// TODO: this is not thread safe and should be safely handled.
			URL url=this.getIndexUrl(store, "_open");
			JsonData response=this.getResponse(url, HttpClientX.Methods.POST, null);
			result=this.isValidResponse(response);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	// Getters and setters
	@SuppressWarnings("UnusedDeclaration")
	public URL getBulkURL()
	{
		return HttpClientX.createURL(String.format("%s://%s:%d/_bulk", EsIndexEngine.PROTOCOL,
			StringX.stripStart(this.config.getConfig().getHttpHost(), "/")
			, this.config.getConfig().getHttpPort()
		));
	}

	public String getVersion()
	{
		return this.version;
	}
}
