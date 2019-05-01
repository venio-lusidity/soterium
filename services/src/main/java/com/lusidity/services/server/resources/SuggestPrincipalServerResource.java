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
import com.lusidity.discover.DiscoveryEngine;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.DiscoveryItems;
import com.lusidity.domains.acs.security.AnonymousUser;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.SystemUser;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.organization.Organization;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@AtWebResource(pathTemplate = "/svc/discover/suggest/principal", methods = "get",
        requiredParams = "query (String)", optionalParams = "limit(Integer),start(Integer)",
        description = "Perform a search against the discovery engine returning suggestions.")
@AtAuthorization()
public class SuggestPrincipalServerResource extends BaseServerResource {

	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
        try {
            String phrase = this.getParameter("phrase");
            if(!StringX.isBlank(phrase)) {
                phrase = StringX.urlDecode(phrase, "UTF-8");
	            phrase = phrase.trim().toLowerCase();
                if (StringX.startsWith(phrase, "\"") && StringX.endsWith(phrase, "\"")) {
                    phrase = StringX.removeEnd(phrase, "\"");
                    phrase = StringX.removeStart(phrase, "\"");
                }

                int start = this.getStart();
                int limit = this.getLimit();

                JsonData items = this.query(phrase, start, limit);
                result.put("hits", items.length());
                result.put("results", items);
            }
        } catch (Exception e) {
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

    private JsonData query(String query, int start, int limit) {
        JsonData results = JsonData.createArray();
        try {
            Set<Class<? extends BasePrincipal>> subTypesOf = Environment.getInstance().getReflections().getSubTypesOf(BasePrincipal.class);

            if(null!=subTypesOf) {

                Collection<Class<? extends DataVertex>> types = new ArrayList<>();
                for (Class<? extends BasePrincipal> subType : subTypesOf) {
                    types.add(subType);
                }

                types.remove(AnonymousUser.class);
                types.remove(SystemUser.class);
                types.remove(Organization.class);
                types.remove(Group.class);

                try(DiscoveryEngine engine = new DiscoveryEngine(this.getUserCredentials()))
                {
                    DiscoveryItems discoveries=engine.discover(query, false, start, limit, types);
                    if (!discoveries.isEmpty())
                    {
                        for (DiscoveryItem item : discoveries.getItems())
                        {
                            results.put(item.toJson());
                        }
                    }
                }
                catch (Exception ignored){}
            }
        }
        catch (Exception ignored){}
        return results;
    }
}
