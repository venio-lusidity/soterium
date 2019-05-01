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

import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.workers.workflow.IWorkflowItem;
import com.lusidity.workers.workflow.WorkflowEngine;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import java.util.Collection;

@AtWebResource(pathTemplate = "/svc/workflow/types", methods = "get",
    description = "Returns a list of available workflow types")
@AtAuthorization()
public class WorkflowTypesServerResource
        extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Get
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
        JsonData items = JsonData.createArray();
        Collection<Class<? extends IWorkflowItem>> workflowTypes = WorkflowEngine.getInstance().getWorkflowTypes();
        for(Class<? extends IWorkflowItem> workflowType: workflowTypes){
            AtSchemaClass asc = workflowType.getAnnotation(AtSchemaClass.class);
            if(null!=asc && !StringX.isBlank(asc.name()))
            {
                JsonData item = JsonData.createObject();
                item.put("title", asc.name());
                item.put("value", workflowType.getName());
                item.put("description", asc.description());
                items.put(item);
            }
        }
        result.put("results", items);
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
