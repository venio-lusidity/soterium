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

package com.lusidity.threading;


import com.lusidity.Environment;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.system.SystemInfo;
import com.lusidity.tasks.TaskManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class BaseThrottler implements Closeable
{

	protected static final Double ADD_THREAD_MAX_LOAD=250.00;
	protected static final Double PROCESSOR_MAX_LOAD=280.00;
	protected static final int MAX_WAIT=((1000*60)*5);
	protected static final int MAX_BATCH=100;

	private boolean multiThreaded=false;
	private int threadCount=1;
	private TaskManager taskManager=null;
	private int maxThreads=1;
	private int maxWaitInMillis=BaseThrottler.MAX_WAIT;
	private int maxBatch=BaseThrottler.MAX_BATCH;

	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private Collection<Future<ProcessStatus>> futures=new ArrayList<>();

	// Constructors
	public BaseThrottler(int maxThreads, int maxWaitInMillis)
	{
		super();
		this.setMaxThreads(maxThreads);
		this.maxWaitInMillis=maxWaitInMillis;
		this.multiThreaded=(this.maxThreads>1);
		this.load();
	}

	private void load()
	{
		if (this.isMultiThreaded())
		{
			this.startFixed();
		}
	}

	private boolean isMultiThreaded()
	{
		return this.multiThreaded;
	}

	private void startFixed()
	{
		this.taskManager=new TaskManager();
		this.getTaskManager().startFixed(this.threadCount);
	}

	protected TaskManager getTaskManager()
	{
		return this.taskManager;
	}

	public BaseThrottler(int maxThreads, int maxBatch, int maxWaitInMillis)
	{
		super();
		this.maxThreads=maxThreads;
		this.maxWaitInMillis=maxWaitInMillis;
		this.multiThreaded=(this.maxThreads>1);
		this.maxBatch=(maxBatch<BaseThrottler.MAX_BATCH) ? maxBatch : BaseThrottler.MAX_BATCH;
		this.load();
	}

// Overrides
	@Override
	public void close()
		throws IOException
	{
		try
		{
			this.waitAll(true);
			if(null!=this.getTaskManager())
			{
				this.getTaskManager().stop();
			}
		}
		catch (Exception ignored){}
	}

	protected void waitAll(boolean force)
	{
		if (!this.futures.isEmpty() && (force || (this.futures.size()>=this.maxBatch)))
		{
			this.getTaskManager().callAndWait(this.futures);
			this.futures=new ArrayList<>();
		}
	}

	public synchronized void submit(Callable<ProcessStatus> task)
	{
		try
		{
			this.waitAll(false);
			Future<ProcessStatus> future=this.getTaskManager().submit(task);
			this.futures.add(future);
			this.throttle();
		}
		catch (ApplicationException e)
		{
			Environment.getInstance().getReportHandler().severe(e);
		}
	}

	protected synchronized void throttle()
	{
		if (this.isMultiThreaded() && (this.futures.size()>this.maxThreads) && (this.threadCount<this.maxThreads))
		{
			Double processorLoad=SystemInfo.getInstance().getProcessorLoad();
			int availableThreads=SystemInfo.getInstance().getAvailableProcessors();

			if ((this.futures.size()>1) && (processorLoad>BaseThrottler.PROCESSOR_MAX_LOAD))
			{
				if (this.threadCount>1)
				{
					this.threadCount-=1;
					this.restart();
				}
			}
			else if ((processorLoad<BaseThrottler.ADD_THREAD_MAX_LOAD)
			         && ((this.threadCount+1)<=availableThreads))
			{
				this.threadCount+=1;
				this.restart();
			}
		}
	}

	private void restart()
	{
		this.waitAll(true);
		try
		{
			this.getTaskManager().stop();
		}
		catch (Exception ignored)
		{
		}
		this.startFixed();
	}

// Getters and setters
	public synchronized int getMaxThreads()
	{
		return this.maxThreads;
	}

	public synchronized void setMaxThreads(int maxThreads)
	{
		this.maxThreads=maxThreads;
	}

	public synchronized void setMaxBatch(int maxBatch){
		this.maxBatch = maxBatch;
	}
}
