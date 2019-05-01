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

package com.lusidity.collections;


import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.progress.ProgressHandler;
import com.lusidity.framework.text.StringX;
import com.lusidity.jobs.data.tasks.DataVertexDeleteTask;
import com.lusidity.tasks.TaskManager;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class VertexIterator implements ProgressHandler
{
	// Fields
	public static final int DEFAULT_BATCH_SIZE=100;
	private Class<? extends Edge> edge = null;
	private Class<? extends DataVertex> store;
	private int start = 0;
	private int limit = 0;
	private BaseQueryBuilder queryBuilder = null;
	private boolean stopping = false;
	private int processed = 0;
	private int total = 0;
	private String text = "Processing";

	// Constructors
	public VertexIterator(Class<? extends DataVertex> store){
		super();
		this.store= store;
	}

	public VertexIterator(Class<? extends DataVertex> store, int start, int limit){
		super();
		this.store= store;
		this.start = start;
		this.limit=limit;
	}

	public VertexIterator(Class<? extends DataVertex> store, BaseQueryBuilder queryBuilder, int start, int limit){
		super();
		this.store= store;
		this.start = start;
		this.limit = limit;
		this.queryBuilder = queryBuilder;
	}

	public VertexIterator(Class<? extends Edge> edge, Class<? extends DataVertex> store, int start, int limit)
	{
		super();
		this.edge = edge;
		this.store=store;
		this.start = start;
		this.limit=limit;
	}

	// Overrides
	@Override
	public int getWholeProcessCount()
	{
		return this.total;
	}

	@Override
	public int getCurrentProgressCount()
	{
		return this.processed;
	}

	@Override
	public String getProgressStatusText()
	{
		return StringX.isBlank(this.text) ? "Processing" : this.text;
	}

	public void partition(IVertexHandler  vertexHandler, ProcessStatus processStatus, int threads)
	{

	}


	/**
	 * Get all of the vertices in this class, will also get deprecated vertices.
	 *
	 * @param handler An class that implements IVertexHandler.
	 * @param processStatus A ProcessStatus
	 * @param maxThreads  Maximum number of threads to use.
	 */
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	public void iterate(IQueryResultHandler handler, ProcessStatus processStatus, int maxThreads)
	{
		this.iterate(handler, processStatus, 0, maxThreads);
	}

	/**
	 * Get all of the vertices in this class, will also get deprecated vertices.
	 *
	 * @param handler An class that implements IVertexHandler.
	 * @param processStatus A ProcessStatus
	 * @param max Maximum number of vertices to iterate.
	 * @param maxThreads  Maximum number of threads to use.
	 */
	public void iterate(IQueryResultHandler handler, ProcessStatus processStatus, int max, int maxThreads)
	{
		try(TaskManager tm = new TaskManager())
		{
			if (null!=this.store)
			{
				if (this.start<=this.getLimit())
				{
					if (null==this.queryBuilder)
					{
						this.queryBuilder=Environment.getInstance().getIndexStore().getQueryBuilder((null==this.edge) ? this.store : this.edge, this.store, 0, this.getLimit());
						this.queryBuilder.matchAll();
						this.queryBuilder.sort("createdWhen", BaseQueryBuilder.Sort.asc);
					}

					int hits=Environment.getInstance().getIndexStore().getQueryFactory().count(this.queryBuilder);
					processStatus.fetchTotal().getValue().add(hits);
					int on=0;
					boolean stop=false;
					this.setTotal(hits);
					if(maxThreads>1){
						tm.startFixed(maxThreads);
					}
					Collection<Future<Boolean>> futures = new ArrayList<>();

					for (int i=0; i<hits; i+=this.getLimit())
					{
						if (this.isStopping())
						{
							break;
						}
						this.queryBuilder.setStart(i);
						this.queryBuilder.setLimit(this.getLimit());

						QueryResults queryResults=this.queryBuilder.execute();
						if (queryResults.isEmpty())
						{
							break;
						}

						for (IQueryResult result : queryResults)
						{
							try
							{
								if (this.isStopping())
								{
									break;
								}
								if (maxThreads>1)
								{
									QueryResultIteratorTask task=new QueryResultIteratorTask(handler, result, processStatus, on, hits, this.start, this.getLimit());
									Future<Boolean> future=tm.submit(task);
									futures.add(future);
									if(futures.size()>=VertexIterator.DEFAULT_BATCH_SIZE){
										this.callAndWait(tm, futures, processStatus);
										futures = new ArrayList<>();
									}
								}
								else
								{
									stop=handler.handle(result, processStatus, on, hits, this.start, this.getLimit());
									this.increment();
									processStatus.fetchProcessed().getValue().increment();
								}
								if (stop)
								{
									break;
								}
							}
							catch (Exception ex){
								Environment.getInstance().getReportHandler().warning(ex);
							}
							on++;
						}

						if (stop || ((max>0) && ((i+this.getLimit())>=max)))
						{
							break;
						}
					}

					if(!futures.isEmpty()){
						this.callAndWait(tm, futures, processStatus);
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		this.stopping = false;
	}

	public synchronized int getLimit()
	{
		if (this.limit<=0)
		{
			this.limit=Environment.getInstance().getConfig().maxIteratorSize();
		}
		return this.limit;
	}

	public void setTotal(int total)
	{
		this.total=total;
	}

	public boolean isStopping()
	{
		return this.stopping;
	}

	private void callAndWait(TaskManager tm, Collection<Future<Boolean>> futures, ProcessStatus processStatus)
	{
		int size=futures.size();
		tm.callAndWait(futures);
		this.increment(size);
		processStatus.fetchProcessed().getValue().add(size);
	}

	public void increment()
	{
		this.processed++;
	}

	public void increment(int add)
	{
		this.processed+=add;
	}


	/**
	 * Get all of the vertices in this class, will also get deprecated vertices.
	 *
	 * @param handler An class that implements IVertexHandler.
	 * @param processStatus A ProcessStatus
	 * @param maxThreads  Maximum number of threads to use.
	 */
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	public void iterate(IVertexHandler handler, ProcessStatus processStatus, int maxThreads)
	{
		this.iterate(handler, processStatus, 0, maxThreads);
	}

	/**
	 * Get all of the vertices in this class, will also get deprecated vertices.
	 *
	 * @param handler An class that implements IVertexHandler.
	 * @param processStatus A ProcessStatus
	 * @param max Maximum number of vertices to iterate.
	 * @param maxThreads  Maximum number of threads to use.
	 */
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	public void iterate(IVertexHandler handler, ProcessStatus processStatus, int max, int maxThreads)
	{
		try (TaskManager tm = new TaskManager())
		{
 			if (null!=this.store)
			{
				if (this.start<=this.getLimit())
				{
					if (null==this.queryBuilder)
					{
						this.queryBuilder=Environment.getInstance().getIndexStore().getQueryBuilder((null==this.edge) ? this.store : this.edge, this.store, 0, this.getLimit());
						this.queryBuilder.matchAll();
						this.queryBuilder.sort("createdWhen", BaseQueryBuilder.Sort.asc);
					}

					int hits=Environment.getInstance().getIndexStore().getQueryFactory().count(this.queryBuilder);
					processStatus.fetchTotal().getValue().add(hits);
					int on=0;
					boolean stop=false;
					this.setTotal(hits);
					if(maxThreads>1){
						tm.startFixed(maxThreads);
					}
					Collection<Future<Boolean>> futures = new ArrayList<>();

					for (int i=0; i<hits; i+=this.getLimit())
					{
						if (this.isStopping())
						{
							break;
						}
						this.queryBuilder.setStart(i);
						this.queryBuilder.setLimit(this.getLimit());

						QueryResults queryResults=this.queryBuilder.execute();
						if (queryResults.isEmpty())
						{
							break;
						}

						for (IQueryResult result : queryResults)
						{
							try
							{
								if (this.isStopping())
								{
									break;
								}
								DataVertex vertex=result.getVertex();
								if (maxThreads>1)
								{
									VertexIteratorTask task=new VertexIteratorTask(handler, vertex, processStatus, on, hits, this.start, this.getLimit());
									Future<Boolean> future=tm.submit(task);
									futures.add(future);
									if(futures.size()>=VertexIterator.DEFAULT_BATCH_SIZE){
										this.callAndWait(tm, futures, processStatus);
										futures = new ArrayList<>();
									}
								}
								else
								{
									stop=handler.handle(vertex, processStatus, on, hits, this.start, this.getLimit());
									this.increment();
									processStatus.fetchProcessed().getValue().increment();
								}
								if (stop)
								{
									break;
								}
							}
							catch (Exception ex){
								Environment.getInstance().getReportHandler().warning(ex);
							}
							on++;
							if (stop || ((max>0) && (on>=max)))
							{
								break;
							}
						}

						if (stop || ((max>0) && (on>=max)))
						{
							break;
						}
					}

					if(!futures.isEmpty()){
						this.callAndWait(tm, futures, processStatus);
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		this.stopping = false;
	}

	public boolean stop()
	{
		this.stopping = true;
		return true;
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	public boolean delete(ProcessStatus processStatus, int maxThreads)
	{
		boolean result = true;
		if (null!=this.store){
			try(TaskManager tm = new TaskManager())
			{
				int queryLimit=Environment.getInstance().getConfig().maxIteratorSize();
				if (null==this.queryBuilder)
				{
					this.queryBuilder=Environment.getInstance().getIndexStore().getQueryBuilder((null==this.edge) ? this.store : this.edge, this.store, 0, queryLimit);
					this.queryBuilder.matchAll();
					this.queryBuilder.sort("createdWhen", BaseQueryBuilder.Sort.asc);
				}

				int hits=Environment.getInstance().getIndexStore().getQueryFactory().count(this.queryBuilder);
				processStatus.fetchTotal().getValue().add(hits);
				this.setTotal(hits);
				if(hits>0)
				{
					if(maxThreads>1){
						tm.startFixed(maxThreads);
					}
					Collection<Future<Boolean>> futures = new ArrayList<>();

					processStatus.setMessage("Building delete list.");

					for (int i=0; i<hits; i+=queryLimit)
					{
						if (this.isStopping())
						{
							break;
						}

						// always start at 0 as the previous vertices where deleted.
						this.queryBuilder.setStart(0);
						this.queryBuilder.setLimit(queryLimit);
						QueryResults qrs=this.queryBuilder.execute();
						if (qrs.isEmpty())
						{
							break;
						}

						Set<ApolloVertex> vertices=new HashSet<>();

						for (IQueryResult qr : qrs)
						{
							if (this.isStopping())
							{
								break;
							}
							ApolloVertex vertex=qr.getVertex();
							vertices.add(vertex);
							processStatus.fetchProcessed().getValue().increment();
						}

						int len=vertices.size();
						for (int j=0; j<len; j++)
						{
							if (this.isStopping())
							{
								break;
							}

							ApolloVertex vertex=(ApolloVertex) CollectionUtils.get(vertices, j);
							processStatus.setMessage("%s deleting", StringX.isBlank(vertex.fetchTitle().getValue()) ? vertex.fetchId().getValue() : vertex.fetchTitle().getValue());

							if(maxThreads>1)
							{
								DataVertexDeleteTask task = new DataVertexDeleteTask(vertex, processStatus, true);
								Future<Boolean> future = tm.submit(task);
								futures.add(future);
							}
							else{
								boolean deleted=vertex.delete();
								if (deleted)
								{
									processStatus.fetchDeleted().getValue().increment();
								}
								else
								{
									processStatus.setMessage("%s cannot delete: ", vertex.fetchId().getValue());
								}
							}
						}
						if(!futures.isEmpty()){
							int size = futures.size();
							tm.callAndWait(futures);
							processStatus.fetchDeleted().getValue().add(size);
							this.increment(size);
							futures = new ArrayList<>();
						}
						Environment.getInstance().getIndexStore().makeAvailable(this.store, true);
						this.increment();
					}
					if(!futures.isEmpty()){
						int size = futures.size();
						tm.callAndWait(futures);
						processStatus.fetchDeleted().getValue().add(size);
						this.increment(size);
						futures = new ArrayList<>();
					}

					Environment.getInstance().getIndexStore().makeAvailable(this.store, true);
					this.queryBuilder.setStart(0);
					hits=Environment.getInstance().getIndexStore().getQueryFactory().count(this.queryBuilder);
					result = (hits==0);
				}
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().warning(ex);
				result = false;
			}
		}
		processStatus.setMessage("Processing");
		return result;
	}

	public void setProgressText(String text){
		this.text = text;
	}
}
