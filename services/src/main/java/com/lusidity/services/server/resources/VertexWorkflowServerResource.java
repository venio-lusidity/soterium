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
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.process.workflow.WorkflowItem;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.Set;

@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/workflows", methods = "delete,get,post", description = "Delete, Get, " +
    "Create or Update a vertex.", matchingMode = AtWebResource.MODE_FIRST_MATCH)
@AtAuthorization()
public class VertexWorkflowServerResource
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

        String url = null;
        try {
            url = this.getRequest().getOriginalRef().toString();
            String webId = this.getAttribute("webId");

            DataVertex vertex = this.getVertex(webId);
            if (null != vertex)
            {
                if(this.authorized(vertex, Permission.Types.read)) {
                    JsonData results = JsonData.createArray();
                    Set<Class<? extends WorkflowItem>> subs =Environment.getInstance().getReflections().getSubTypesOf(WorkflowItem.class);
                    for(Class<? extends WorkflowItem> sub: subs){
                        BaseQueryBuilder qb = Environment.getInstance().getIndexStore()
                                                         .getQueryBuilder(Edge.class, sub, 0, 0);
                        qb.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, "/electronic/base_infrastructure/infrastructures");
                        qb.filter(BaseQueryBuilder.Operators.must, "toEndpoint", BaseQueryBuilder.StringTypes.raw, vertex.fetchId().getValue());
                        QueryResults queryResults = qb.execute();
                        for(IQueryResult queryResult: queryResults){
                            DataVertex v = queryResult.getOtherEnd(vertex.fetchId().getValue());
                            results.put(v.toJson(false));
                        }
                    }
                    result.put("results", results);

                }
                else {
                    this.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            }
            else
            {
                this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }
        catch (Exception e)
        {
            Environment.getInstance().getReportHandler().severe(
                    "Exception processing GET %s: %s", (!StringX.isBlank(url)) ? url : "unknown URI", e.toString()
            );
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public
    Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public
    Representation update(Representation representation)
    {
        return null;
    }
}
