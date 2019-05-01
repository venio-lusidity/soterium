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
import com.lusidity.data.interfaces.data.query.IQueryResultHandler;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.exceptions.QueryBuilderException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.office.CSVUtils;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.helper.QueryHelper;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
@AtWebResource(pathTemplate = "/svc/query/csv", methods = "post", description = "A query used to search Apollo, https://lusidity.fogbugz.com/default.asp?W11.",
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
public class QueryServerCsvExportResource
        extends BaseServerResource {

	// Overrides
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
        JsonData result = null;
        try {
            int start = this.getStart();
            int limit = this.getLimit();
            if(limit<=0){
                limit = 1000000;
            }
            JsonData data = this.getJsonContent();
            // TODO: if the limit is 0 then we should get all the results.
            // leave the paging as this could be useful for exporting in chunks.
            result = JsonData.createObject();
            String title = data.getString("fileNamePrefix");
            if(null==title){
                title = UUID.randomUUID().toString();
            }
            String url = this.export(start, limit, title, data);
            result.put("url", url);

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

    private String export(int start, int limit, String title, JsonData data) throws Exception {
        String result = null;
        try {
            boolean powerUser = this.getBoolean("pu");
            if(powerUser){
            	powerUser = this.isPowerUser();
            }
            @SuppressWarnings("SimplifiableConditionalExpression")
            QueryHelper queryHelper= new QueryHelper(this.getUserCredentials(), (powerUser ? false :ScopedConfiguration.getInstance().isEnabled()), data, start, limit);

	        IQueryResultHandler handler = null;
            Set<DataVertex> results = queryHelper.execute();
            if(null==results){
                this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            else if (results.isEmpty()) {
                this.setStatus(
                    Status.SUCCESS_NO_CONTENT
                );
            }
            else{
                // todo: not now but we should implement the capability of the client passing a schema.
                JsonData schema = data.getFromPath("schema");
                LinkedHashMap<String, Object> fSchema = null;
                if((null!=schema) && schema.isJSONArray() && (schema.length()>0)){
                    fSchema = new LinkedHashMap<>();
                    for(Object o: schema){
                        if(o instanceof JSONObject){
                            JsonData item = JsonData.create(o);
                            fSchema.put(item.getString("key"),  item.getString("label"));
                        }
                    }
                }
                DateTime now = DateTime.now();
                String prefix=String.format("%s-%d-%d-%d", title, now.getYear(), now.getMonthOfYear(), now.getDayOfMonth());
                String path=queryHelper.getFile().getPath();
                File file=new File(path, String.format("%s.csv", prefix));
                boolean exists=file.exists();
                if (!exists)
                {
                    result = this.writeCsvReport(prefix, results, queryHelper, fSchema);
                }
                else
                {
                    result=FileX.getWebUrl(file, Environment.getInstance().getConfig().getBlobBaseUrl(), "files", "hierarchy");
                }
            }

        } catch (QueryBuilderException e) {
            this.getResponse().setStatus(e.isInternalError() ? Status.SERVER_ERROR_INTERNAL: Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
        return result;
    }
    private String writeCsvReport(String prefix, Set<DataVertex> vertexResults, QueryHelper helper, LinkedHashMap<String, Object> schema)
        throws Exception
    {
        String result = null;
        String path = helper.getFile().getPath();
        File file = new File(path, String.format("%s.csv", prefix));
        @SuppressWarnings("CollectionDeclaredAsConcreteClass")
        LinkedHashMap<String, Object> fSchema = schema;

        CSVUtils csvUtils = new CSVUtils();
        String sc = Environment.getInstance().getSecurityClassification();
        for(DataVertex vertex: vertexResults){
            if(null==fSchema)
            {
                fSchema=vertex.getExcelSchema().getCSVSchema();
            }
            JsonData item = vertex.toJson(false);
            csvUtils.append(schema, item, file, StringX.isBlank(sc) ? "" : sc, StringX.isBlank(sc) ? "" : String.format("This document in its entirety is %s and should be treated accordingly.", sc));
        }
        if(!StringX.isBlank(sc))
        {
            csvUtils.append(file, sc);
        }
        result =FileX.getWebUrl(file, Environment.getInstance().getConfig().getBlobBaseUrl(), "files", "hierarchy");
        return result;
    }
}
