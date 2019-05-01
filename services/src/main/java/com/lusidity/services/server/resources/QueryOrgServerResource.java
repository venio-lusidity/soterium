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
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.object.edge.OrganizationEdge;
import com.lusidity.domains.organization.Organization;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.exceptions.QueryBuilderException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.helper.QueryHelper;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.security.InvalidParameterException;

@SuppressWarnings({
    "UnusedDeclaration",
    "Duplicates"
})
@AtWebResource(pathTemplate = "/svc/query/org", methods = "post", description = "A query used to retrieve unrestricted organizations.")
@AtAuthorization(required = false)
public class QueryOrgServerResource
        extends BaseServerResource {
	// Overrides
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
        JsonData result;
        try {
            int start = this.getStart();
            int limit = this.getLimit();
            JsonData data = this.getJsonContent();
            String domain = data.getString("domain");
            Class<? extends DataVertex> index = Environment.getInstance().getApolloVertexType(domain);
            if(ClassX.isKindOf(index, Organization.class) || ClassX.isKindOf(index, OrganizationEdge.class))
            {
                result=this.getResults(start, limit, data);
            }
            else{
                //noinspection ThrowCaughtLocally
                throw new InvalidParameterException("The domain must be of type Organization.");
            }
        } catch (QueryBuilderException e) {
            result = JsonData.createObject();
            result.put("error", e.getMessage());
            result.put("internalError", e.isInternalError());
            this.getResponse().setStatus(e.isInternalError() ? Status.SERVER_ERROR_INTERNAL: Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
        catch (Exception e)
        {
            result = JsonData.createObject();
            result.put("error", e.getMessage());
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return this.getRepresentation(result);
    }

    private JsonData getResults(int start, int limit, JsonData data) throws Exception {
        JsonData result;
        try {
            QueryHelper queryHelper= new QueryHelper(SystemCredentials.getInstance(), false, data, start, limit);
            result = queryHelper.execute(null);
            if (result.hasKey("error")) {
                this.setStatus(result.getBoolean("clientError") ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, result.getString("error"));
            }
        } catch (QueryBuilderException e) {
            result = JsonData.createObject();
            result.put("error", e.getMessage());
            result.put("internalError", e.isInternalError());
            this.getResponse().setStatus(e.isInternalError() ? Status.SERVER_ERROR_INTERNAL: Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
        return result;
    }
}
