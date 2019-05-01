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
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResultHandler;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.exceptions.QueryBuilderException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.helper.QueryHelper;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.lang.reflect.Constructor;

@SuppressWarnings("UnusedDeclaration")
@AtWebResource(pathTemplate = "/svc/query", methods = "post", description = "A query used to search Apollo, https://lusidity.fogbugz.com/default.asp?W11.",
        bodyFormat = "{" +
                "  \"domains\": [\"/film/film\"]," +
                "  \"apiKey\": \"_search\"," +
                "  \"types\": []," +
                "  \"filters\":[" +
                "    {\"operator\": \"must\", \"propertyName\": \"title\", \"type\": \"starts_with\", \"value\": \"big trouble\"}" +
                "  ]," +
                "  \"sort\":[{\"propertyName\": \"title\", \"direction\": \"asc\"}]" +
                "}"
)
@AtAuthorization()
public class QueryServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
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
        JsonData result;
        try {
            int start = this.getStart();
            int limit = this.getLimit();
            JsonData data = this.getJsonContent();
            result = this.getResults(start, limit, data);

            if(data.hasKey("materializedViewType") && data.hasKey("materializedViewKey")){
                this.getFromView(result, data);
            }
            if(result.isEmpty()){
                result.put("next", 0);
                result.put("limit", 0);
                result.put("hits", 0);
                result.put("took", 0);
                result.put("maxScore", 0);
                result.put("actual", 0);
                result.put("excluded", 0);
                result.put("results", JsonData.createArray());
            }
        } catch (QueryBuilderException e) {
            result = JsonData.createObject();
            result.put("error", e.getMessage());
            result.put("internalError", e.isInternalError());
            this.getResponse().setStatus(e.isInternalError() ? Status.SERVER_ERROR_INTERNAL: Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
        catch (Exception e)
        {
            result = JsonData.createObject();
            result.put("error", e.getMessage());
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return this.getRepresentation(result);
    }

    private void getFromView(JsonData result, JsonData data)
    {
        String mvType = data.getString("materializedViewType");
        String mvKey = data.getString("materializedViewKey");
        String mvWildCard = data.getString("materializedViewWildcard");

        Class<? extends DataVertex> cls = Environment.getInstance().getApolloVertexType(mvType);

        JsonData results = JsonData.createArray();
        JsonData items = result.getFromPath("results");
        for(Object o: items){
            JsonData item = JsonData.create(o);
            String lid = item.getString("lid");
            if(!StringX.isBlank(mvWildCard)){
                lid = StringX.replace(mvWildCard, "[lid]", lid);
            }
            BaseQueryBuilder qb =Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, 0, 1);
            qb.filter(BaseQueryBuilder.Operators.wildcard, mvKey, BaseQueryBuilder.StringTypes.raw, lid);
            qb.setCredentials(SystemCredentials.getInstance());
            QueryResults qrs = qb.execute();
            DataVertex vertex = qrs.getFirst();
            if(null!=vertex){
                JsonData vData = vertex.toJson(false);
                this.handleWebResource(vertex, null, vData, null);
                if(item.hasKey("_edge")){
                    vData.put("_edge", item.getFromPath("_edge"), true);
                }
                results.put(vData);
            }
        }

        result.remove("results");
        result.put("results", results);
    }

    private JsonData getResults(int start, int limit, JsonData data) throws Exception {
        JsonData result=null;
        try {
            boolean eleveatePermissions = this.elevatePermissions();
            @SuppressWarnings("SimplifiableConditionalExpression")
            QueryHelper queryHelper= new QueryHelper(this.getUserCredentials(), (eleveatePermissions ? false :ScopedConfiguration.getInstance().isEnabled()), data, start, limit);

	        IQueryResultHandler handler = null;
	        String paramHandler = this.getParameter("handler");
	        if(!StringX.isBlank(paramHandler)){
	        	try{
	        		Class cls = Class.forName(paramHandler);
			        if(ClassX.isKindOf(cls, IQueryResultHandler.class)){
				        @SuppressWarnings("unchecked")
				        Constructor constructor = cls.getConstructor();
				        handler = (IQueryResultHandler)constructor.newInstance();
			        }
		        }
		        catch (Exception ignored){}
	        }
            result = queryHelper.execute(handler);
            if ((null==result) || result.hasKey("error")) {
                this.setStatus(result.getBoolean("clientError") ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL, result.getString("error"));
            }
        } catch (QueryBuilderException e) {
            this.getResponse().setStatus(e.isInternalError() ? Status.SERVER_ERROR_INTERNAL: Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
        return result;
    }
}
