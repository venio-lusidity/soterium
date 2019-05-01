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

package com.lusidity.data;

import com.lusidity.Environment;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.system.primitives.Primitive;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.process.BaseProgressHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DeduplicateData extends BaseProgressHandler
{
	private boolean outputToConsole=false;
	private int deleted=0;
	private int index=0;
	private Class<? extends DataVertex> current=null;
	private Collection<DeduplicatedItem> deduplicated=new ArrayList<>();

// Constructors
	@SuppressWarnings("unused")
	public DeduplicateData(int maxThreads, int maxItems)
	{
		super(maxThreads, maxItems);
	}

	public DeduplicateData(int maxThreads, int maxItems, boolean outputToConsole)
	{
		super(maxThreads, maxItems);
		this.outputToConsole=outputToConsole;
	}

	@Override
	public void process()
	{
		if (!Environment.getInstance().getApolloVertexTypes().isEmpty())
		{
			Collection<Class<? extends DataVertex>> processed=new ArrayList<>();
			for (Map.Entry<String, Class<? extends ApolloVertex>> entry : Environment.getInstance().getApolloVertexTypes().entrySet())
			{
				if (!processed.contains(entry.getValue()))
				{
					processed.add(entry.getValue());
					try
					{
						this.process(entry.getValue());
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().severe(ex);
						if (this.outputToConsole)
						{
							Environment.getInstance().getReportHandler().say(ex.getMessage());
						}
					}
				}
			}
		}
	}

	@Override
	public File writeExceptionReport()
	{
		return null;
	}

	@SuppressWarnings({
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	public void process(Class<? extends DataVertex> store)
		throws Exception
	{
		this.getProcessStatus().fetchProcessed().getValue().reset();
		this.getProcessStatus().fetchMatches().getValue().reset();
		this.index=0;
		if (!ClassX.isKindOf(store, Primitive.class))
		{
			this.current=store;
			int limit=1000;
			BaseQueryBuilder queryBuilder=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, 0, limit);

			queryBuilder.matchAll();
			queryBuilder.sort("lid", BaseQueryBuilder.Sort.asc);

			int hits=Environment.getInstance().getIndexStore().getQueryFactory().count(queryBuilder);
			DeduplicatedItem deduplicatedItem=this.getDeduplicatedItem();

			if (hits>0)
			{
				this.getProcessStatus().fetchTotal().getValue().set(hits);
				deduplicatedItem.setHits(hits);
				String last=null;
				for (this.index=0; this.index<hits; this.index+=limit)
				{
					Collection<DataVertex> vertices=VertexFactory.getInstance().getAll(store, this.index, limit);
					if (vertices.isEmpty())
					{
						break;
					}
					else
					{
						for (DataVertex vertex : vertices)
						{
							if (!StringX.equals(vertex.fetchId().getValue(), last))
							{
								last=vertex.fetchId().getValue();
								this.handle(vertex, deduplicatedItem);
							}
							else
							{
								this.getProcessStatus().fetchMatches().getValue().increment();
							}
							this.getProcessStatus().fetchProcessed().getValue().increment();
							deduplicatedItem.incrementProcessed();
						}
					}
				}
			}
			if (this.outputToConsole && (deduplicatedItem.getDeleted()>1))
			{
				System.out.println(String.format("%s%s: %d/%d Duplicates(found: %d deleted: %d)%s", System.lineSeparator(),
					deduplicatedItem.getType().getSimpleName(), deduplicatedItem.getIncrementProcessed(), deduplicatedItem.getHits(),
					deduplicatedItem.getFound(), deduplicatedItem.getDeleted(), System.lineSeparator()
				));
			}
		}
		this.current=null;
	}

	private void handle(DataVertex vertex, DeduplicatedItem deduplicatedItem)
		throws Exception
	{
		if (ClassX.isKindOf(vertex, ApolloVertex.class))
		{
			BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(this.current, this.current, 0, 100);
			builder.filter(BaseQueryBuilder.Operators.must, IDataStore.DATA_STORE_ID, BaseQueryBuilder.StringTypes.raw, vertex.fetchId().getValue());
			builder.sort("modifiedWhen", BaseQueryBuilder.Sort.asc);
			builder.setIncludeDuplicates(true);

			if (deduplicatedItem.getFound()==0)
			{
				builder.setApi(BaseQueryBuilder.API._count);
				deduplicatedItem.setFound((deduplicatedItem.getFound()+builder.execute().getCount()));
				builder.setApi(BaseQueryBuilder.API._search);
			}

			// find all the deleted and remove them.
			QueryResults queryResults=builder.execute();
			Collection<DataVertex> items=new ArrayList<>();

			for (IQueryResult queryResult : queryResults)
			{
				items.add(queryResult.getVertex());
			}

			DataVertex working=null;
			for (DataVertex item : items)
			{
				if ((null!=item) && item.hasId())
				{
					if (null==working)
					{
						working=item;
					}
					else
					{
						this.delete(item, deduplicatedItem);
					}
				}
			}

			if (null!=working)
			{
				// the extended properties have been deleted so step4 the object again if it had deleted.
				working.save();
			}
		}
	}

	private void delete(DataVertex vertex, DeduplicatedItem deduplicatedItem)
		throws ApplicationException
	{
		Environment.getInstance().getCache().remove(vertex.getClass(), vertex.getClass(), vertex);
		boolean delete=Environment.getInstance().getDataStore().delete(vertex.getClass(), vertex, 0);
		if (!delete)
		{
			throw new ApplicationException("Failed to delete a duplicate vertex.");
		}
		else
		{
			deduplicatedItem.setDeleted((deduplicatedItem.getDeleted()+1));
			this.deleted+=1;
			this.index--;
		}
	}

	@Override
	protected void done()
	{

	}

	@Override
	public String getStatusText()
	{
		return String.format("%s, deleted: %d, matches: %d", (null==this.current) ? "Processing" : this.current.getSimpleName(), this.getDeleted(), this.getProcessStatus().fetchMatches().getValue().getCount());
	}

	@Override
	public void start()
	{
		this.process();
	}

	public int getDeleted()
	{
		return this.deleted;
	}

	public Collection<DeduplicatedItem> getDeduplicated()
	{
		return this.deduplicated;
	}

	private DeduplicatedItem getDeduplicatedItem()
	{
		DeduplicatedItem result=new DeduplicatedItem(this.current);
		if (this.getDeduplicated().contains(result))
		{
			for (DeduplicatedItem item : this.getDeduplicated())
			{
				if (item.equals(result))
				{
					result=item;
					break;
				}
			}
		}
		else
		{
			this.getDeduplicated().add(result);
		}
		return result;
	}
}
