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

import com.lusidity.configuration.JobConfigurations;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.DateTimeX;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.lang.reflect.Constructor;
import java.util.Objects;

@SuppressWarnings({
	"EqualsAndHashcode",
	"ClassNamingConvention"
})
public class JobItem
{
	// Fields
	public static final int DEFAULT_HOUR=23;
	public static final int DEFAULT_MINUTE=50;
	private static final int DEFAULT_DELAY=500;
	private static final int DEFAULT_IDLE_THRESHOLD=600000;
	private final Class<? extends IJob> type;
	private String description=null;
	private String title=null;
	private DateTime lastRun=DateTime.now();
	private boolean executable=true;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private BaseJob job=null;
	private Object[] args = null;

	// Constructors
	public JobItem(Class<? extends IJob> type, ProcessStatus processStatus)
		throws Exception
	{
		super();
		this.type=type;
		this.load(processStatus);
	}

	private void load(ProcessStatus processStatus)
		throws Exception
	{
		IJob jb=this.getJob(processStatus);
		this.description=jb.getDescription();
		this.title=jb.getTitle();
		this.executable=jb.isExecutable();
		this.nullify(true);
	}

	public synchronized BaseJob getJob(ProcessStatus processStatus)
		throws Exception
	{
		if (null==this.job)
		{
			@SuppressWarnings("JavaReflectionMemberAccess")
			Constructor constructor=this.type.getConstructor(ProcessStatus.class);
			BaseJob jb=((BaseJob) constructor.newInstance(processStatus));
			jb.setLastRun(this.getLastRun());
			this.setJob(jb);
		}
		return this.job;
	}

	public synchronized void nullify(boolean nullifyLastRun)
	{
		this.args=null;
		if (null!=this.job)
		{
			try
			{
				this.job.close();
			}
			catch (Exception ignored){}
		}
		this.setJob(null);
		if(nullifyLastRun)
		{
			this.setLastRun(null);
		}
	}

	public DateTime getLastRun()
	{
		return this.lastRun;
	}

	public void setLastRun(DateTime lastRun)
	{
		this.lastRun=lastRun;
	}

	public synchronized void setJob(BaseJob job)
	{
		this.job=job;
	}

	// Overrides
	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if (obj instanceof JobItem)
		{
			result=Objects.equals(this.getType(), ((JobItem) obj).getType());
		}
		return result;
	}

	public Class<? extends IJob> getType()
	{
		return this.type;
	}

	public void updateTimeOfDay(String timeOfDay)
	{
		boolean changed = JobConfigurations.getInstance().updateTimeOfDay(this.getType(), timeOfDay);
		if(changed){
			LocalTime lt = LocalTime.parse(timeOfDay);
			LocalTime now = LocalTime.now();
			if(lt.isBefore(now)){
				DateTime dateTime = DateTime.now().withTime(lt);
				this.setLastRun(dateTime);
			}
			else{
				this.setLastRun(null);
			}
		}
	}

	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();
		result.put("title", JsonData.makeLabel("Title", this.getTitle()));
		result.put("description", JsonData.makeLabel("Description", this.getDescription()));
		result.put("automated", JsonData.makeLabel("Automated", this.isAutomated()));
		result.put("executable", this.executable);
		result.put("lastRun", JsonData.makeLabel("Last Run", this.getLastRun()));
		result.put("type", this.getType());
		BaseJob temp=null;
		if (null==this.job)
		{
			try
			{
				@SuppressWarnings("JavaReflectionMemberAccess")
				Constructor constructor=this.getType().getConstructor(ProcessStatus.class);
				temp=(BaseJob) constructor.newInstance(new ProcessStatus());
				result.put("reportOnly", temp.hasReportOnly());
			}
			catch (Exception ignored)
			{
			}
		}
		else
		{
			temp = this.job;
			result.put("reportOnly", this.job.hasReportOnly());
		}
		String group=temp.getGroup().toString();
		group=StringX.isBlank(group) ? "Data" : group;
		result.put("group", group);
		result.put("_ordinal", temp.getOrdinal(), true);
		result.put("_title", temp.getTitle(), true);
		String timeOfDay=JobConfigurations.getInstance().getTimeOfDay(this.getType());
		if (!StringX.isBlank(timeOfDay))
		{
			result.put("timeOfDay", JsonData.makeLabel("Runs every day on or after", timeOfDay));
		}
		return result;
	}

	public String getTitle()
	{
		return this.title;
	}

	public String getDescription()
	{
		return this.description;
	}

	public boolean isAutomated()
	{
		JsonData data=this.getJobData();
		return (null==data) ? false : data.getBoolean("automated", "value");
	}

	public JsonData getJobData()
	{
		return JobConfigurations.getInstance().getJobData(this.type);
	}

	// Getters and setters
	public boolean isAvailable()
	{
		return !BaseJob.isRunning(this.getType()) && this.isAutomated() && this.isElapsed();
	}

	public boolean isElapsed()
	{
		DateTime now=DateTime.now();
		// Checking for same day will prevent multiple executions.
		boolean sameDay=(null!=this.getLastRun()) && DateTimeX.isSameDay(this.getLastRun());
		// Only run if it is not the same day and the expected time of day has expired.
		String timeOfDay=JobConfigurations.getInstance().getTimeOfDay(this.type);
		return (StringX.equals(timeOfDay, "0")) || (((null==this.getLastRun()) || !sameDay) && DateTimeX.isSameOrAfterTimeOfDay(now, timeOfDay));
	}

	@SuppressWarnings("unused")
	public int getDelay()
	{
		Integer result=null;
		JsonData data=this.getJobData();
		if (null!=data)
		{
			result=data.getInteger("delay", "value");
		}
		return (null==result) ? JobItem.DEFAULT_DELAY : result;
	}

	@SuppressWarnings("unused")
	public int getIdleThreshold()
	{
		Integer result=null;
		JsonData data=this.getJobData();
		if (null!=data)
		{
			result=data.getInteger("idleThreshold", "value");
		}
		return (null==result) ? JobItem.DEFAULT_IDLE_THRESHOLD : result;
	}

	public boolean isExecutable()
	{
		return this.executable;
	}

	public Object[] getArgs()
	{
		return this.args;
	}

	public void setArgs(Object... args)
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.args=args;
	}
}
