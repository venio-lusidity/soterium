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
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.jobs.IJob;
import org.joda.time.DateTime;

@AtSchemaClass(name = "Job History", description = "Information about a job that has been completed.", discoverable = false)
public class JobHistory extends BaseDomain
{
	private KeyData<ProcessStatus> processStatus = null;
	private KeyData<Class> jobType = null;
	private KeyData<DateTime> startedWhen = null;
	private KeyData<DateTime> stoppedWhen = null;
	private KeyData<String> elapsed = null;
	private KeyData<String> server = null;
	private KeyData<IJob.Status> status = null;

	public JobHistory(){
		super();
	}

	public JobHistory(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	public KeyData<Class> fetchJobType(){

		if (null==this.jobType)
		{
			this.jobType=new KeyData<>(this, "jobType", Class.class, false, null);
		}
		return this.jobType;
	}

	public KeyData<ProcessStatus> fetchProcessStatus(){

		if (null==this.processStatus)
		{
			this.processStatus=new KeyData<>(this, "processStatus", ProcessStatus.class, false, null);
		}
		return this.processStatus;
	}

	public KeyData<DateTime> fetchStartedWhen()
	{
		if (null==this.startedWhen)
		{
			this.startedWhen=new KeyData<>(this, "startedWhen", DateTime.class, false, null);
		}
		return this.startedWhen;
	}

	public KeyData<DateTime> fetchStoppedWhen()
	{
		if (null==this.stoppedWhen)
		{
			this.stoppedWhen=new KeyData<>(this, "stoppedWhen", DateTime.class, false, null);
		}
		return this.stoppedWhen;
	}

	public KeyData<String> fetchedElapsed()
	{
		if (null==this.elapsed)
		{
			this.elapsed=new KeyData<>(this, "elapsed", String.class, false, null);
		}
		return this.elapsed;
	}

	public KeyData<String> fetchServer()
	{
		if (null==this.server)
		{
			this.server=new KeyData<>(this, "server", String.class, false, Environment.getInstance().getConfig().getServerName());
		}
		return this.server;
	}

	public KeyData<IJob.Status> fetchStatus(){
		if(null==this.status){
			this.status = new KeyData<>(this, "status", IJob.Status.class, false, null);
		}
		return this.status;
	}


	public static JobHistory create(String title, Class cls, Stopwatch stopwatch, ProcessStatus processStatus, IJob.Status status)
	{
		JobHistory result = new JobHistory();
		result.fetchTitle().setValue(title);
		result.fetchServer().setValue(Environment.getInstance().getConfig().getServerName());
		result.fetchJobType().setValue(cls);
		result.fetchProcessStatus().setValue(processStatus);
		result.fetchStartedWhen().setValue(stopwatch.getStartedWhen());
		result.fetchStoppedWhen().setValue(stopwatch.getStoppedWhen());
		result.fetchedElapsed().setValue(stopwatch.elapsed().toString());
		result.fetchStatus().setValue(status);
		return result;
	}

	public JsonData getReport(){
		JsonData result = this.toJson(false);

		JsonData event=JsonData.createObject();
		event.put("started", JsonData.makeLabel("Started", this.fetchStartedWhen().getValue()));
		event.put("finished", JsonData.makeLabel("Finished", this.fetchStoppedWhen().getValue()));
		event.put("elapsed", JsonData.makeLabel("Elapsed", this.fetchedElapsed().getValue()));
		result.put("event", event);

		return result;
	}
}
