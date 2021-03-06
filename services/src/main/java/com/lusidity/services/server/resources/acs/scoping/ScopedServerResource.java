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

package com.lusidity.services.server.resources.acs.scoping;

import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePosition;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtAuthorization()
@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/acsscoped", methods = "get", description = "ACS scoped positions")
public class ScopedServerResource extends BaseServerResource
{
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
	    JsonData result=JsonData.createObject();

	    BasePrincipal principal=this.getUserCredentials().getPrincipal();
	    if((null!=principal) && Group.isInGroup(principal, "admin"))
	    {
		    String webId=this.getAttribute("webId");

		    Class<? extends DataVertex> store=BaseDomain.getDomainType(this.getAttribute("domain"));
		    DataVertex vertex=this.getVertex(store, webId);

		    if ((null!=vertex) && ClassX.isKindOf(vertex, BasePosition.class))
		    {
			    BasePosition position=(BasePosition) vertex;
			    result=this.getScoped(position);
		    }
		    else
		    {
			    this.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
		    }
	    }
	    return this.getRepresentation(result);
    }

	private JsonData getScoped(BasePosition position)
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
        return null;
    }
}
