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
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.discover.DiscoveryEngine;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.DiscoveryItems;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.ArrayList;
import java.util.Collection;

@AtWebResource(pathTemplate = "/svc/discover", methods = "get",
        requiredParams = "phrase (String)", optionalParams = "limit(Integer),size(Integer)",
        description = "Perform a search against the discovery engine.")
@AtAuthorization()
public class DiscoveryServerResource extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
    	return this.getRepresentation(this.getResult(null));
    }

	@Override
	public Representation update(Representation representation)
	{
		JsonData result = null;
		Collection<Class<? extends DataVertex>> types = new ArrayList<>();
		JsonData content = this.getJsonContent();
		if((null!=content) && content.isJSONArray())
		{
			for (Object o : content)
			{
				Class<? extends DataVertex> t = null;
				JsonData item = JsonData.create(o);
				String domain = item.getString("domain");
				if(StringX.startsWith(domain, "/")){
					t = BaseDomain.getDomainType(domain);
				}
				else{
					Class cls = item.getClassFromName("domain");
					if((null!=cls) && ClassX.isKindOf(cls, DataVertex.class)){
						//noinspection unchecked
						t = (Class<? extends DataVertex>)cls;
					}
				}

				if((null!=t)){
					types.add(t);
				}
			}
			result = this.getResult(types);
		}
		return this.getRepresentation(result);
	}

	private JsonData getResult(Collection<Class<? extends DataVertex>> types)
	{
		JsonData result = null;
		try(DiscoveryEngine engine = new DiscoveryEngine(this.getUserCredentials())){
		    String phrase = this.getParameter("phrase");
		    if(StringX.isBlank(phrase)){
		        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    }
		    else
		    {
		        if (!StringX.isBlank(phrase))
		        {
		            phrase=StringX.urlDecode(phrase, "UTF-8");
		            phrase=phrase.trim().toLowerCase();
		        }
		        int limit=this.getLimit();
		        int start=this.getStart();

		        result=JsonData.createObject();
			    result.put("phrase", phrase);
			    result.put("limit", limit);


		        boolean groupIt = this.getBoolean("groupIt");

		        if(groupIt){
			        Collection<DiscoveryItems> discoveries=engine.grouped(phrase, false, start, limit, types);
			        int hits =0;
			        for(DiscoveryItems di: discoveries){
			        	hits+=di.getHits();
			        }
			        result.put("hits", hits);
			        this.getGroups(result, discoveries, phrase, limit);
		        }
		        else
		        {
			        DiscoveryItems discoveries=engine.discover(phrase, false, start, limit, types);
			        result.put("hits", discoveries.getHits());
			        this.getResults(result, discoveries);
		        }
		    }
		}
		catch (Exception ex){
		    Environment.getInstance().getReportHandler().severe(ex);
		    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return result;
	}

	private void getGroups(JsonData result, Collection<DiscoveryItems> discoveries, String phrase, int limit)
	{
		JsonData results = JsonData.createArray();
		result.put("groups", results);

		for(DiscoveryItems discoveryItem: discoveries){
			if(discoveryItem.getItems().isEmpty()){
				continue;
			}
			JsonData group=JsonData.createObject();
			group.put("hits", discoveryItem.getHits());
			group.put("phrase", phrase);
			group.put("limit", limit);
			JsonData values = JsonData.createArray();
			group.put("results", values);
			String name = null;
			boolean hasValues = false;
			for(DiscoveryItem item: discoveryItem.getItems()){
				if(null==name){
					name = item.getName();
				}
				DataVertex vertex=item.getVertex();
				boolean process=true;
				if (ScopedConfiguration.getInstance().isEnabled() && (null!=vertex) && !ClassX.isKindOf(this.getUserCredentials(), SystemCredentials.class))
				{
					if (vertex.enforcePolicy() && !PolicyDecisionPoint.isInScope(vertex, this.getUserCredentials()))
					{
						process=false;
					}
				}
				if (process)
				{
					hasValues = true;
					JsonData value=item.toJson();
					values.put(value);
				}
			}
			if(hasValues)
			{
				group.put("name", name);
				results.put(group);
			}
		}
	}

	private void getResults(JsonData result, DiscoveryItems discoveries)
	{
		JsonData items=JsonData.createArray();
		discoveries.sort();
		for (DiscoveryItem discoveryItem : discoveries.getItems())
		{
		    DataVertex vertex=discoveryItem.getVertex();
		    boolean process=true;
		    if (ScopedConfiguration.getInstance().isEnabled() && (null!=vertex) && !ClassX.isKindOf(this.getUserCredentials(), SystemCredentials.class))
		    {
		        if (vertex.enforcePolicy() && !PolicyDecisionPoint.isInScope(vertex, this.getUserCredentials()))
		        {
		            process=false;
		        }
		    }
		    if (process)
		    {
		        JsonData item=discoveryItem.toJson();
		        items.put(item);
		    }
		}
		result.put("results", items);
	}

	@Override
    public Representation store(Representation representation)
    {
        return null;
    }

}
