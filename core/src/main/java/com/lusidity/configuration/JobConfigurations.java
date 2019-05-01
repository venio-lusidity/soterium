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

package com.lusidity.configuration;

import com.lusidity.Environment;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.jobs.IJob;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JobConfigurations extends BaseSoteriumConfiguration
{
	private static JobConfigurations instance=null;

	// Constructors
	public JobConfigurations(File file)
	{
		super(file);
		this.load();
		JobConfigurations.instance=this;
	}

	// Methods
	public static JobConfigurations getInstance()
	{
		return JobConfigurations.instance;
	}

	public String getTimeOfDay(Class<? extends IJob> cls)
	{
		String result=this.getData().getString("jobs", cls.getName(), "timeOfDay", "value");
		if (StringX.isBlank(result))
		{
			result=this.getDefaultTimeOfDay();
		}
		return result;
	}

	public String getDefaultTimeOfDay()
	{
		String result=this.getData().getString("defaultTimeOfDay", "value");
		return (StringX.isBlank(result)) ? "00:00" : result;
	}

	public boolean updateTimeOfDay(Class<? extends IJob> cls, String time){
		boolean result = false;
		try{
			if(DateTimeX.is24HourTime(time))
			{
				JsonData jd=this.getData().getFromPath("jobs", cls.getName(), "timeOfDay");
				if (null!=jd)
				{
					jd.update("value", time);
					this.getData().save(this.getFile(), true);
					result = true;
 				}
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	public String getCategory(Class<? extends IJob> cls)
	{
		String result=this.getData().getString("category", "value");
		return (StringX.isBlank(result)) ? "daily" : result;
	}

	public boolean isAutomated(Class<? extends IJob> cls)
	{
		JsonData clsData=this.getJobData(cls);
		return (null==clsData) ? false : clsData.getBoolean("automated", "value");
	}

	public JsonData getJobData(Class<? extends IJob> cls)
	{
		this.checkAndReload();
		return this.getData().getFromPath("jobs").getFromPath(cls.getName());
	}

	// Getters and setters
	public List<Class<? extends IJob>> getJobs()
	{
		List<Class<? extends IJob>> results=new ArrayList<>();
		Collection<String> keys=this.getData().getFromPath("jobs").keys();
		for (String key : keys)
		{
			if (!StringX.isBlank(key))
			{
				try
				{
					Class cls=Class.forName(key);
					if ((null!=cls) && ClassX.isKindOf(cls, IJob.class))
					{
						//noinspection unchecked
						results.add(cls);
					}
				}
				catch (Exception ignored)
				{
				}
			}
		}
		return results;
	}
}
