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

import com.lusidity.Environment;
import com.lusidity.apollo.elasticSearch.EsIndexStore;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtAuthorization()
@AtWebResource(pathTemplate = "/svc/admin/es", methods = "post", description = "Returns status information on ElasticSearch")
public class ElasticSearchStatusResource
        extends BaseServerResource {

	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @SuppressWarnings("AccessOfSystemProperties")
    @Override
    public Representation retrieve() {
        return null;
    }

    @Override
    public Representation store(Representation representation)
    {
        return this.update(representation);
    }

    @Override
    public Representation update(Representation representation)
    {
        JsonData result = JsonData.createObject();
        try
        {
            if(!this.getUserCredentials().getPrincipal().isAdmin(false))
            {
                this.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
            else{
                JsonData options = this.getJsonContent();
                if(ClassX.isKindOf(Environment.getInstance().getIndexStore(), EsIndexStore.class)){
                    EsIndexStore store = (EsIndexStore)Environment.getInstance().getIndexStore();
                    result = store.report(options);
                }
                else{
                    result.put("error", "The index store is not ElasticSearch");
	                this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }
        }
        catch (Exception ignored) {
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (null!=result) ? result.toJsonRepresentation() : null;
    }
}
