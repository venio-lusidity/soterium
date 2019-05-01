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
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.jobs.BaseJob;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.lang.reflect.Constructor;
import java.util.Set;

@AtWebResource(pathTemplate = "/svc/jobs/info", matchingMode = AtWebResource.MODE_BEST_MATCH,
        methods = "get", description = "Available jobs to run.")
@AtAuthorization(required = true)
public class JobsInfoServerResource extends BaseServerResource {
	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
        JsonData results = JsonData.createArray();
        result.put("results", results);

	    BasePrincipal principal = this.getUserCredentials().getPrincipal();
	    if((null!=principal) && Group.isInGroup(principal, "admin"))
	    {
		    Set<Class<? extends BaseJob>> types =Environment.getInstance().getReflections().getSubTypesOf(BaseJob.class);

		    for(Class<? extends BaseJob> type: types){
			    JsonData item = JsonData.createObject();
			    try
			    {
				    Constructor constructor=type.getConstructor(ProcessStatus.class);
				    BaseJob job =(BaseJob) constructor.newInstance(new ProcessStatus());
				    item.put("title", job.getTitle());
				    item.put("desc", job.getDescription());
				    results.put(item);
			    }
			    catch (Exception ex){
				    item.put("error", ex.getMessage());
			    }
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
