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

package com.lusidity.jobs;

import com.lusidity.Environment;
import com.lusidity.configuration.JobConfigurations;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.system.assistant.worker.JobWorker;
import com.lusidity.framework.json.JsonData;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class JobsEngine implements Closeable
{
	private static final int DEFAULT_DELAY=500;
	private static final int DEFAULT_IDLE_THRESHOLD=500;
	private static JobsEngine instance=null;
	private final Map<JobItem, JobWorker> jobs=new LinkedHashMap<>();
	private final List<JobItem> jobItems=new ArrayList<>();
	private boolean started=false;

// Constructors
	public JobsEngine()
	{
		super();
		JobsEngine.instance=this;
	}

// Methods
	public static JobsEngine getInstance()
	{
		return JobsEngine.instance;
	}

	public void start()
	{
		if (!this.isStarted())
		{
			this.loadJobTypes();
			this.begin();
			this.started=true;
		}
	}

	public boolean isStarted()
	{
		return this.started;
	}

	private void loadJobTypes()
	{
		List<Class<? extends IJob>> items=JobConfigurations.getInstance().getJobs();
		for (Class<? extends IJob> item : items)
		{
			try
			{
				if(null!=item)
				{
					JobItem jobItem=new JobItem(item, new ProcessStatus());
					this.jobItems.add(jobItem);
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		this.jobItems.sort(new Comparator<JobItem>()
		{
			// Overrides
			@Override
			public int compare(JobItem o1, JobItem o2)
			{
				int result=0;
				try
				{
					result=o1.getTitle().compareTo(o2.getTitle());
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
				return result;
			}
		});
	}

	private void begin()
	{
		for (JobItem item : this.jobItems)
		{
			JobWorker worker=new JobWorker(item);
			this.jobs.put(item, worker);
			if(item.isAutomated())
			{
				worker.start(JobsEngine.DEFAULT_DELAY, JobsEngine.DEFAULT_IDLE_THRESHOLD);
			}
		}
	}

	public boolean isRunning(Class<? extends IJob> cls)
	{
		JobWorker worker=this.getWorker(cls);
		return (null!=worker) && worker.isRunning();
	}

	public synchronized JobWorker getWorker(Class<? extends IJob> cls)
	{
		JobWorker result=null;
		for (Map.Entry<JobItem, JobWorker> entry : this.jobs.entrySet())
		{
			if (entry.getKey().getType().equals(cls))
			{
				result=entry.getValue();
				break;
			}
		}

		return result;
	}

	public boolean doJob(Class<? extends IJob> cls, boolean stop, boolean reset, Object... args){
		boolean result = false;
		JobItem item = this.getJobItem(cls);
		if(null!=item){
			try
			{
				JobWorker worker=this.getWorker(cls);
				item.setArgs(args);
				if((null!=worker) && reset){
					worker.restart();
					worker.reset();
				}
				else if((null!=worker) && stop){
					result = !worker.stop();
				}
				else
				{
					if ((null!=worker) && !worker.isRunning())
					{
						worker.start(worker.getDelay(), worker.getIdleThreshold());
						worker.setRunOnce(true);
						result=true;
					}
				}
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	private JobItem getJobItem(Class<? extends IJob> cls)
	{
		JobItem result=null;
		for (JobItem item : this.jobItems)
		{
			if (item.getType().equals(cls))
			{
				result=item;
				break;
			}
		}
		return result;
	}

	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();
		JsonData items=JsonData.createArray();
		for (Map.Entry<JobItem, JobWorker> task : this.jobs.entrySet())
		{
			JsonData data=task.getKey().toJson();
			data.put("worker", task.getValue().getReport(true));
			items.put(data);
		}
		result.put("workers", items);
		return result;
	}

	public void stop()
		throws IOException
	{
		this.close();
	}

	@Override
	public void close()
		throws IOException
	{
		for (Map.Entry<JobItem, JobWorker> entry : this.jobs.entrySet())
		{
			JobWorker worker=entry.getValue();
			worker.stop();
		}
	}
}
