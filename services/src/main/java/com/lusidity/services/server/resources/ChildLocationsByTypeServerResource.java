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
import com.lusidity.domains.location.Location;
import com.lusidity.domains.system.primitives.Text;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/location_location", methods = "get", description = "Get children location for the specified parents.", requiredParams = "url")
@AtAuthorization()
public class ChildLocationsByTypeServerResource
        extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
        JsonData result = JsonData.createObject();

        try {
            String webId = this.getAttribute("webId");
            String type = this.getParameter("type");
            Class<? extends DataVertex> store =BaseDomain.getDomainType(this.getAttribute("domain"));

            if(!StringX.isBlank(webId) && !StringX.isBlank(type)) {
                DataVertex vertex = this.getVertex(store, webId);
                type = StringX.urlDecode(type, "UTF-8");
                if ((null!=vertex) && ClassX.isKindOf(vertex.getClass(), Location.class)) {
                    Location parent = (Location) vertex;
                    List<Location> children = new ArrayList<>();
                    Text t = new Text(type);
                    for (Location child : parent.getPlaces()) {
                        try {
                         //   if (child.getTypes().contains(t)) {
                                children.add(child);
                          //  }
                        }
                        catch (Exception ex){
                            Environment.getInstance().getReportHandler().warning(ex);
                        }
                    }

                    Collections.sort(children, new Comparator<Location>() {
                        @Override
                        public int compare(Location o1, Location o2) {
                            String a = o1.fetchTitle().getValue();
                            String b = o2.fetchTitle().getValue();
                            int result;
                            if (StringX.equals(a, b)) {
                                result = 0;
                            } else if (StringX.isBlank(a)) {
                                result = -1;
                            } else if (StringX.isBlank(b)) {
                                result = 1;
                            } else {
                                result = a.compareTo(b);
                            }
                            return result;
                        }
                    });

                    result.put("next", 0);
                    result.put("limit", 0);
                    result.put("hits", children.size());
                    result.put("took", 0);
                    result.put("maxScore", 0);

                    JsonData items = JsonData.createArray();
                    for(Location child: children){
                        JsonData item = child.toJson(false);
                        this.handleWebResource(child, null, item, null);
                        items.put(item);
                    }
                    result.put("results", items);
                }
                else
                {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            }
            else
            {
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        catch (Exception e)
        {
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
}
