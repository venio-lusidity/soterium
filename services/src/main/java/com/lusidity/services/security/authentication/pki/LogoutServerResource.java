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

package com.lusidity.services.security.authentication.pki;

import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.Response;
import org.restlet.representation.Representation;

@SuppressWarnings("UnusedDeclaration")
@AtAuthorization(required = false, anonymous = true)
@AtWebResource(pathTemplate = "/svc/pki/logout", methods = "get", description = "Authenticates a client.")
public class LogoutServerResource extends BaseServerResource{

	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve()
    {
        JsonData result = this.logout();

        //https://www.w3.org/TR/clear-site-data/
	    JsonData clear = JsonData.createObject()
	                             .put("types", JsonData.createArray()
	                                                   .put("cache")
	                                                   .put("storage").put("executionContexts"));
	    Response response = this.getResponse();
	    response.getHeaders().add("Clear-Site-Data", clear.toString());
        return this.getRepresentation(result);
    }

    private
    JsonData logout()
    {
        JsonData result = new JsonData();
        boolean loggedOut = false;
        if ((null!=this.getClientInfo()) && this.getClientInfo().isAuthenticated() && (null!=this.getClientInfo().getUser()))
        {
            // TODO: need to log the logout process and somehow maintain that
	        // the user logged out so that the next login can be recorded even if the time hasn't expired.
        }
        result.put("loggedOut", loggedOut);
        return result;
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
