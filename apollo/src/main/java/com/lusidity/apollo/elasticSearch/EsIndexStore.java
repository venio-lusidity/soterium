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

import com.google.api.client.http.HttpContent;
import com.lusidity.Environment;
import com.lusidity.LogHandlerCallback;
import com.lusidity.apollo.common.Operation;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.BaseQueryDateFormat;
import com.lusidity.data.interfaces.data.query.IQueryFactory;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.internet.http.HttpClientXPool;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.interfaces.IIndexEngine;
import com.lusidity.index.interfaces.IIndexStore;
import com.lusidity.index.interfaces.IReIndexer;
import org.apache.http.HttpHost;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

public class EsIndexStore implements IIndexStore
{
	// Fields
	public static final int DEFAULT_THREAD_POOL_SIZE=50;
	private static EsIndexStore INSTANCE=null;
	private boolean opened=false;
	private EsConfiguration config=null;
	private EsIndexEngine engine=null;
	private boolean offline=false;
	private boolean clientLogged=false;
	private boolean restLogged=false;

	// Constructors
	public EsIndexStore()
	{
		super();
		EsIndexStore.INSTANCE=this;
	}

	// Overrides
	@Override
	public boolean start(Object... params)
		throws Exception
	{
		ReportHandler rh=Environment.getInstance().getReportHandler();
		for (Object param : params)
		{
			if (param instanceof EsConfiguration)
			{
				rh.info("EsConfiguration param found");
				this.config=(EsConfiguration) param;
				rh.info("EsConfiguration json: %s", this.config.getData().toString());
				rh.info("EsIndexStore awaiting connection");
				this.initSSL();
				this.waitForConnection();
				rh.info("EsIndexStore connection aquired");
				if (Environment.getInstance().getConfig().clearStores())
				{
					rh.fine("Clearing index store");
					boolean deleted=this.clearAllIndexes();
					if (!deleted)
					{
						rh.severe("The index stores were not cleared.");
					}
					else{
						this.initSSL();
					}
				}
				if (!this.verifyREST())
				{
					rh.severe("Cannot connect using REST.");
				}
				else if (this.getConfig().clientEnabled() && !this.verifyClient())
				{
					rh.severe("Cannot connect using Client.");
				}
				else
				{
					rh.info("EsIndexStore starting");
					EsIndexStoreStatus.start();
					this.opened=true;
					if (this.config.isEnabled())
					{
						rh.info(
							"\nEnsure to install the icu plugin.\n");
						rh.info("EsIndexStore building indexes");
						this.buildIndexes(0);
					}
				}
				break;
			}
		}

		rh.info("EsIndexStore is %s", this.isOpened() ? "opened" : "closed");
		return this.isOpened();
	}

	private void initSSL()
		throws Exception
	{
		if(this.config.initializeSSl()){
			EsCommandLine esCmd = new EsCommandLine(EsCommandLine.Commands.initialize);
			EsCommandLine.CommandItem cmdItem = esCmd.execute();
			if(!cmdItem.isSuccess()){
				throw new ApplicationException("ElasticSearch SearchGuard is configured properly.");
			}
		}
	}

	@Override
	public boolean restart()
	{
		return false;
	}

	@Override
	public boolean credentialsRequired()
	{
		return false;
	}

	@Override
	public boolean isOffline()
	{
		return this.offline;
	}

	@Override
	public void setOffline(boolean offline)
	{
		if (!this.offline && offline)
		{
			LogHandlerCallback callback=new LogHandlerCallback();
			callback.processed(Level.SEVERE, "The ElasticSearch has gone offline. Reported by %s.", Environment.getInstance().getConfig().getBaseServerUrl());
		}
		else if (this.offline && !offline)
		{
			LogHandlerCallback callback=new LogHandlerCallback();
			callback.processed(Level.SEVERE, "The ElasticSearch has bome back online. Reported by %s.", Environment.getInstance().getConfig().getBaseServerUrl());
		}
		this.offline=offline;
	}

	@Override
	public int getPort()
	{
		return 0;
	}

	@Override
	public boolean isOpened()
	{
		return (null!=this.config) && this.config.isEnabled() && this.opened;
	}

	@Override
	public void close()
	{
		// this index store is connected via REST and does not require being closed.
		this.opened=false;
		try
		{
			this.engine.close();
		}
		catch (Exception ignored)
		{
		}

		if (null!=EsClientTransport.getInstance())
		{
			EsClientTransport.getInstance().close();
		}
		EsIndexStore.INSTANCE=null;
	}

	@Override
	public boolean create(Class<? extends DataVertex> store, DataVertex dataVertex, Object dataStoreId)
	{
		boolean result=false;
		if (this.isOpened())
		{
			result=this.engine.create(store, dataVertex, dataStoreId, 0);
		}
		return result;
	}

