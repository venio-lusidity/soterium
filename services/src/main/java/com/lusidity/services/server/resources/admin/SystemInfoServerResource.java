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

package com.lusidity.services.server.resources.admin;

import com.lusidity.Environment;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.system.apollo.ApolloStatistics;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import com.lusidity.system.SystemInfo;
import com.lusidity.system.security.UserCredentials;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

@AtAuthorization()
@AtWebResource(pathTemplate = "/svc/admin/sysinfo",
        methods = "get", description = "Returns information about Athena.")
public class SystemInfoServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @SuppressWarnings("AccessOfSystemProperties")
    @Override
    public Representation retrieve() {
        JsonData result = null;
        UserCredentials credentials = this.getUserCredentials();
        if(null==credentials){
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
        BasePrincipal principal = credentials.getPrincipal();
        if(null==principal){
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        if(!principal.isAdmin(false))
        {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
        }

	    try
	    {
		    result=SystemInfo.toJsonData();
		    ApolloStatistics statistics = Environment.getInstance().getDataStore().isStatisticsAvailable() ? Environment.getInstance().getDataStore().getStatistics(false) : new ApolloStatistics();
		    result.put("ds_stats", this.statistics(statistics));
	    }
	    catch (Exception ignored)
	    {
		    throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }

        return (null!=result) ? result.toJsonRepresentation() : null;
    }

    private JsonData statistics(ApolloStatistics statistics){
	    JsonData result = JsonData.createObject();
	    result.put("create", SystemInfo.create(statistics.fetchCreated().getValue().getCount(), "Created"));
	    result.put("delete", SystemInfo.create(statistics.fetchDeleted().getValue().getCount(), "Deleted"));
	    result.put("queries", SystemInfo.create(statistics.fetchQueried().getValue().getCount(), "Queried"));
	    result.put("update", SystemInfo.create(statistics.fetchUpdated().getValue().getCount(), "Updated"));
	    if(!statistics.hasId()){
	    	result.update("available", false);
	    }
	    return result;
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
