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
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.electronic.Notification;
import com.lusidity.domains.electronic.Notifications;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/notifications", methods = "delete,get,post", description = "Delete, Get, " +
    "Update a notification.", matchingMode = AtWebResource.MODE_BEST_MATCH)
@AtAuthorization()
public class NotificationServerResource
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
        try {
            if ((null!=this.getUserCredentials()) && this.getUserCredentials().isAuthenticated() && (null!=this.getUserCredentials().getPrincipal()))
            {
                BasePrincipal principal = this.getUserCredentials().getPrincipal();
                Notifications notifications = new Notifications(principal, this.getStart(), this.getLimit());
                result = notifications.toJson();
                if(null==result){
                    result = JsonData.createObject();
                    result.put("hits", 0);
                    result.put("results", JsonData.createArray());
                    this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
                }
            }
            else
            {
                this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }
        catch (Exception e)
        {
            Environment.getInstance().getReportHandler().severe(e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public Representation update(Representation representation) {
        JsonData result = JsonData.createObject();
        String url = null;
        try {
            url = this.getRequest().getOriginalRef().toString();
            if ((null!=this.getUserCredentials()) && this.getUserCredentials().isAuthenticated() && (null!=this.getUserCredentials().getPrincipal()))
            {
                Class<? extends DataVertex> store=BaseDomain.getDomainType("/electronic/notification");
                JsonData data=this.getJsonContent();
                if (data.isValid() && data.hasKey("id"))
                {
                    DataVertex vertex = this.getVertex(store, data.getString("id"));
                    if(null != vertex){
                        Notification notification = (Notification)vertex;
                        notification.fetchRead().setValue(true);
                        result.put("updated", notification.save());
                    }
                }
            }
            else
            {
                this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            }
        }
        catch (Exception e)
        {
            Environment.getInstance().getReportHandler().severe(
                "Exception processing UPDATE %s: %s", StringX.isBlank(url) ? "unknown URI" : url, e.toString()
            );
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }
}
