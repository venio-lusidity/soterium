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

package com.lusidity.services.server.resources.admin;

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@SuppressWarnings("UnusedDeclaration")
@AtAuthorization()
@AtWebResource(pathTemplate = "/svc/admin/api_key", methods = "get", optionalParams = "provider, identifier", description = "Generate an API key.")
public class ApiKeyServerResource extends BaseServerResource{

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        JsonData result = null;
        BasePrincipal principal = this.getUserCredentials().getPrincipal();
        if((null!=principal) && this.getUserCredentials().getPrincipal().isAdmin(true))
        {
            String provider=this.getParameter("provider");
            String identity=this.getParameter("identity");
            if (!StringX.isBlank(identity))
            {
                String key=Identity.composeKey(provider, identity);
                if (!StringX.isBlank(key))
                {
                    result=JsonData.createObject();
                    result.put("key", key);
                }
            }
        }
        else{
            this.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
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
