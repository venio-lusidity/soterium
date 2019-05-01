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
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.Map;
import java.util.Set;

@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/edge/{edgeId}", methods = "get", description = "Retrieve and edge between two vertices.")
@AtAuthorization()
public class EdgeServerResource extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
        JsonData result = null;
        try {
            Map<String, Object> attributes = this.getRequest().getAttributes();
            String vertexId = String.valueOf(attributes.get("webId"));
            String edgeId = String.valueOf(attributes.get("edgeId"));

            Set<Class<? extends Edge>> subTypesOf = Environment.getInstance().getReflections().getSubTypesOf(Edge.class);
            for(Class<? extends Edge> subType: subTypesOf) {
                Edge edge = VertexFactory.getInstance().get(subType, edgeId);
                if (null != edge) {
                    Endpoint endpoint = edge.getOther(vertexId);
                    DataVertex vertex = endpoint.getVertex();
                    JsonData dso = vertex.getVertexData();
                    if (dso.hasKey("elementType") && dso.getString("elementType").equals("/object/entity")) {
                        if (this.authorized(vertex, Permission.Types.read)) {
                            result = vertex.toJson(false);
                        } else {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        }
                    } else {
                        result = vertex.toJson(false);
                    }
                    break;
                }
            }
        }
        catch(Exception ignored)
        {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (null!=result) ? result.toJsonRepresentation() : null;
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
