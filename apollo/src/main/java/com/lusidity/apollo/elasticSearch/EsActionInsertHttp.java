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
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.client.transport.NoNodeAvailableException;

import java.io.InvalidObjectException;
import java.net.URL;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class EsActionInsertHttp implements IAction
{
	protected static int inserts=0;
	private final IOperation op;

	// Constructors
	public EsActionInsertHttp(IOperation operation)
	{
		super();
		this.op=operation;
	}

	// Overrides
	@Override
	public boolean execute(BulkItems<? extends DataVertex> bulkItems)
		throws Exception
	{
		throw new NotImplementedException("Bulk insert not implemented in HTTP.");
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
				String indexName=ClassHelper.getIndexKey(this.op.getStoreType());
				String partitionName=ClassHelper.getIndexKey(this.op.getPartitionType());

				URL url=Environment.getInstance().getIndexStore().getEngine().getIndexUrl(indexName, partitionName);
				JsonData content=this.op.getVertex().toJson(true);
				JsonData response=Environment.getInstance().getIndexStore().getEngine().getResponse(url, HttpClientX.Methods.POST, content);

				if (HttpClientX.isValidResponse(response))
				{
					result=true;
					this.op.getVertex().setIndexId(response.getString("_id"));
					this.op.setSuccessful(true);
					if (this.op.getVertex().isImmediate())
					{
						Environment.getInstance().getIndexStore().makeAvailable(this.op.getStoreType(), true);
					}
					if (!this.op.getVertex().isImmediate())
					{
						EsIndexEngine.setTransaction(this.op);
					}
				}
				Environment.getInstance().getDataStore().setOffline(false);
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
	public static int getInserts()
	{
		return EsActionInsertHttp.inserts;
	}

	public static void execute(IOperation operation)
		throws Exception
	{
		if (operation.getVertex().hasId())
		{
			throw new InvalidObjectException("The vertex cannot be created as it already has an ID.  It should have been updated.");
		}
		String id=StringX.removeNonAlphaNumericCharacters(UUID.randomUUID().toString());
		operation.getVertex().fetchId().setValue(id);

		EsActionInsertHttp action=new EsActionInsertHttp(operation);
		IDataThreadPool pool=Environment.getInstance().getDataStore().getDataPool();
		if (null!=pool)
		{
			pool.submit(action);
		}
		else
		{
			operation.setSuccessful(action.execute());
		}
	}

	private static synchronized void reset()
	{
		EsActionInsertHttp.inserts=0;
	}

	private static synchronized void increment()
	{
		EsActionInsertHttp.inserts++;
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
