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
import com.lusidity.apollo.elasticSearch.tasks.RequestDeleteTask;
import com.lusidity.apollo.elasticSearch.tasks.ResponseHandler;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.engine.DocumentMissingException;

import java.net.URI;
import java.net.URL;
import java.security.InvalidParameterException;

@SuppressWarnings("Duplicates")
public class EsActionDelete
{
	private IOperation op=null;

	// Constructors
	public EsActionDelete(IOperation operation)
	{
		super();
		this.op=operation;
	}

	// Methods
	public static void execute(IOperation operation)
	{
		EsActionDelete action=new EsActionDelete(operation);
		operation.setSuccessful(action.execute());
	}

	public boolean execute()
	{
		String indexName=ClassHelper.getIndexKey(this.op.getStoreType());
		String partitionName=ClassHelper.getIndexKey(this.op.getPartitionType());
		if (null==this.op.getVertex().getIndexId())
		{
			this.retrieveIndexId();
		}
		return this.execute(indexName, partitionName, this.op.getVertex().getIndexId().toString());
	}

	public boolean execute(String indexName, String partitionName, String indexId)
	{
		return this.client(indexName, partitionName, indexId);
	}

	public boolean client(String indexName, String partitionName, String indexId)
	{
		boolean result=false;

		try
		{
			if (StringX.isBlank(indexId))
			{
				//noinspection ThrowCaughtLocally
				throw new InvalidParameterException("The index id cannot be null.");
			}

			if (Environment.getInstance().getDataStore().isOffline())
			{
				Environment.getInstance().getDataStore().waitForConnection();
			}

			BaseClient client;
			if (EsIndexStore.getInstance().getConfig().clientEnabled())
			{
				client=EsClientNode.getInstance();
			}
			else
			{
				client=EsClientTransport.getInstance();
			}

			if (null!=client)
			{
				DeleteRequestBuilder builder=client.prepareDelete().setIndex(indexName).setType(partitionName).setId(indexId);
				result=this.sync(builder, true);
			}
		}
		catch (DocumentMissingException dme)
		{
			Environment.getInstance().getReportHandler().info(dme);
			result=true;
		}
		catch (Exception ex)
		{
			if (!StringX.isBlank(indexId) && !Environment.getInstance().getDataStore().isOffline())
			{
				if (ClassX.isKindOf(ex, NoNodeAvailableException.class))
				{
					if ((this.op.getRetries()<Environment.getInstance().getConfig().getOfflineRetries()))
					{
						try
						{
							Thread.sleep(Environment.getInstance().getConfig().getOfflineWaitInterval());
						}
						catch (Exception ignored)
						{
						}
						this.op.incrementRetries();
						result=this.execute(indexName, partitionName, indexId);
					}
					else
					{
						Environment.getInstance().getDataStore().verifyConnection();
					}
				}
				else if (!ClassX.isKindOf(ex, IndexNotFoundException.class) && !ClassX.isKindOf(ex, org.elasticsearch.index.IndexNotFoundException.class))
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
			else
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	public void retrieveIndexId()
	{
		// We might be waiting on an index refresh before this is available so wait.
		boolean found=false;
		Stopwatch sw=new Stopwatch();
		sw.start();
		while (!found)
		{
			DataVertex result=Environment.getInstance().getDataStore().getObjectById(this.op.getStoreType(), this.op.getPartitionType(), this.op.getVertex().fetchId().getValue(), true);
			found=(null!=result) && (null!=result.getIndexId());
			if (found)
			{
				this.op.getVertex().setIndexId(result.getIndexId());
			}
			else
			{
				found=(sw.elapsed().getStandardSeconds()>10);
			}
		}
	}

	private boolean http(String indexName, String partitionName, String indexId)
	{
		boolean result=false;
		try
		{
			URI uri=URI.create(String.format("%s://%s:%d/%s/%s/%s",
				EsIndexStore.getInstance().getConfig().isHttps() ? "https" : "http",
				EsIndexStore.getInstance().getConfig().getHttpHost(),
				EsIndexStore.getInstance().getConfig().getHttpPort(),
				indexName,
				partitionName,
				indexId
			));
			URL url=uri.toURL();
			RequestDeleteTask task=new RequestDeleteTask(this.op.getVertex(), url);
			ResponseHandler handler=task.call();
			result=handler.isOk();
			if (result)
			{
				Environment.getInstance().getIndexStore().makeAvailable(this.op.getStoreType(), true);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	private boolean sync(DeleteRequestBuilder builder, boolean immediate)
	{
		if (immediate)
		{
			builder.setRefresh(true);
		}

		DeleteResponse response=builder.get();
		boolean result=response.isFound();
		this.op.setSuccessful(result);

		if (!immediate)
		{
			EsIndexEngine.setTransaction(this.op);
		}
		return result;
	}

	private boolean async(DeleteRequestBuilder builder)
	{
		builder.execute(new EsActionDeleteListener(this.op.getVertex()));
		this.op.setSuccessful(true);
		EsIndexEngine.setTransaction(this.op);
		return true;
	}
}
