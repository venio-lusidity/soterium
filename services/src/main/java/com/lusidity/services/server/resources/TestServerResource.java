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

import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/test", methods = "delete,get,post",
    description = "Delete, Get, Create or Update a vertex.")
@AtAuthorization()
public class TestServerResource
        extends BaseServerResource {

    @Override
    public Representation retrieve() {
        JsonData result = JsonData.createObject();

        try {

        }
        catch (Exception ex){
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation store(Representation representation)
    {
        JsonData result = JsonData.createObject();

        try {

        }
        catch (Exception ex){
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation remove() {
        JsonData result = JsonData.createObject();

        try {

        }
        catch (Exception ex){
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation update(Representation representation) {
        JsonData result = JsonData.createObject();

        try {

        }
        catch (Exception ex){
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }
}
