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

import com.lusidity.core.ElementAction;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/actions", methods = "get", description = "Retrieve actions for a vertex like buy, read, watch, etc...")
@AtAuthorization()
public class ActionsServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Get
    public Representation retrieve() {
        /*
        JsonRepresentation result = null;
        try {
            String webId = (String) this.getRequestAttributes().get("webId");
            webId = StringX.urlDecode(String.format(DataVertex.URI_FORMAT, webId), TextEncoding.UTF8);

            Entity entity = ElementFactory.getFactory().get(webId, Entity.class);

            if (null != entity) {
                if(entity.getActions().isEmpty()){
                    ActionTask.getActions(entity);
                    entity.reload();
                }
                result = !entity.getActions().isEmpty() ?
                        ActionsServerResource.buildJsonResponse(entity.getActions()).toJsonRepresentation() : null;
            }
            else
            {
                this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }
        catch (Exception ignore) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        if(null == result)
        {
            this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        }
        */
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
        return null;
    }

    private static JsonData buildJsonResponse(Iterable<ElementAction> actions) {
        JsonData results = JsonData.createObject();

        for (ElementAction action : actions) {
            JsonData item = action.toJson(false);
            if(!results.hasKey(action.getActionType())){
                results.put(action.getActionType(), JsonData.createArray());
            }
            results.put(action.getActionType().toString(), item);
        }

        return results;
    }
}
