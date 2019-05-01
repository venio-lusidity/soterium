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
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.representation.Representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@AtWebResource(pathTemplate = "/svc/endpoints", description = "A list of all REST endpoints.", methods = "get")
@AtAuthorization()
public class EndpointsServerResource extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
        JsonData items = JsonData.createArray();
        Collection<Class<? extends BaseServerResource>> resources = Environment.getInstance().getReflections().getSubTypesOf(BaseServerResource.class);

        int total = 0;
        if (null != resources) {
            List<Class<? extends BaseServerResource>> list = new ArrayList<>(resources);
            list.sort(new Comparator<Class<? extends BaseServerResource>>() {
                @Override
                public int compare(Class<? extends BaseServerResource> o1, Class<? extends BaseServerResource> o2) {
                    return o1.getSimpleName().compareTo(o2.getSimpleName());
                }
            });
            for (Class<? extends BaseServerResource> resource : list) {
                JsonData item = JsonData.createObject();
                item.put("type", resource);
                AtWebResource wra = resource.getAnnotation(AtWebResource.class);
                if (null == wra) {
                    item.put("error", "Missing WebResourceAnnotation.");
                } else {
                    item.put("matchingMode", wra.matchingMode());
                    item.put("path", wra.pathTemplate());
                    item.put("methods", wra.methods());
                    item.put("requiredParams", wra.requiredParams());
                    item.put("optionalParams", wra.optionalParams());
                    item.put("description", wra.description());
                    item.put("bodyFormat", wra.bodyFormat());
                }
                items.put(item);
            }
        }
        result.put("total", items.length());
        result.put("results", items);
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
