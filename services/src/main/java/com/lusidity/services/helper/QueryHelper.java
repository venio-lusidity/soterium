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

package com.lusidity.services.helper;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.IQueryResultHandler;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.framework.exceptions.QueryBuilderException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.UserCredentials;
import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class QueryHelper
{
	private final UserCredentials credentials;
	private final JsonData data;
	private final int start;
	private final int limit;
	private final boolean scopingEnabled;
	private QueryResults.Format format=QueryResults.Format._default;


// Constructors

	/**
	 * This method takes a simple Json query and transforms it into a BaseQueryBuilder.
	 *
	 * @param credentials    UserCredentials will return data in scope.
	 * @param scopingEnabled Use scoping authorizations with this query?
	 * @param data           The JsonData from the web.
	 * @param start          Where to begin the results at for paging.
	 * @param limit          Max amount to return.    @return  Returns results retrieved based on query.
	 */
	public QueryHelper(UserCredentials credentials, boolean scopingEnabled, JsonData data, int start, int limit)
	{
		super();
		this.credentials=credentials;
		this.data=data;
		this.start=start;
		this.limit=limit;
		this.scopingEnabled=scopingEnabled;
	}

	@SuppressWarnings("unused")
	private static BaseQueryBuilder applyFilterAndSort(Class<? extends DataVertex> index, Class<? extends DataVertex> type,
	                                                   JsonData filters, JsonData sort, int start, int limit)
		throws QueryBuilderException
	{
		BaseQueryBuilder result=Environment.getInstance().getIndexStore().getQueryBuilder(index, type, start, limit);
		QueryHelper.applyFilter(result, filters);
		QueryHelper.applySort(result, sort);
		return result;
	}

	private static void applyFilter(BaseQueryBuilder qb, JsonData items)
		throws QueryBuilderException
	{
		String error=String.format("\nExpected: \n\"filters\":["+
		                           "\n\t{\"operator\": \"\", \"propertyName\": \"\", \"type\": \"\", \"value\": \"\"}"+
		                           "\n]"+
		                           "\noperators: %s"+
		                           "\ntypes: %s\n", StringX.enumToString(BaseQueryBuilder.Operators.class), StringX.enumToString(BaseQueryBuilder.StringTypes.class));

		for (Object o : items)
		{
			if (o instanceof JSONObject)
			{
				JsonData item=new JsonData(o);

				BaseQueryBuilder.Operators operator=item.getEnum(BaseQueryBuilder.Operators.class, "operator");
				String propertyName=item.getString("propertyName");
				BaseQueryBuilder.StringTypes stringType=item.getEnum(BaseQueryBuilder.StringTypes.class, "type");
				Object value=item.getObjectFromPath("value");

				if (null==operator)
				{
					String v=item.getString("operator");
					throw new QueryBuilderException(false, "The operator, %s, is not valid.%s", StringX.isBlank(v) ? "null" : v, error);
				}
				if (StringX.isBlank(propertyName))
				{
					throw new QueryBuilderException(false, "The propertyName cannot be null.%s", error);
				}
				if (null==stringType)
				{
					String v=item.getString("type");
					throw new QueryBuilderException(false, "The type, %s, is not valid.%s", StringX.isBlank(v) ? "null" : v, error);
				}
				if (null==value)
				{
					throw new QueryBuilderException(true, "Sorry, you cannot query for null values at this time.");
				}
				qb.filter(operator, propertyName, stringType, value);
			}
			else
			{
				throw new QueryBuilderException(false, "The filters are not in the expect format.%s", error);
			}
		}
	}

	private static void applySort(BaseQueryBuilder qb, JsonData items)
		throws QueryBuilderException
	{
		String error=String.format("\nExpected: \n\"sort\":[{\"propertyName\": \"\", \"direction\": \"asc\"}]"+
		                           "\nsort: %s\n", StringX.enumToString(BaseQueryBuilder.Sort.class));
		if (null!=items)
		{
			for (Object o : items)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=new JsonData(o);

					BaseQueryBuilder.Sort direction=item.getEnum(BaseQueryBuilder.Sort.class, "direction");
					String propertyName=item.getString("propertyName");

					if (null==direction)
					{
						String v=item.getString("direction");
						throw new QueryBuilderException(false, "The direction, %s, is not valid.%s", StringX.isBlank(v) ? "null" : v, error);
					}
					if (StringX.isBlank(propertyName))
					{
						throw new QueryBuilderException(false, "The propertyName cannot be null.%s", error);
					}
					qb.sort(propertyName, direction);

				}
				else
				{
					throw new QueryBuilderException(false, "The filters are not in the expect format.%s", error);
				}
			}
		}
	}


	public JsonData execute(IQueryResultHandler handler)
		throws Exception
	{
		JsonData result;
		if (this.data.hasKey("native"))
		{
			result=this.queryNative(handler);
		}
		else
		{
			throw new NoSuchMethodError("Native queries should be used in place of the old query builder.");
		}

		return result;
	}

	/**
	 * This method uses an ElasticSearch native query format and transforms it into a BaseQueryBuilder.
	 *
	 * @throws QueryBuilderException A QueryBuilderException
	 */
	private JsonData queryNative(IQueryResultHandler handler)
		throws Exception
	{
		JsonData result=JsonData.createObject();
		JsonData results=JsonData.createArray();
		result.put("results", results);

		QueryHelperItem qhi=new QueryHelperItem(this.data, this.format, this.start, this.limit);
		BaseQueryBuilder qb=qhi.getQueryBuilder(this.credentials);
		String lid=qhi.getLid();
		BaseQueryBuilder.API api=qhi.getApi();
		if (null==qb.getCredentials())
		{
			qb.setCredentials(this.credentials);
		}
		return this.execute(qb, lid, api, handler);
	}

	private JsonData execute(BaseQueryBuilder qb, String lid, BaseQueryBuilder.API api, IQueryResultHandler handler)
	{
		JsonData result;
		qb.setApi(api);
		QueryResults qrs=qb.execute();
		qrs.setScopingEnabled(this.scopingEnabled);
		if (null==qb.getCredentials())
		{
			qb.setCredentials(this.credentials);
		}
		if (qb.getApi()==BaseQueryBuilder.API._count)
		{
			result=JsonData.createObject().put("hits", qrs.getHits());
		}
		else
		{
			result=qrs.toJson(qb, lid, this.format, handler);
		}
		return result;
	}

	public Set<DataVertex> execute()
		throws Exception
	{
		Set<DataVertex> results=new HashSet<>();
		if (this.data.hasKey("native"))
		{
			this.queryNative(results);
		}
		else
		{
			throw new NoSuchMethodError("Native queries should be used in place of the old query builder.");
		}
		return results;
	}

	private void queryNative(Set<DataVertex> results)
		throws Exception
	{
		QueryHelperItem qhi=new QueryHelperItem(this.data, this.format, this.start, this.limit);
		BaseQueryBuilder qb=qhi.getQueryBuilder(this.credentials);
		QueryResults qrs=qb.execute();
		for (IQueryResult qr : qrs)
		{
			DataVertex vertex=qr.getVertex();
			results.add(vertex);
		}
	}

	// Getters and setters
	public File getFile()
	{
		File result;
		String path=String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files/hierarchy");
		path=StringX.replace(path, "//", "/");
		result=new File(path);
		if (!result.exists())
		{
			//noinspection ResultOfMethodCallIgnored
			result.mkdirs();
		}
		return result;
	}
}
