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
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.system.assistant.worker.JobHistory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/jobs/history", matchingMode = AtWebResource.MODE_BEST_MATCH,
        methods = "get", description = "Available jobs to run.")
@AtAuthorization(required = true)
public class JobsHistoryServerResource extends BaseServerResource {
	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
	    BasePrincipal principal = this.getUserCredentials().getPrincipal();
	    if((null!=principal) && Group.isInGroup(principal, "admin"))
        {
	        BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(JobHistory.class, JobHistory.class, this.getStart(), this.getLimit());
	        qb.sort("createdWhen", BaseQueryBuilder.Sort.desc);
	        if((null != this.getParameter("server"))) {
		        qb.filter(BaseQueryBuilder.Operators.must, "server", BaseQueryBuilder.StringTypes.folded, this.getParameter("server"));  		        
	        }
	        else{
		        qb.matchAll();
	        }
	        QueryResults qrs=qb.execute();
	        result = qrs.toJson(qb, null, QueryResults.Format._default, null);
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
