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

import com.lusidity.data.DataVertex;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/merge/domains/{domain2}/{webId2}", methods = "get", description = "Merge two vertices.")
@AtAuthorization()
public class MergeServerResource
        extends BaseServerResource {
	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve(){
    	JsonData result = null;
    	if(this.getUserCredentials().getPrincipal().isAdmin(true))
	    {
		    DataVertex vertex=this.getVertex();
		    DataVertex vertex2=this.getVertex2();
		    if ((null!=vertex) && (null!=vertex2))
		    {
			    DataVertex fVertex = vertex2.mergeTo(vertex, true);
			    result = fVertex.toJson(false);
		    }
		    else
		    {
			    this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		    }
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
}
