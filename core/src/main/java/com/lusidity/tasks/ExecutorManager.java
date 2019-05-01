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

package com.lusidity.tasks;

import com.lusidity.Environment;
import com.lusidity.framework.exceptions.ApplicationException;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Executor Manager uses a calculated fixed thread pool.
 *
 * @param <T> A type of Callable or Runnable.
 */
public class ExecutorManager<T> implements Closeable
{
	// ------------------------------ FIELDS ------------------------------

	// Fields
	private static final long SHUTDOWN_WAIT_INTERVAL=((60*1000)*10);
	private ExecutorService executorService=null;
	private List<Future> futures=new ArrayList<>();
	private int batchSize=100;

	// -------------------------- OTHER METHODS --------------------------

	// Overrides
	@Override
	public void close()
		throws IOException
	{
		this.stop();
	}

	/**
	 * Stop task manager.
	 */
	private void stop()
	{
		if (null!=this.executorService)
		{
			//  Stop accept new threads
			this.executorService.shutdown();
			try
			{
				if (!this.executorService.awaitTermination(ExecutorManager.SHUTDOWN_WAIT_INTERVAL, TimeUnit.MILLISECONDS))
				{
					this.executorService.shutdownNow();
				}
			}
			catch (InterruptedException e)
			{
				this.executorService.shutdownNow();
			}
		}
	}

	/**
	 * Call and wait for all remaining task.
	 */
	public List<T> finished()
	{
		List<T> results=this.callAndWait(true);
		this.futures=new ArrayList<>();
		return results;
	}

	@SuppressWarnings("unchecked")
	public List<T> callAndWait(boolean override)
	{
		List<T> results=new ArrayList<>();
		if (override || (this.futures.size()>=this.batchSize))
		{
			for ( Future<T> future : this.futures)
			{
				try
				{
					T t=future.get();
					if (null!=t)
					{
						results.add(t);
					}
				}
				catch (Exception e)
				{
					Environment.getInstance().getReportHandler().warning(e);
					try
					{
						future.cancel(true);
					}
					catch (Exception ignore)
					{
					}
				}
			}
		}
		return results;
	}

	public <T> void waitUntilDone(Iterable<Future<T>> futures)
	{
		long sleep=100;
		for (Future<T> future : futures)
		{
			boolean done=false;
			while (!done)
			{
				done=(future.isDone() || future.isCancelled());
				if (!done)
				{
					try
					{
						Thread.sleep(sleep);
					}
					catch (Exception ignored){}
				}
			}
		}
	}

	/**
	 * Synchronously submit a single task.
	 *
	 * @param task A Callable task.
	 */
	public <T> void submit(Callable<T> task)
		throws ApplicationException
	{
		if (!this.isReady())
		{
			this.start(this.batchSize);
		}
		Future future=this.getExecutorService().submit(task);
		this.futures.add(future);
	}

	/**
	 * Is task manager ready to accept new com.lusidity.tasks?
	 *
	 * @return true if task manager is ready to accept new com.lusidity.tasks.
	 */
	public boolean isReady()
	{
		return (this.executorService!=null) && !this.executorService.isShutdown();
	}

	/**
	 * Start the Executor Manager
	 *
	 * @param maxBatchSize How many tasks can be queued before blocking.
	 */
	public void start(int maxBatchSize)
	{
		this.batchSize=maxBatchSize;
		int cores=Runtime.getRuntime().availableProcessors();
		Environment.getInstance().getReportHandler().fine("ExecutorManager cores available %d.", cores);
		if (cores<=2)
		{
			Environment.getInstance().getReportHandler().fine("Multi-threading is not possible with only %d cores.", cores);
		}
		else
		{
			int nThreads=Math.max(2, (cores/2));
			Environment.getInstance().getReportHandler().fine("Starting the ExecutorManager with %d threads.", nThreads);
			this.executorService=Executors.newFixedThreadPool(nThreads);
		}
	}

	public ExecutorService getExecutorService()
	{
		return this.executorService;
	}

	/**
	 * Synchronously submit a single task.
	 *
	 * @param task A Runnable task.
	 */
	public void submit(Runnable task)
	{
		if (!this.isReady())
		{
			this.start(this.batchSize);
		}

		//noinspection unchecked
		Future<T> future=(Future<T>) this.executorService.submit(task);
		this.futures.add(future);
	}

	public List<T> callAndWait()
	{
		return this.callAndWait(false);
	}
}