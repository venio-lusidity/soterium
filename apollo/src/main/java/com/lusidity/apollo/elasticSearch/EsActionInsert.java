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
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.bulk.BulkItems;
import com.lusidity.data.interfaces.data.IDataThreadPool;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.data.interfaces.operations.IAction;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;

import java.io.InvalidObjectException;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class EsActionInsert implements IAction
{
	protected static int inserts=0;
	private final IOperation op;

	// Constructors
	public EsActionInsert(IOperation operation)
	{
		super();
		this.op=operation;
	}

	// Overrides
	@Override
	public boolean execute(BulkItems<? extends DataVertex> bulkItems)
		throws Exception
	{
		/*
		boolean result = false;

		try
		{
			if(Environment.getInstance().getDataStore().isOffline()){
				Environment.getInstance().getDataStore().waitForConnection();
			}

			if(this.op.getRetries()>0){
				DataVertex dataVertex = Environment.getInstance().getDataStore().getObjectById(this.op.getStoreType(), this.op.getPartitionType(), this.op.getDataStoreId());
				if(null!=dataVertex){
					this.op.getVertex().setIndexId(dataVertex.getIndexId());
					result = true;
				}
			}

			if(!result)
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

				if (null!=client)
				{
					String indexName = ClassHelper.getIndexKey(this.op.getStoreType());
					String partitionName = ClassHelper.getIndexKey(this.op.getPartitionType());

					BulkRequestBuilder builder = client.prepareBulk();
					for(DataVertex vertex: bulkItems){
						this.fixId(this.op.getStoreType(), vertex);
						JsonData indexValue = vertex.toJson(true);
						IndexRequestBuilder irb = client.prepareIndex(indexName, partitionName)
						                                    .setSource(indexValue.toString());
						builder.add(irb);
					}

					BulkResponse response=builder.get();
					// figure out how to get the response status.
					if (result)
					{
						// todo: set the index id of each bulk request.
						Environment.getInstance().getDataStore().setOffline(false);
					}
				}
			}
		}
		catch (Exception ex){
			this.error(ex);
		}

		return result;
		*/
		throw new NotImplementedException("Code commented out has not been tested.");
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

			if (this.op.getRetries()>0)
			{
				DataVertex dataVertex=Environment.getInstance().getDataStore().getObjectById(this.op.getStoreType(), this.op.getPartitionType(), this.op.getDataStoreId());
				if (null!=dataVertex)
				{
					this.op.getVertex().setIndexId(dataVertex.getIndexId());
					result=true;
				}
			}

			if (!result)
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

				if (null!=client)
				{
					String indexName=ClassHelper.getIndexKey(this.op.getStoreType());
					String partitionName=ClassHelper.getIndexKey(this.op.getPartitionType());

					this.fixId(this.op.getStoreType(), this.op.getVertex());

					JsonData indexValue=this.op.getVertex().toJson(true);

					if (null!=indexValue)
					{
						String value=indexValue.toString();
						IndexRequestBuilder builder=client.prepareIndex(indexName, partitionName).setSource(value);
						//result = this.async(builder);
						result=this.sync(builder, this.op.getVertex().isImmediate());
					}
				}
			}
		}
		catch (Exception ex)
		{
			this.error(ex);
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
		if ((null==operation.getVertex()) && ((null==operation.getBulkItems()) || operation.getBulkItems().isEmpty()))
		{
			throw new InvalidObjectException("The BulkItems are empty.");
		}
		else
		{
			EsActionInsert action=new EsActionInsert(operation);
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

	private void fixId(Class<? extends DataVertex> storeType, DataVertex vertex)
		throws ApplicationException
	{
		if ((null!=vertex) && vertex.hasId())
		{
			boolean result=Environment.getInstance().getIndexStore().delete(storeType, vertex);
			if (result)
			{
				// Set the index id to null as this will have changed.
				vertex.setIndexId(null);
			}
			else
			{
				throw new ApplicationException("The vertex, %s, could not be deleted and therefore can not be re-inserted into the datastore", vertex.getUri().toString());
			}
		}
		else if ((null!=vertex))
		{
			String id=StringX.removeNonAlphaNumericCharacters(UUID.randomUUID().toString());
			vertex.fetchId().setValue(id);
		}
	}

	private boolean async(IndexRequestBuilder builder)
	{
		//if (!this.maxed(builder))
		//{
		builder.execute(new EsActionInsertListener(this.op.getVertex()));
		this.op.setSuccessful(true);
		EsIndexEngine.setTransaction(this.op);
		//}
		return true;
	}

	private synchronized boolean maxed(IndexRequestBuilder builder)
	{
		boolean result=EsActionInsert.getInserts()==500;
		if (result)
		{
			this.sync(builder, true);
			Environment.getInstance().getIndexStore().makeAvailable(this.op.getStoreType(), true);
			EsActionInsert.reset();
		}
		else
		{
			EsActionInsert.increment();
		}
		return result;
	}

	public static int getInserts()
	{
		return EsActionInsert.inserts;
	}

	private boolean sync(IndexRequestBuilder builder, boolean immediate)
	{
		if (immediate)
		{
			builder.setRefresh(true);
		}

		IndexResponse response=builder.get();
		boolean result=response.isCreated();
		if (result)
		{
			this.op.getVertex().setIndexId(response.getId());
			this.op.setSuccessful(true);
			if (!immediate)
			{
				EsIndexEngine.setTransaction(this.op);
			}
			Environment.getInstance().getDataStore().setOffline(false);
		}
		return result;
	}

	private static synchronized void reset()
	{
		EsActionInsert.inserts=0;
	}

	private static synchronized void increment()
	{
		EsActionInsert.inserts++;
	}

	protected void error(Exception ex)
		throws Exception
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
}