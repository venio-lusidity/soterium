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

import com.lusidity.Environment;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.WebServerConfig;
import com.lusidity.services.server.WebServices;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.representation.Representation;

@SuppressWarnings("UnusedDeclaration")
@AtAuthorization(required = false)
@AtWebResource(pathTemplate = "/svc/pki/login", methods = "get", description = "Authenticates a client.")
public class LoginServerResource extends BaseServerResource{

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        JsonData result = this.authenticate();
        return this.getRepresentation(result);
    }

    private
    JsonData authenticate()
    {
        JsonData result = null;
        if(WebServices.getInstance().getConfiguration().getAuthMode()==WebServerConfig.AuthMode.none){
            result = new JsonData();
            result.put("firstName", "System");
            result.put("lastName", "System");
            result.put("middleName", "System");
            result.put("authenticated", true);
            result.put("validated", true);
            result.put("registered", true);
            result.put("principalUri", null);
        }
        else if ((null!=this.getClientInfo()) && this.getClientInfo().isAuthenticated() && (null!=this.getClientInfo().getUser())) {

        	boolean test = false;
            if(Environment.getInstance().getDataStore().isOffline()){
                result.put("connected", false);
            }
            else
            {
                PKICredentials credentials=new PKICredentials(this.getRequest(), this.getParameter("apiKey"));
                result=credentials.toJson();
                credentials.log();

                // TODO: log failed attempts even if registered and not approved.
            }

            if(test){
	            result.update("authenticated", false);
	            result.update("validated", true);
	            result.update("registered", false);
	            result.remove("roles");
            }
            result.remove("identifier");

        }
        if(null!=result)
        {
            result.put("connected", true);
        }
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
