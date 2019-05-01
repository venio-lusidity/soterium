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
import com.lusidity.console.Command;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtAuthorization()
@AtWebResource(pathTemplate = "/svc/admin/command",
        methods = "get", description = "Can execute an internal command if it is web enabled.")
public class CommandServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        return null;
    }

    @Override
    public Representation store(Representation representation)
    {
       return null;
    }

    @Override
    public Representation update(Representation representation)
    {
        JsonData result = null;

        try
        {
            if(!this.getUserCredentials().getPrincipal().isAdmin(false))
            {
                this.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
            else
            {
                JsonData content=this.getJsonContent();
                if ((null!=content) && content.isJSONObject())
                {
                    String command=content.getString("command");
                    JsonData params=content.getFromPath("params");
                    Command cmd=Environment.getInstance().getWebCommand(command);

                    if ((null!=cmd) && (null!=params) && params.isJSONObject())
                    {
                        UserActivity.logActivity(String.format("%s\n\r", command, params.toString()), this.getUserCredentials(), LogEntry.OperationTypes.post, this.getRequest().getOriginalRef(), true);

                        result=cmd.execute(params, this.getUserCredentials().getPrincipal());

                        if (null==result)
                        {
                            this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
                        }
                        else
                        {
                            this.getResponse().setStatus(Status.SUCCESS_OK);
                        }
                    }
                    else
                    {
                        result=JsonData.createObject();
                        result.put("error", "Either the command does not exist, the command is not web enabled or there is no instance running.");
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                }
            }
        }
        catch (Exception ignored) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (null!=result) ? result.toJsonRepresentation() : null;
    }
}
