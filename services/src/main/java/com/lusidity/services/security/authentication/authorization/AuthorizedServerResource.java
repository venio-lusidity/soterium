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

package com.lusidity.services.security.authentication.authorization;

import com.lusidity.Environment;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.people.Person;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtAuthorization(required = true)
@AtWebResource(pathTemplate = "/svc/authorization/authorized", methods = "get", description = "Authenticates a client.")
public class AuthorizedServerResource extends BaseServerResource
{
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
	public Representation update(Representation representation) {
		JsonData result = new JsonData();
		boolean authorized = false;
		JsonData data = this.getJsonContent();
		if(null!=data){
			JsonData groups = data.getFromPath("groups");
			authorized=!Environment.getInstance().getWebServer().credentialsRequired();
			if(!authorized && (null!=groups) && groups.isJSONArray()){
				BasePrincipal principal = this.getUserCredentials().getPrincipal();
				if(principal instanceof Person){
					Person person = (Person) principal;
					for(Object o: groups){
						if(o instanceof String){
							Group group = VertexFactory.getInstance().getByTitle(Group.class, (String) o);
							if(null!=group){
								authorized = Group.isInGroupOrChild(person, group);
								if(authorized){
									break;
								}
							}
						}
					}
				}
			}
		}
		if(!authorized)
		{
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
		result.put("authorized", authorized);
		
		return this.getRepresentation(result);
	}
}