	@Override
	public boolean delete(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, String indexId)
	{
		JsonData response=null;
		try
		{
			String name=ClassHelper.getIndexKey(store);
			String type=ClassHelper.getIndexKey(partition);
			URL url=new URL(String.format("%s/%s/%s", this.getIndexUrl(name).toString(), type, indexId));
			response=this.getResponse(url, HttpClientX.Methods.DELETE, null);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return this.isValidResponse(response);
	}

	private URL getIndexUrl(String type)
	{
		return HttpClientX.createURL(String.format("%s://%s:%d/%s/",
			this.getConfig().getProtocol().getName().toLowerCase(),
			this.config.getHttpHost()
			, this.config.getHttpPort(), type
		));
	}

	@SuppressWarnings("SameParameterValue")
	private JsonData getResponse(URL url, HttpClientX.Methods method, HttpContent content, String... headers)
	{
		JsonData result=null;

		try
		{
			result=HttpClientX.getResponse(url, method, content, Environment.DEFAULT_HOST, headers);

			if ((null!=result) && !result.isValid())
			{
				Environment.getInstance().getReportHandler().severe(result.getString("http_response_message"));
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return result;
	}

	protected EsConfiguration getConfig()
	{
		return this.config;
	}

	@Override
	public boolean delete(Class<? extends DataVertex> store, DataVertex dataVertex)
	{
		boolean result=this.config.isOpened();
		if (result)
		{
			try
			{
				Operation operation=new Operation(store, IOperation.Type.delete, dataVertex);
				result=this.engine.execute(operation);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
				result=false;
			}
		}
		return result;
	}

	@Override
	public boolean delete(String store, String partition, String indexId)
	{
		return this.engine.delete(store, partition, indexId);
	}

	@Override
	public boolean update(Class<? extends DataVertex> store, DataVertex dataVertex)
	{
		return this.engine.update(store, dataVertex, 0);
	}

	@Override
	public JsonData toJson()
	{
		return EsIndexStoreStatus.getInstance().toJson();
	}

	@Override
	public JsonData report(JsonData options)
	{
		EsActionReport report=new EsActionReport(options);
		return report.execute();
	}

	@Override
	public void makeAvailable(Class<? extends DataVertex> store, boolean enforce)
	{
		DateTime now=DateTime.now().minusSeconds(2);
		DateTime last=EsIndexEngine.getLastTransaction(store);
		if (null!=last)
		{
			Operation operation=new Operation(store, IOperation.Type.none);
			EsIndexEngine.setTransaction(operation);
		}
		if (enforce || ((null!=last) && last.isAfter(now)))
		{
			this.getEngine().makeAvailable(store);
		}
	}

	@Override
	public boolean isValidResponse(JsonData response)
	{
		return HttpClientX.isValidResponse(response);
	}

	@Override
	public BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, int start, int limit)
	{
		return new EsQueryBuilder(indexStore, indexType, start, limit);
	}

	@Override
	public BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, BaseQueryBuilder.API api, int start, int limit)
	{
		return new EsQueryBuilder(indexStore, indexType, api, start, limit);
	}

	@Override
	public BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, JsonData query, BaseQueryBuilder.API api, int start, int limit)
	{
		BaseQueryBuilder result=new EsQueryBuilder(indexStore, indexType, api, start, limit);
		result.setQuery(query);
		return result;
	}

	@Override
	public BaseQueryDateFormat getQueryDateFormat(DateTime dateTime)
	{
		return new EsQueryDateFormat(dateTime);
	}

	@Override
	public BaseQueryDateFormat getQueryDateFormat(DateTime from, DateTime to)
	{
		return new EsQueryDateFormat(from, to);
	}

	@Override
	public IReIndexer getReIndexer(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, JsonData map)
		throws ApplicationException
	{
		return new EsReIndexer(store, partition, map, this.engine);
	}

	@Override
	public boolean drop(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType)
	{
		return this.engine.dropIndex(storeType, partitionType);
	}

	@Override
	public boolean exists(Class<? extends DataVertex> StoreType, Class<? extends DataVertex> partitionType)
	{
		return this.engine.exists(EsIndexBuilder.getIndexUrl(StoreType, partitionType));
	}

	@Override
	public synchronized void waitForConnection()
	{
		// Should only report offline on initial call and every 60 seconds.
		while (!this.verifyConnection())
		{
			try
			{
				//noinspection BusyWait,SleepWhileHoldingLock,CallToNativeMethodWhileLocked
				Thread.sleep((10*1000));
			}
			catch (Exception ignored)
			{
			}
		}
	}

	@Override
	public boolean verifyConnection()
	{
		boolean result=this.isConnected();
		this.setOffline(!result);
		return result;
	}

	@Override
	public void reset(Class<? extends DataVertex> store)
	{

	}

