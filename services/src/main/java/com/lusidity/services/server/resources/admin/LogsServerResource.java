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
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.system.logging.LoggerX;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtAuthorization()
@AtWebResource(pathTemplate = "/svc/admin/logs",
        methods = "get", description = "Retrieve the logs for Athena.")
public class LogsServerResource
        extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        JsonData results = null;
        try {
            if(!this.getUserCredentials().getPrincipal().isAdmin(false))
            {
                this.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
            else
            {
                int limit=this.getLimit();
                int start=this.getStart();

                LoggerX logger=Environment.getInstance().getReportHandler().getLogger();
                results=logger.getLog(start, limit);
            }
        } catch (Exception e) {
            results = JsonData.createArray();
            this.setStatus(org.restlet.data.Status.SERVER_ERROR_INTERNAL);
        }
        return this.getRepresentation(results);
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
