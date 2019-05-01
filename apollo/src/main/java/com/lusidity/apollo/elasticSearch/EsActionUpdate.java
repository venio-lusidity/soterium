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
import com.lusidity.blockers.VertexBlocker;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.bulk.BulkItems;
import com.lusidity.data.interfaces.data.IDataThreadPool;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.data.interfaces.operations.IAction;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.Stopwatch;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.engine.DocumentMissingException;

import java.io.InvalidObjectException;

@SuppressWarnings("Duplicates")
public class EsActionUpdate implements IAction
{
	private static VertexBlocker blocker=new VertexBlocker();
	private final IOperation op;

	// Constructors
	public EsActionUpdate(IOperation operation)
	{
		this.op=operation;
	}

	// Overrides
	@SuppressWarnings("OverlyLongMethod")
	@Override
	public boolean execute(BulkItems<? extends DataVertex> bulkItems)
		throws Exception
	{
		boolean result=false;

		try
		{
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
				BulkRequestBuilder builder=client.prepareBulk();

				boolean process=false;
				for (DataVertex vertex : bulkItems)
				{
					// The UpdateRequest doesn't work in ElasticSearch 2.3 so use the bulk request for updates.
					Environment.getInstance().getCache().remove(bulkItems.getStore(), bulkItems.getPartition(), vertex);
					boolean added=this.add(builder, client, vertex, bulkItems.getStore(), bulkItems.getPartition());
					if (added)
					{
						process=true;
					}
				}
				if (process)
				{
					result=this.sync(builder, !EsIndexStore.getInstance().getConfig().isTransactionCheckEnabled());
				}
			}
		}
		catch (DocumentMissingException dme)
		{
			Environment.getInstance().getReportHandler().info(dme);
		}
		catch (Exception ex)
		{
			if (!Environment.getInstance().getDataStore().isOffline())
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
						this.execute();
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

	@Override
	public boolean execute()
		throws Exception
	{
		boolean result=false;

		try
		{
			if (Environment.getInstance().getDataStore().isOffline())
			{
				Environment.getInstance().getDataStore().waitForConnection();
			}

			EsActionUpdate.blocker.start(this.op.getVertex());

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
				// The UpdateRequest doesn't work in ElasticSearch 2.3 so use the bulk request for updates.
				Environment.getInstance().getCache().remove(this.op.getStoreType(), this.op.getPartitionType(), this.op.getVertex());

				BulkRequestBuilder builder=client.prepareBulk();

				boolean added=this.add(builder, client, this.op.getVertex(), this.op.getStoreType(), this.op.getPartitionType());
				if (added)
				{
					//result = this.async(builder);
					result=this.sync(builder, this.op.getVertex().isImmediate());
				}
			}
		}
		catch (DocumentMissingException dme)
		{
			Environment.getInstance().getReportHandler().info(dme);
		}
		catch (Exception ex)
		{
			EsActionUpdate.blocker.finished(this.op.getVertex());
			if (!Environment.getInstance().getDataStore().isOffline())
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
						this.execute();
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
		finally
		{
			EsActionUpdate.blocker.finished(this.op.getVertex());
		}

		return result;
	}

	@Override
	public IOperation getOperation()
	{
		return this.op;
	}

	// Methods
	public static void execute(IOperation operation)
		throws Exception
	{
		if (((null!=operation.getVertex()) && !operation.getVertex().hasId()))
		{
			throw new InvalidObjectException("The vertex cannot be updated as it does not have an ID.  It should have been created.");
		}
		else if ((null==operation.getVertex()) && ((null==operation.getBulkItems()) || operation.getBulkItems().isEmpty()))
		{
			throw new InvalidObjectException("The BulkItems are empty.");
		}
		else
		{
			EsActionUpdate action=new EsActionUpdate(operation);
			IDataThreadPool pool=Environment.getInstance().getDataStore().getDataPool();
			if (null!=pool)
			{
				pool.submit(action);
			}
			else
			{
				if (null!=operation.getVertex())
				{
					operation.setSuccessful(action.execute());
				}
				else if ((null!=operation.getBulkItems()) && !operation.getBulkItems().isEmpty())
				{
					BulkItems<? extends DataVertex> bulkItems=operation.getBulkItems();
					operation.setSuccessful(action.execute(bulkItems));
				}
				else
				{
					operation.setSuccessful(false);
				}
			}
		}
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

	private boolean add(BulkRequestBuilder builder, BaseClient client, DataVertex vertex, Class<? extends DataVertex> store, Class<? extends DataVertex> partition)
	{
		boolean result=true;
		try
		{
			String indexName=ClassHelper.getIndexKey(store);
			String partitionName=ClassHelper.getIndexKey(partition);
			if (null==vertex.getIndexId())
			{
				this.retrieveIndexId();
			}

			String indexId=vertex.getIndexId().toString();
			JsonData indexValue=vertex.toJson(true);

			UpdateRequestBuilder urb=client.prepareUpdate(indexName, partitionName, indexId).setDoc(indexValue.toString());
			builder.add(urb);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
			result=false;
		}
		return result;
	}

	private boolean sync(BulkRequestBuilder builder, boolean immediate)
	{
		boolean result=false;
		if (immediate)
		{
			builder.setRefresh(true);
		}
		if (builder.numberOfActions()>0)
		{
			ListenableActionFuture<BulkResponse> response=builder.execute();
			BulkResponse action=response.actionGet();
			result=!action.hasFailures();
			if (result)
			{
				if (!immediate)
				{
					EsIndexEngine.setTransaction(this.op);
				}
				Environment.getInstance().getDataStore().setOffline(false);
			}
			else
			{
				//process failures by iterating through each bulk response item
				Environment.getInstance().getReportHandler().severe(action.buildFailureMessage());
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	private boolean async(BulkRequestBuilder builder)
	{
		builder.execute(new EsActionUpdateListener(this.op.getVertex()));
		this.op.setSuccessful(true);
		EsIndexEngine.setTransaction(this.op);
		return true;
	}
}