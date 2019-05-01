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
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/ping", methods = "get", description = "Gives a response of pong and/or other information if available.")
@AtAuthorization(required = false, anonymous = true)
public class PingServerResource
        extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
        result.put("ping", "pong");
        try
        {
            if (Environment.getInstance().isDebugMode())
            {
                Environment.getInstance().getReportHandler().info("PingServerResource called.");
                if (null==this.getClientInfo())
                {
                    Environment.getInstance().getReportHandler().info("PingServerResource client info is null.");
                    if (null==this.getClientInfo().getUser())
                    {
                        Environment.getInstance().getReportHandler().info("PingServerResource client info user is null.");
                    }
                }
            }
            if ((null!=this.getClientInfo()) && (null!=this.getClientInfo().getUser()))
            {

                result.put("user", this.getClientInfo().getUser());
                result.put("authenticated", this.getClientInfo().isAuthenticated());
                result.put("online", !Environment.getInstance().getDataStore().isOffline() || Environment.getInstance().getDataStore().verifyConnection());
                if (Environment.getInstance().isDebugMode())
                {
                    Environment.getInstance().getReportHandler().info("PingServerResource: client info is available validated.");
                }
            }
        }
        catch (Exception ex){
            if (Environment.getInstance().isDebugMode())
            {
                result.put("error", ex.getMessage());
                Environment.getInstance().getReportHandler().info("PingServerResource: %s.", ex.getMessage());
            }
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            Environment.getInstance().getReportHandler().warning(ex);
        }
        if (Environment.getInstance().isDebugMode())
        {
            Environment.getInstance().getReportHandler().info("PingServerResource: %s.", result.toString());
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
