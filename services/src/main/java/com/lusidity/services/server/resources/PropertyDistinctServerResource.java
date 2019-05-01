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
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/property/distinct", description = "Aggregate a column to get distinct values.", methods = "get")
@AtAuthorization()
public class PropertyDistinctServerResource extends BaseServerResource {
	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve(){
        return null;
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

	    try
	    {
		    JsonData data=this.getJsonContent();
		    String property = data.getString("property");
		    if (data.isValid() && data.hasKey("vertexType") && !StringX.isBlank(property))
		    {
			    Class<? extends DataVertex> cls = BaseDomain.getDomainType(data.getString("vertexType"));
			    BaseQueryBuilder qb =Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, 0, 0);
			    qb.aggregations(String.format("%s.raw",property), BaseQueryBuilder.StringTypes.raw, this.getLimit(), true, BaseQueryBuilder.AggTypes.doNotInclude);

			    QueryResults qrs = qb.execute();
			    result = qrs.toJson(qb, null, null, null);
		    }
		    else
		    {
			    this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    }
	    }
		catch (Exception ex)
		{
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}

	    return this.getRepresentation(result);
    }

}
