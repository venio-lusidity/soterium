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

import com.lusidity.data.ApolloVertex;
import com.lusidity.domains.process.workflow.Workflow;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.representation.Representation;

import java.util.Collection;
import java.util.List;

@AtWebResource(pathTemplate = "/svc/workflow/workflows", methods = "get",
    description = "Returns a list of available workflows")
@AtAuthorization()
public class WorkflowsServerResource
        extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
        JsonData items = JsonData.createArray();
        Collection<Workflow> workflows = VertexFactory.getInstance().getAll(Workflow.class, this.getStart(), this.getLimit());

        List<Workflow> list = ApolloVertex.sortTitle(workflows, true);

        for(Workflow workflow: list){
            JsonData item = workflow.toJson(false);
            this.handleWebResource(workflow, null, item, null);
            items.put(item);
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
