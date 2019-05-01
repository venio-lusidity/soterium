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

package com.lusidity.process;


import com.lusidity.Environment;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.threading.BaseThrottler;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public abstract class BaseProgressHandler extends BaseThrottler implements IProgressStatusProvider
{
	protected Stopwatch timer=null;
	private int maxItems=0;
	protected boolean stopping=false;
	private String vertexId=null;
	private ProcessStatus processStatus= new ProcessStatus();

	// Constructors
	public BaseProgressHandler(int maxThreads, int maxItems)
	{
		super(maxThreads, BaseThrottler.MAX_WAIT);
		this.maxItems=maxItems;
	}

	public BaseProgressHandler(int maxThreads, int maxItems, int maxWait)
	{
		super(maxThreads, maxWait);
		this.maxItems=maxItems;
	}

	public BaseProgressHandler(int maxThreads, int maxItems, int maxBatch, int maxWait)
	{
		super(maxThreads, maxBatch, maxWait);
		this.maxItems=maxItems;
	}
// Overrides
	@Override
	public String getProgressStatusText()
	{
		String result=this.getStatusText();
		if (StringX.isBlank(result))
		{
			result = "Processing";
			/*
			ProcessStatus processStatus=this.combineProcessStatus();
			//noinspection IntegerDivisionInFloatingPointContext
			result=String.format("%s: P: %s C: %s M: %s S: %s, E: %s Est:%d PS: %d",
				StringX.isBlank(processStatus.getMessage()) ? "Processing" : processStatus.getMessage(),
				NumberFormat.getNumberInstance(Locale.US).format(processStatus.fetchPrimary().getValue().getCount()),
				NumberFormat.getNumberInstance(Locale.US).format(processStatus.fetchInnerProcessed().getValue().getCount()),
				NumberFormat.getNumberInstance(Locale.US).format(processStatus.fetchMatches().getValue().getCount()),
				NumberFormat.getNumberInstance(Locale.US).format(processStatus.fetchSkipped().getValue().getCount()),
				NumberFormat.getNumberInstance(Locale.US).format(processStatus.fetchErrors().getValue().getCount()),
				((null!=this.getTimer()) && this.getTimer().isStarted()) ? (int) Math.floor(this.getTimer().elapsed().getMillis()/1000) : 0, this.getItemsPerSecond()
			);
			*/
		}
		return result;
	}

	@Override
	public int getCurrentProgressCount()
	{
		return this.combineProcessStatus().fetchProcessed().getValue().getCount();
	}

	@Override
	public int getWholeProcessCount()
	{
		return this.combineProcessStatus().fetchTotal().getValue().getCount();
	}

	@Override
	public String toString()
	{
		return String.format("\n%d/%d %s\n", this.getCurrentProgressCount(), this.getWholeProcessCount(),
			(null!=this.timer) ? this.timer.elapsed().toString() : "not started"
		);
	}

	public ProcessStatus getProcessStatus()
	{
		return this.processStatus;
	}

	public ProcessStatus combineProcessStatus(){
		return  this.processStatus;
	}

	public void setProcessStatus(ProcessStatus processStatus){
		this.processStatus= processStatus;
	}

	@Override
	public void close()
		throws IOException
	{
		super.close();
		//noinspection StatementWithEmptyBody
		while (this.stopping)
		{
			//TODO: ensure that this is not creating a dead lock.
		}
		this.timer.stop();
	}

	public void process(String vertexId)
	{
		this.vertexId=vertexId;
		this.process();
	}

	public void process()
	{
		this.timer=new Stopwatch();
		this.timer.start();
		this.start();
		if (this.getMaxThreads()>1)
		{
			this.waitAll(true);
		}
		this.timer.stop();
		this.stopping=false;

		this.done();
		this.writeExceptionReport();
		this.report();
	}

	public abstract File writeExceptionReport();

	public abstract void start();

	public void report()
	{
		String out;
		try
		{
			if ((null!=this.timer))
			{
				long perSecond=this.getItemsPerSecond();
				int est=0;
				if ((this.getCurrentProgressCount()>0) && (this.getWholeProcessCount()>0))
				{
					long left=this.getWholeProcessCount()-this.getCurrentProgressCount();
					est=Math.round(left/perSecond);
				}
				DateTime estimated=DateTime.now().plusSeconds(est);
				out=String.format("\n%d/%d %s Estimated: %s\n",
					this.getCurrentProgressCount(),
					this.getWholeProcessCount(),
					this.getProgressStatusText(),
					estimated.toString("dd-MMM-yy HH:mm:ss")
				);
				System.out.println(out);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	public synchronized void stop()
	{
		Environment.getInstance().getReportHandler().say("Canceled and stopping...");
		this.stopping();
	}

	public void stopping()
	{
		this.stopping=true;
	}

	public void startTimer()
	{
		this.timer=new Stopwatch();
	}

	protected abstract void done();

// Getters and setters
	public abstract String getStatusText();

	public synchronized int getMaxItems()
	{
		return this.maxItems;
	}

	public String getVertexId()
	{
		return this.vertexId;
	}

	public Stopwatch getTimer()
	{
		return this.timer;
	}

	public long getItemsPerSecond()
	{
		long result=0;
		if ((null!=this.timer) && this.timer.isStarted() && (this.getCurrentProgressCount()>0))
		{
			result=this.timer.getPerSecond(this.getCurrentProgressCount());
		}
		return result;
	}

	public boolean isStopping()
	{
		return this.stopping || Environment.getInstance().getDataStore().isOffline();
	}
}
