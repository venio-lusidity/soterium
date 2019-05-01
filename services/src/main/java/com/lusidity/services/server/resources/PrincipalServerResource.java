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
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.*;

@AtWebResource(pathTemplate = "/svc/acs/security/principals", methods = "get",
    description = "Returns a list of available workflows")
@AtAuthorization()
public class PrincipalServerResource
        extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
        JsonData result = JsonData.createObject();
        try
        {
            Class<? extends DataVertex> cls = null;
            String query = this.getParameter("query");
            String domain = this.getParameter("type");

            Collection<BasePrincipal> principals = null;

            if (!StringX.isBlank(domain))
            {
                cls = BaseDomain.getDomainType(domain);
            }

            if (!StringX.isBlank(query))
            {
                principals = this.doQuery(query);
            }
            else if (null!=cls)
            {
                principals = VertexFactory.getInstance().getAll(cls, this.getStart(), this.getLimit());
            }
            else
            {
                principals = this.getAll();
            }

            @SuppressWarnings("unchecked")
            List<BasePrincipal> sorted = new ArrayList<>(principals);

            Collections.sort(sorted);

            for (BasePrincipal bp : sorted)
            {
                String vt = bp.fetchVertexType().getValue();
                JsonData item = bp.toJson(false);
                result.put(vt, item);
            }
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().severe(ex);
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
    public Representation update(Representation representation)
    {
        return null;
    }

    private Collection<BasePrincipal> doQuery(String query)
    {
        Collection<BasePrincipal> results = new ArrayList<>();
        Set<Class<? extends BasePrincipal>> subTypes = Environment.getInstance().getReflections().getSubTypesOf
            (BasePrincipal.class);

        if(null!=subTypes){
            for(Class<? extends BasePrincipal> subType: subTypes){
                Collection<BasePrincipal> items = VertexFactory.getInstance().startsWith(subType, query, this
                    .getStart(), this.getLimit());
                if(null!=items){
                    CollectionX.addAllIfUnique(results, items);
                }
            }
        }

        return results;
    }

    private Collection<BasePrincipal> getAll()
    {
        Collection<BasePrincipal> results = new ArrayList<>();
        Set<Class<? extends BasePrincipal>> subTypes = Environment.getInstance().getReflections().getSubTypesOf
            (BasePrincipal.class);

        if(null!=subTypes){
            for(Class<? extends BasePrincipal> subType: subTypes){
                Collection<BasePrincipal> items = VertexFactory.getInstance().getAll(subType, this.getStart(), this.getLimit());
                if(null!=items){
                    CollectionX.addAllIfUnique(results, items);
                }
            }
        }

        return results;
    }
}
