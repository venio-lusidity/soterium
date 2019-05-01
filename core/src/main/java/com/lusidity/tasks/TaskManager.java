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
import com.lusidity.framework.time.Stopwatch;
import org.apache.commons.collections.CollectionUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class TaskManager implements Closeable
{
	// ------------------------------ FIELDS ------------------------------

// Fields
	public static final int MAX_SLEEP=500;
	/**
	 * Number of CPU cores reserved for system threads.
	 */
	private static final int RESERVED_CORES=2;
	/**
	 * Polling interval during shutdown, in milliseconds.
	 */
	private static final long SHUTDOWN_WAIT_INTERVAL=((60*1000)*10);
	private static final int MAX_THREADS=10;
	private ExecutorService executorService=null;

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
	public void stop()
	{
		if (null!=this.executorService)
		{
			//  Stop accept new threads
			this.executorService.shutdown();
			try
			{
				if (!this.executorService.awaitTermination(TaskManager.SHUTDOWN_WAIT_INTERVAL, TimeUnit.MILLISECONDS))
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
	 * Start task manager.
	 */
	public void startFixed()
	{
		int nCores=Runtime.getRuntime().availableProcessors();
		int nThreads=Math.max(1, nCores-TaskManager.RESERVED_CORES);
		this.executorService=Executors.newFixedThreadPool(nThreads);
	}

	public void start(int dividedBy)
	{
		int nCores=Runtime.getRuntime().availableProcessors();
		int nThreads=Math.max(1, nCores/dividedBy);
		this.executorService=Executors.newFixedThreadPool(nThreads);
	}

	/**
	 * Start task manager.
	 */
	public void start()
	{
		this.executorService=Executors.newCachedThreadPool();
	}

	public void startFixed(int nThreads)
	{
		this.executorService=Executors.newFixedThreadPool(nThreads);
	}

	/**
	 * Invoke all com.lusidity.tasks, waiting for execution to complete.
	 *
	 * @param futures Tasks.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> callAndWait(Collection<Future<T>> futures)
	{
		List<T> results=new ArrayList<>();
		if (!this.isReady())
		{
			this.start();
		}

		try
		{
			for (Future<T> future : futures)
			{
				try
				{
					Object result=future.get();
					if ((null!=result))
					{
						if ((result.getClass().equals(ArrayList.class) || result.getClass().equals(Collection.class)))
						{
							Iterable<T> iterable=(Iterable<T>) result;
							for (T item : iterable)
							{
								if (!results.contains(item))
								{
									results.add(item);
								}
							}
						}
						else
						{
							T item=(T) result;
							results.add(item);
						}
					}
				}
				catch (Exception e)
				{
					Environment.getInstance().getReportHandler().warning(e);
					try
					{
						future.cancel(true);
					}
					catch (Exception ignore){}
					try
					{
						if (null!=results)
						{
							Object v=false;
							T value=(T) v;
							results.add(value);
						}
					}
					catch (Exception ignored){}
				}
			}
		}
		catch (Exception ex)
		{
			// There is always the possibility for the tasks to be updated while this is running and cause a concurrent modification exception on the array.
			// So we ignore and start over.
		}

		return results;
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

	public <T> void waitUntilDone(Iterable<Future<T>> futures)
	{
		long sleep=100;
		Stopwatch stopwatch=Stopwatch.begin();
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
					catch (Exception ignored)
					{
					}
				}
				else if (stopwatch.isStarted())
				{
					stopwatch.stop();
					sleep=stopwatch.elapsed().getMillis();
					sleep=(sleep>1000) ? TaskManager.MAX_SLEEP : sleep;
				}
			}
		}
	}

	/**
	 * Invoke all com.lusidity.tasks, waiting for execution to complete.
	 *
	 * @param futures         Tasks.
	 * @param results         Can be null;
	 * @param timeOutInMillis The maximum amount of time to wait on each individual future before moving on.
	 */
	@SuppressWarnings("unchecked")
	public <T> void callAndWait(Iterable<Future<T>> futures, Collection results, long timeOutInMillis)
	{
		if (!this.isReady())
		{
			this.start();
		}

		try
		{
			for (Future<T> future : futures)
			{
				try
				{
					Object result=future.get(timeOutInMillis, TimeUnit.MILLISECONDS);
					if ((null!=results) && (null!=result))
					{
						if ((result.getClass().equals(ArrayList.class) || result.getClass().equals(Collection.class)))
						{
							Iterable<T> iterable=(Iterable<T>) result;
							for (T item : iterable)
							{
								if (!results.contains(item))
								{
									results.add(item);
								}
							}
						}
						else
						{
							T item=(T) result;
							results.add(item);
						}
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

					try
					{
						if (null!=results)
						{
							results.add(false);
						}
					}
					catch (Exception ignored)
					{
					}
				}
			}
		}
		catch (Exception ex)
		{
			// There is always the possibility for the tasks to be updated while this is running and cause a concurrent modification exception on the array.
			// So we ignore and start over.
		}
	}

	public ExecutorService getExecutorService()
	{
		return this.executorService;
	}

	/**
	 * Asynchronously submit a single task.
	 *
	 * @param task Task.
	 * @return Future.
	 */
	public synchronized <T> Future<T> submit(Callable<T> task)
		throws ApplicationException
	{
		if (!this.isReady())
		{
			throw new ApplicationException("\nTask manager is not ready!\n");
		}
		return this.getExecutorService().submit(task);
	}

	public <T> Future<T> submit(Runnable task)
	{
		//noinspection unchecked
		return (Future<T>) this.executorService.submit(task);
	}

	/**
	 * Synchronously invoke a single task and wait for it to complete.
	 *
	 * @param task Task to invoke.
	 * @return result
	 */
	public <T> T callAndWait(Callable<T> task)
		throws ApplicationException
	{
		Collection<Callable<T>> tasks=new ArrayList<Callable<T>>();
		tasks.add(task);
		Collection<T> results=this.invokeAll(tasks);
		//noinspection unchecked
		return (T) CollectionUtils.get(results, 0);
	}

	/**
	 * Invoke all com.lusidity.tasks, waiting for execution to complete.
	 *
	 * @param tasks Tasks.
	 * @return Futures.
	 */
	public <T> List<T> invokeAll(Collection<Callable<T>> tasks)
		throws ApplicationException
	{
		List<T> results=new ArrayList<>();
		if (!this.isReady())
		{
			throw new ApplicationException("\nTask manager is not ready!\n");
		}

		try
		{
			List<Future<T>> futures = this.executorService.invokeAll(tasks);
			for (Future<T> future : futures)
			{
				T result = future.get();
				results.add(result);
			}
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}

		return results;
	}
}