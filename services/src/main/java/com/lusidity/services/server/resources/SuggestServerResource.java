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
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.ArrayList;
import java.util.Collection;

@AtWebResource(pathTemplate = "/svc/discover/suggest", methods = "get",
        requiredParams = "phrase (String)", optionalParams = "limit(Integer),start(Integer)",
        description = "Perform a search against the discovery engine returning suggestions.")
@AtAuthorization()
public class SuggestServerResource extends BaseServerResource {
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
                if(StringX.startsWith(phrase, "\"") && StringX.endsWith(phrase, "\"")){
                    phrase = StringX.removeEnd(phrase, "\"");
                    phrase = StringX.removeStart(phrase, "\"");
                }
                int start = this.getStart();
                int limit = this.getLimit();
                this.query(result, phrase, null, start, limit);
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
        JsonData result = JsonData.createObject();
        try {
            JsonData data = this.getJsonContent();
            if((null!=data) && data.hasValue("phrase")) {
                Collection<Class<? extends DataVertex>> types = new ArrayList<>();
                String phrase = data.getString("phrase");
                JsonData values = data.getFromPath("types");

                if((null!=values) && values.isJSONArray()){
                    for(Object o: values){
                        if(o instanceof String){
                            String value = (String)o;
                            Class<? extends DataVertex> type =BaseDomain.getDomainType(value);
                            if(null!=type){
                                types.add(type);
                            }
                        }
                    }
                }

                int start = this.getStart();
                int limit = this.getLimit();

                this.query(result, phrase, types, start, limit);

            }
            else{
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    private void query(JsonData result, String query, Collection<Class<? extends DataVertex>> types, int start, int limit) {
        JsonData results = JsonData.createArray();
        result.put("results", results);
        try(DiscoveryEngine engine = new DiscoveryEngine(this.getUserCredentials())) {
            DiscoveryItems discoveryItems = engine.discover(query, true, start, limit, types);
	        result.put("hits", discoveryItems.getHits());
            if (!discoveryItems.isEmpty()) {
                for (DiscoveryItem item : discoveryItems.getItems()) {
                    results.put(item.toJson());
                }
            }
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().warning(ex);
        }
    }
}
