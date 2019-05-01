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

package com.lusidity.domains.system.assistant.worker;

import com.lusidity.Environment;
import com.lusidity.collections.ElementEdges;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.system.assistant.message.AssistantMessage;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.jobs.BaseJob;
import com.lusidity.jobs.IJob;
import com.lusidity.jobs.JobItem;
import com.lusidity.workers.assistant.IAssistantListener;
import com.lusidity.workers.assistant.IntervalListener;
import org.joda.time.DateTime;

@AtSchemaClass(name="Job Worker", discoverable=false)
public class JobWorker extends BaseAssistantWorker
{
	private transient volatile ProcessStatus processStatus=null;
	private transient volatile JobItem jobItem=null;
	private transient volatile boolean runOnce=false;
	private transient volatile BaseJob job=null;
	private transient volatile boolean running=false;
	private volatile boolean stopping = false;
	private volatile IAssistantListener listener= null;

	// Constructors
	@SuppressWarnings("unused")
	public JobWorker()
	{
		super();
	}

	@SuppressWarnings("unused")
	public JobWorker(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public JobWorker(JobItem jobItem)
	{
		super();
		this.jobItem=jobItem;
	}

	// Overrides	@Override
	public int getDelay()
	{
		return this.getJobItem().getIdleThreshold();
	}
	@Override
	public boolean add(AssistantMessage assistantMessage)
	{
		return false;
	}

	// Getters and setters
	public boolean isStopping()
	{
		return this.stopping;
	}

	@SuppressWarnings("OverlyComplexMethod")
	@Override
	public void doWork(int tries)
	{
		try
		{
			if (!this.isRunning() && (null!=this.getJobItem()) && Environment.getInstance().isOpened())
			{
				try
				{
					this.setRunning(true);
					this.processStatus=new ProcessStatus();

					if ((this.isRunOnce() || this.getJobItem().isAvailable()))
					{
						DateTime started = DateTime.now();
						this.getJobItem().setLastRun(started);
						this.setJob(this.getJobItem().getJob(this.getProcessStatus()));
						if (this.getJob().recordInLog())
						{
							Environment.getInstance().getReportHandler().info("%s job started.", this.getJobItem().getTitle());
						}
						this.getJob().begin(this.processStatus, this.getJobItem().getArgs());
						if ((null!=this.getJob()) && this.job.recordInLog())
						{
							Environment.getInstance().getReportHandler().info("%s job completed.", this.getJobItem().getTitle());
						}
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(String.format("Job error for %s. %s", (null==this.getJob()) ? "unknown job" : this.getJob().getClass().getName(), ex.getMessage()));
				}
				finally
				{
					this.finish();
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	private void finish()
	{
		try
		{
			if (null!=this.getJob())
			{
				this.record();
				this.getJobItem().nullify(false);
				this.setJob(null);
				this.stopping=false;
			}

			if (this.isRunOnce() && !this.getJobItem().isAutomated())
			{
				this.stop();
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
		finally{
			this.setRunOnce(false);
			this.setRunning(false);
		}
	}

	private void record()
	{
		try
		{
			if ((null!=this.getJob()) && this.getJob().recordHistory())
			{
				JobHistory history=JobHistory.create(this.getJob().getTitle(), this.getJob().getClass(), this.getJob().getTimer(), this.getJob().getProcessStatus(),
					(this.getJob().getStatus()==IJob.Status.idle) ? IJob.Status.processed : this.getJob().getStatus()
				);
				history.save();
			}
		}
		catch (Exception ignored){}
	}

	@Override
	public ElementEdges<AssistantMessage> getQueue()
	{
		return null;
	}

	@Override
	protected boolean requiresListener()
	{
		return true;
	}

	@Override
	public Class<? extends AssistantMessage> getExpectedMessageType()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return "account_auditor";
	}

	@Override
	public synchronized IAssistantListener getListener()
	{
		if(null==this.listener){
			IntervalListener il = new IntervalListener(this);
			this.listener = il;
		}
		return this.listener;
	}

	@Override
	public synchronized void setListener(IAssistantListener listener)
	{
		this.listener = listener;
	}

	@Override
	public JsonData getReport(boolean detailed)
	{
		JsonData result=super.getReport(detailed);
		if (null!=this.getJob())
		{
			result.put("job", this.getJob().toJson());
		}

		return result;
	}

	public ProcessStatus getProcessStatus()
	{
		return this.processStatus;
	}

	public synchronized boolean isRunning()
	{
		return this.running;
	}

	public synchronized void setRunning(boolean running)
	{
		this.running=running;
	}


	public boolean isRunOnce()
	{
		return this.runOnce;
	}

	public synchronized void setRunOnce(boolean runOnce)
	{
		this.runOnce=runOnce;
	}

	public JobItem getJobItem(){
		return this.jobItem;
	}

	@Override
	public boolean stop()
	{
		this.stopping = true;
		this.setStarted(false);
		this.setRunOnce(false);
		this.setRunning(false);
		if(null!=this.getJob()){
			this.getJob().stop();
		}
		return super.stop();
	}

	@Override
	public void reset()
	{
		if(null!=this.getJob()){
			this.setJob(null);
		}
		if(null!=this.getJobItem()){
			this.getJobItem().nullify(true);
		}
	}

	public synchronized BaseJob getJob()
	{
		return this.job;
	}

	public synchronized void setJob(BaseJob job)
	{
		this.job=job;
	}
}
