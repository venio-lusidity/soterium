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

package com.lusidity.services.server.resources;


import com.lusidity.Environment;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.system.assistant.worker.JobWorker;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.jobs.BaseJob;
import com.lusidity.jobs.JobItem;
import com.lusidity.jobs.JobsEngine;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.Set;

@AtWebResource(pathTemplate = "/svc/jobs", methods = "get", description = "Retrieve a file by URL.")
@AtAuthorization()
public class JobStatusServerResource
        extends BaseServerResource {
	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve() {
    	JsonData result = JsonData.createObject();

    	BasePrincipal principal = this.getUserCredentials().getPrincipal();
	    if((null!=principal) && Group.isInGroup(principal, "admin"))
	    {

		    try{
			    JsonData results = JsonData.createArray();
			    result.put("results", results);
			    JobsEngine engine = JobsEngine.getInstance();
			    Set<Class<? extends BaseJob>> jobs =Environment.getInstance().getReflections().getSubTypesOf(BaseJob.class);
			    for(Class<? extends BaseJob> jobType: jobs){
				    JsonData item = JsonData.createObject();
				    try
				    {
					    JobWorker worker=engine.getWorker(jobType);
					    JobItem jobItem = worker.getJobItem();
					    item.put("title", jobItem.getTitle());
					    item.put("desc", jobItem.getDescription());
					    item.put("running", worker.isRunning());
					    item.put("started", worker.isStarted());
					    item.put("class", jobType.getName());
					    BaseJob job = jobItem.getJob(worker.getProcessStatus());
					    if(null!=job)
					    {
						    String group=job.getGroup().toString();
						    item.put("group", group);
						    item.put("_ordinal", job.getOrdinal(), true);
						    item.put("_title", job.getTitle(), true);
					    }
					    results.put(item);
				    }
				    catch (Exception ex){
					    item.put("error", ex.getMessage());
				    }
			    }
		    }
		    catch (Exception e)
		    {
			    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			    Environment.getInstance().getReportHandler().warning(e);
		    }
	    }
	    else{
		    this.getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
	    }

	    return this.getRepresentation(result);
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public Representation update(Representation representation)
    {
        return null;
    }
}

