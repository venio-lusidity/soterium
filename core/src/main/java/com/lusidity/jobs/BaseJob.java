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
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.office.CSVUtils;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public abstract class BaseJob implements IJob
{
	private Stopwatch timer = null;
	private ProcessStatus processStatus = null;
	private IJob.Status status=IJob.Status.idle;
	private DateTime lastRun = null;
	private CSVUtils csvUtils = new CSVUtils();
	private boolean stopping = false;

	// Constructors
	public BaseJob()
	{
		super();
		this.timer = new Stopwatch();
		this.processStatus = new ProcessStatus();
	}

	public BaseJob(ProcessStatus processStatus)
	{
		super();
		this.timer = new Stopwatch();
		this.processStatus = processStatus;
	}

    // Overrides
	@Override
	public JsonData toJson()
	{
		JsonData result=(null==this.getProcessStatus()) ? JsonData.createObject() : this.getProcessStatus().toJson();
		result.put("title", JsonData.makeLabel("Title", this.getTitle()));
		result.put("group", JsonData.makeLabel("Group", this.getGroup()));
		result.put("_ordinal", this.getOrdinal(), true);
		result.put("_title", this.getTitle(), true);
		result.put("automated", this.isAutomated());
		result.put("description", JsonData.makeLabel("Description", this.getDescription()));
		result.put("started", JsonData.makeLabel("Started", (this.timer.isStarted()) ? this.timer.getStartedWhen() : "Not started"));
		result.put("elapsed", JsonData.makeLabel("Elapsed", (this.timer.isStarted()) ? this.timer.elapsed().toString() : 0));
		result.put("executable", this.isExecutable());
		result.put("status", this.getStatus());
		result.put("hasReportOnly", this.hasReportOnly());
		return result;
	}

	@Override
	public boolean recordInLog()
	{
		return true;
	}

	@Override
	public boolean recordHistory()
	{
		return true;
	}

	@Override
	public boolean stop()
	{
		this.setStopping(true);
		return true;
	}

	@Override
	public boolean isExecutable()
	{
		return true;
	}

	@Override
	public synchronized boolean isStopping()
	{
		return this.stopping;
	}

	@Override
	public synchronized void setStopping(boolean stopping)
	{
		this.stopping = stopping;
	}

	@Override
	public boolean isRunning()
	{
		return this.getStatus()==IJob.Status.processing;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public IJob.Status getStatus()
	{
		return this.status;
	}

	@Override
	public void setStatus(IJob.Status status)
	{
		this.status=status;
	}

	@Override
	public ProcessStatus getProcessStatus()
	{
		return this.processStatus;
	}

	@Override
	public DateTime getLastRun()
	{
		if (null!=this.lastRun)
		{
			this.lastRun=DateTime.now();
		}
		return this.lastRun;
	}

	@Override
	public void setLastRun(DateTime lastRun)
	{
		DateTime dt=lastRun;
		if (null==dt)
		{
			dt=DateTime.now();
		}
		this.lastRun=dt;
	}

	@Override
	public boolean hasReportOnly()
	{
		return false;
	}

	/**
	 * The order in which to display the data.
	 *
	 * @return The ordinal.
	 */
	@Override
	public int getOrdinal()
	{
		return this.getData().getInteger(1000, "data", "value", "ordinal");
	}

	public Object getGroup()
	{
		String result=this.getData().getString("data", "value", "group");
		return StringX.isBlank(result) ? "Data" : result;
	}

	public boolean isAutomated()
	{
		return JobConfigurations.getInstance().isAutomated(this.getClass());
	}

	public JsonData getData()
	{
		JsonData result = JobConfigurations.getInstance().getJobData(this.getClass());
		if(null==result){
			result = JsonData.createObject();
		}
		return result;
	}

// Methods
public static synchronized boolean isRunning(Class... jobs){
	boolean result = false;
	JobsEngine engine = JobsEngine.getInstance();
	for(Class cls: jobs){
		try
		{
			if(ClassX.isKindOf(cls, BaseJob.class))
			{
				@SuppressWarnings("unchecked")
				JobWorker worker=engine.getWorker(cls);
				BaseJob job=worker.getJobItem().getJob(null);
				result=(null!=job) && job.isRunning();
				if (result)
				{
					break;
				}
			}
		}
		catch (Exception ignored){}
	}
	return result;
}

	@SuppressWarnings("ParameterHidesMemberVariable")
	public void begin(ProcessStatus processStatus, Object... args)
	{
		this.timer=new Stopwatch();
		this.timer.start();
		this.processStatus=processStatus;
		this.setStatus(IJob.Status.processing);
		this.start(args);
		try
		{
			this.close();
		}
		catch (IOException ignored)
		{
		}
		this.setStopping(false);
		this.setStatus(IJob.Status.idle);
	}

	@Override
	public void close()
		throws IOException
	{
		this.stop();
		this.timer.stop();
	}

	public File getFile(String... names)
	{
		String path=String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files");
		path=StringX.replace(path, "//", "/");
		DateTime asOf=DateTime.now();
		String name=String.format("%d_%d_%d_%d_%d_%d.csv", asOf.getYear(), asOf.getMonthOfYear(), asOf.getDayOfMonth(), asOf.getHourOfDay(), asOf.getMinuteOfHour(), asOf.getSecondOfMinute());
		if (null!=names)
		{
			String v="";
			for (String n : names)
			{
				if (!v.isEmpty())
				{
					v=String.format("%s_", v);
				}
				v=String.format("%s%s", v, n);
			}

			name=String.format("%s_%s", v, name);
		}
		return new File(path, name);
	}

	// Getters and setters
	@SuppressWarnings("unused")
	public CSVUtils getCsvUtils()
	{
		return this.csvUtils;
	}

	public Stopwatch getTimer()
	{
		return this.timer;
	}

	public int getMaxThreads()
	{
		return this.getData().getInteger(1, 4, "maxThreads", "value");
	}
}