	@Override
	public boolean execute(IOperation operation)
		throws Exception
	{
		return this.engine.execute(operation);
	}

	@Override
	public BaseQueryBuilder getQueryBuilder(String store, String partition, int start, int limit)
	{
		return this.getQueryBuilder(BaseDomain.getDomainType(store), BaseDomain.getDomainType(partition), start, limit);
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
	public IOperation getOperation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, IOperation.Type type, DataVertex vertex)
	{
		return new Operation(storeType, partitionType, type, vertex);
	}

	@Override
	public Object getSchema(Class<? extends DataVertex> store)
	{
		JsonData result=null;
		HttpClientXPool pool=null;
		try
		{

			URL url=EsIndexBuilder.getIndexUrl(store);
			url=new URL(String.format("%s/%s", url.toString(), "_mapping"));

			//noinspection IOResourceOpenedButNotSafelyClosed
			pool=new HttpClientXPool(10, 20,
				new HttpHost(this.config.getHttpHost(), this.config.getHttpPort()), Environment.getInstance().getKeyStoreManagerApollo()
			);
			result=pool.getResponse(url,
				HttpClientX.Methods.GET,
				Environment.getInstance().getConfig().getReferer(), null
			);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		finally
		{
			if (null!=pool)
			{
				try
				{
					pool.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}
		return result;
	}

	@Override
	public IIndexEngine getEngine()
	{
		return this.engine;
	}

	@Override
	public IQueryFactory getQueryFactory()
	{
		return new EsQueries();
	}

	@Override
	public boolean isConnected()
	{
		return (this.verifyREST() && this.verifyClient());
	}

	@Override
	public boolean isBatchMode()
	{
		return false;
	}

	@SuppressWarnings("UnusedParameters")
	public boolean exists(String index, String partition)
	{
		return false;
	}

	private boolean verifyClient()
	{
		boolean result=false;
		try
		{
			EsClientTransport client=EsClientTransport.getInstance();
			result=client.isConnected();
			client.close();
			if (!result && !this.clientLogged)
			{
				this.setClientLogged(true);
				Environment.getInstance().getReportHandler().severe("Cannot connect to client services at %s", EsClientTransport.getInstance().getConfig().getClientPort());
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	private boolean verifyREST()
	{
		boolean result=false;
		HttpClientXPool pool=null;
		try
		{
			URI uri=URI.create(this.getIndexUrl("_cat/health?v").toString());

			//noinspection IOResourceOpenedButNotSafelyClosed
			pool=new HttpClientXPool(10, 20,
				new HttpHost(this.config.getHttpHost(), this.config.getHttpPort()), Environment.getInstance().getKeyStoreManagerApollo()
			);
			String response=pool.getResponse(uri.toURL(),
				HttpClientX.Methods.GET,
				Environment.getInstance().getConfig().getReferer(), null
			)
			                    .getString("content");
			result=(!StringX.isBlank(response) && StringX.contains(response, String.format("%s green", this.config
				.getClusterName())));
			if (!result && !this.restLogged)
			{
				this.setRestLogged(true);
				Environment.getInstance().getReportHandler().severe("Cannot connect to http services at %s", uri.toString());
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		finally
		{
			if (null!=pool)
			{
				try
				{
					pool.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}
		return result;
	}

	private boolean clearAllIndexes()
	{
		boolean result=false;
		try
		{
			String param=this.getConfig().isHttps() ? "*,-searchguard*" : "*";
			JsonData response=this.getResponse(this.getIndexUrl(param), HttpClientX.Methods.DELETE, null);
			result=HttpClientX.isValidResponse(response);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	@SuppressWarnings("SameParameterValue")
	private void buildIndexes(@SuppressWarnings("unused") int retry)
		throws ApplicationException
	{
		if ((null!=this.config.getLogConfigDir()) && this.config.getLogConfigDir().isDirectory())
		{
			EsIndexEngine.getCache().clear();
			File file=new File(this.config.getLogConfigDir(), "vertices.json");
			if (file.isFile())
			{
				this.getIndexEngine(file);
			}
		}
		else
		{
			Environment.getInstance().getReportHandler().severe("The index directory doesn't exist.");
			throw new ApplicationException("The index directory doesn't exist.");
		}
	}

	private void getIndexEngine(File file)
	{
		try
		{
			IndexConfiguration indexConfiguration=new IndexConfiguration(file, this.config);
			this.engine=indexConfiguration.start();
			if (null==this.engine)
			{
				Environment.getInstance().getReportHandler().severe("Failed to start index %s.", file.getName());
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	// Getters and setters
	protected static EsIndexStore getInstance()
	{
		return EsIndexStore.INSTANCE;
	}

	private synchronized void setClientLogged(boolean logged)
	{
		this.clientLogged=logged;
	}

	private synchronized void setRestLogged(boolean logged)
	{
		this.restLogged=logged;
	}
}
