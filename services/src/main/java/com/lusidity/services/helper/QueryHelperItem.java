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
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.QueryBuilderException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.security.data.filters.PrincipalFilterEngine;
import com.lusidity.system.security.UserCredentials;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QueryHelperItem
{
	private final JsonData data;
	private final int start;
	private final int limit;
	private String lid=null;
	private BaseQueryBuilder.API api=null;
	private QueryResults.Format format=null;

	QueryHelperItem(JsonData data, QueryResults.Format format, int start, int limit)
	{
		super();
		this.data=data;
		this.format=format;
		this.start=start;
		this.limit=limit;
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod"
	})
	public BaseQueryBuilder getQueryBuilder(UserCredentials credentials)
		throws QueryBuilderException, CloneNotSupportedException
	{
		String domain=this.data.getString("domain");
		if (StringX.isBlank(domain))
		{
			throw new QueryBuilderException(false, "The domain cannot be empty.");
		}
		Class<? extends DataVertex> index=Environment.getInstance().getApolloVertexType(domain);
		if (null==index)
		{
			throw new QueryBuilderException(false, String.format("Unknown domain, %s.", domain));
		}

		Common.Direction direction=this.data.getEnum(Common.Direction.class, "direction");
		if (null==direction)
		{
			direction=Common.Direction.OUT;
		}

		String type="";
		Class<? extends DataVertex> partition=null;
		if (direction==Common.Direction.OUT)
		{
			type=this.data.getString("type");
			if (!StringX.isBlank(type))
			{
				partition=Environment.getInstance().getApolloVertexType(type);
			}
		}

		boolean filterable = this.data.getBoolean(false, "asFilterable");

		this.lid=this.data.getString("lid");
		boolean returnEdge=this.data.getBoolean("return_edge");
		boolean includeEdge=this.data.getBoolean("include_edge");

		JsonData query=this.data.getFromPath("native");

		if ((null==query) || !query.isJSONObject())
		{
			throw new QueryBuilderException(false, "The query is not in the expected format.");
		}

		if(filterable)
		{
			this.makeFilterable(query);
		}
		if (this.data.hasKey("format"))
		{
			this.format=this.data.getEnum(QueryResults.Format.class, "format");
			if (this.format==null)
			{
				this.format=QueryResults.Format._default;
			}
		}

		this.api=null;
		if (this.data.hasKey("apiKey"))
		{
			this.api=this.data.getEnum(BaseQueryBuilder.API.class, "apiKey");
		}
		if (null==this.api)
		{
			this.api=BaseQueryBuilder.API._search;
		}

		BaseQueryBuilder result=Environment.getInstance().getIndexStore().getQueryBuilder(index, partition, query, this.api, this.start, this.limit);
		if(this.data.hasKey("params")){
			Map<String, Object> params = new HashMap<>();
			JsonData items = this.data.getFromPath("params");
			if(items.isJSONArray()){
				for(Object o: items){
					if(o instanceof JSONObject)
					{
						JsonData item=JsonData.create(o);
						params.put(item.getString("key"), item.getObjectFromPath("value"));
					}
				}
			}
			result.setParams(params);
		}
		if (null==credentials)
		{
			result.setCredentials(credentials);
		}
		result.setReturnEdge(returnEdge);
		result.setIncludeEdge(includeEdge);

		if(filterable)
		{
			PrincipalFilterEngine.getInstance().applyFor(index, credentials, result, true);
		}

		JsonData sorted=this.data.getFromPath("sorted");
		if (null!=sorted)
		{
			result.setSorted(sorted);
		}

		JsonData sort=this.data.getFromPath("sort");
		if (null==sort)
		{
			sort=this.data.getFromPath("native", "sort");
		}
		if (null!=sort)
		{
			if (sort.isJSONArray())
			{
				this.applySortFormArray(result, sort);
			}
			else
			{
				this.applySortFromObject(result, sort);
			}
		}

		/*
		if(filterable && result.isAggregated()){
			JsonData q = ((JsonData)result.getRawQuery());
			JsonData working = q.getFromPath("query", "filtered", "filter", "bool");
			if(null==working){
				working = q.getFromPath("query", "bool");
			}
			JsonData fData = (null==working) ? JsonData.createObject() : working.clone();
			q.remove("query");
			JsonData pf = JsonData.createObject();
			pf.put("bool", fData);
			q.put("post_filter", pf);
		}
		*/
		return result;
	}

	private JsonData makeFilterable(JsonData query)
		throws CloneNotSupportedException
	{
		JsonData result = query;
		JsonData q = result.getFromPath("query");
		if(!q.hasKey("filtered") && q.hasKey("bool")){
			JsonData bool = q.getFromPath("bool").clone();
			q = JsonData.createObject();
			query.update("query", q);
			JsonData fltd = JsonData.createObject();
			JsonData flt = JsonData.createObject();
			flt.put("bool", bool);
			fltd.put("filter", flt);
			q.put("filtered", fltd);
		}
		return result;
	}

	private void applySortFormArray(BaseQueryBuilder qb, JsonData sort)
	{
		for (Object o : sort)
		{
			JsonData item=JsonData.create(o);
			BaseQueryBuilder.Sort dir;
			Collection<String> keys=item.keys();
			String key;
			if (item.hasKey("property") && item.hasKey("asc"))
			{
				boolean asc=item.getBoolean("asc");
				dir=asc ? BaseQueryBuilder.Sort.asc : BaseQueryBuilder.Sort.desc;
				key=item.getString("property");
			}
			else if (keys.size()==1)
			{
				key=(String) CollectionUtils.get(keys, 0);
				JsonData child=item.getFromPath(key);
				dir=StringX.equalsAnyIgnoreCase(child.getString("order", "asc")) ? BaseQueryBuilder.Sort.asc : BaseQueryBuilder.Sort.desc;
			}
			else
			{
				// old way of doing this
				key=item.getString("on");
				dir=item.getEnum(BaseQueryBuilder.Sort.class, item.getString("direction"));
			}
			if (null==dir)
			{
				dir=BaseQueryBuilder.Sort.asc;
			}
			qb.sort(key, dir);
		}
	}

	private void applySortFromObject(BaseQueryBuilder qb, JsonData sort)
	{
		String on=sort.getString("on");
		BaseQueryBuilder.Sort dir=null;
		try
		{
			dir=BaseQueryBuilder.Sort.valueOf(sort.getString("direction"));
		}
		catch (Exception ignored)
		{
		}
		if (null==dir)
		{
			dir=BaseQueryBuilder.Sort.asc;
		}
		qb.sort(on, dir);
	}

	// Getters and setters
	public String getLid()
	{
		return this.lid;
	}

	public BaseQueryBuilder.API getApi()
	{
		return this.api;
	}
}
