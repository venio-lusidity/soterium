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

package com.lusidity.data.interfaces.data.query;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.net.URL;
import java.util.Map;

public abstract class BaseQueryBuilder
{
	public enum Sort
	{
		asc,
		desc
	}

	public enum StringTypes
	{
		// ES does not have a contains.
		contains,
		folded,
		raw,
		starts_with,
		date_time,
		na
	}
	public enum RangeTypes
	{
		gt,
		// > greater than
		lt,
		// < less than
		gte,
		// >= greater than or equal to
		lte //<= less than or equal to
	}

	public enum Operators
	{
		matchAll,
		must,
		wildcard,
		should,
		should_wildcard,
		should_not_wildcard,
		must_not,
		equal,
		gt,
		gte,
		lt,
		lte,
		range,
		and,
		or
	}
	public enum API
	{
		_count,
		_search,
		_cluster,
		_query,
		_delete_by_query,
		_other_end,
		_node
	}

	public enum AggTypes{
		number,
		string,
		doNotInclude
	}
	protected static final int MAX_EXPANSIONS=10;
	protected static final int DEFAULT_LIMIT=30;
	private boolean httpRequest = false;
	private Class<? extends DataVertex> store;
	private Class<? extends DataVertex> partition;
	private int start=0;
	private int limit=0;
	private BaseQueryBuilder.API api=BaseQueryBuilder.API._search;
	private boolean includeDuplicates=false;
	private boolean returnEdge = false;
	private boolean includeEdge = false;
	private UserCredentials credentials = null;
	private Map<String, Object> params = null;
	// Constructors
	public BaseQueryBuilder(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, int start, int limit)
	{
		super();
		this.store=store;
		this.partition=partition;
		this.start=start;
		this.limit=limit;
	}

	public BaseQueryBuilder(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, BaseQueryBuilder.API api, int start, int limit)
	{
		super();
		this.store=store;
		this.partition=partition;
		this.start=start;
		this.limit=limit;
		this.api=api;
	}

// Methods
	public static BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, int start, int limit)
	{
		return Environment.getInstance().getIndexStore().getQueryBuilder(indexStore, indexType, start, limit);
	}

	public static BaseQueryBuilder getQueryBuilder(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> indexType, BaseQueryBuilder.API api, int start, int limit)
	{
		return Environment.getInstance().getIndexStore().getQueryBuilder(indexStore, indexType, api, start, limit);
	}



	public abstract void range(String propertyName, Integer min, Integer max);
	public abstract void range(String propertyName, DateTime min, DateTime max);

	public abstract BaseQueryBuilder sort(String propertyName, BaseQueryBuilder.Sort direction);
	public abstract BaseQueryBuilder filter(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.StringTypes type, Object value);
	public abstract BaseQueryBuilder filter(Operators operator, String propertyName, StringTypes type, Object value, Operators oprtr);
	public abstract BaseQueryBuilder filter(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.RangeTypes type, Object value);
	public abstract BaseQueryBuilder filter(BaseNestedQueryBuilder nb);
	public abstract void filter(BaseQueryBuilder.Operators operator, String propertyName, Integer value);
	public abstract void filter(BaseQueryBuilder.Operators operator, String propertyName, DateTime value);

	public abstract QueryResults execute();

	public abstract boolean delete();

	public abstract void matchAll();

	public abstract BaseQueryBuilder filerBoxBegin();
	public abstract BaseQueryBuilder filerBoxEnd();

	public abstract void nullifySort();

	public abstract void aggregations(String property, BaseQueryBuilder.StringTypes type, Integer limit, boolean includeVertex, BaseQueryBuilder.AggTypes includeIfMissing);

	public abstract void aggregations(JsonData aggs, boolean includeVertices);

	public abstract void aggregations(JsonData aggs);

	public boolean returnEdge()
	{
		return this.returnEdge;
	}
	public boolean includeEdge()
	{
		return this.includeEdge;
	}

	public abstract void resetSort();

	public abstract void propertyMissingOrNull(String key);

	public abstract void applyShould(Object statement);

	public abstract void applyMust(Object statement);

	public abstract void applyMustNot(Object statement);

	public void useHttpRequest(boolean httpRequest){
		this.httpRequest = httpRequest;
	}

	// Getters and setters
	public UserCredentials getCredentials()
	{
		return this.credentials;
	}

	public void setCredentials(UserCredentials credential){
		this.credentials = credential;
	}

	public abstract JsonData getSorted();

	/**
	 * Used for custom sort when the data store can not perform the function.
	 * @param sorted
	 */
	public abstract void setSorted(JsonData sorted);

	public abstract int getFilterSize();

	public int getLimit()
	{
		return this.limit;
	}

	public BaseQueryBuilder setLimit(int limit)
	{
		this.limit=limit;
		return this;
	}

	public int getStart()
	{
		return this.start;
	}

	public BaseQueryBuilder setStart(int start)
	{
		this.start=start;
		return this;
	}

	public abstract BaseNestedQueryBuilder getNestedBuilder();

	public abstract JsonData getJson();

	public BaseQueryBuilder.API getApi()
	{
		return this.api;
	}

	public BaseQueryBuilder setApi(BaseQueryBuilder.API api)
	{
		this.api=api;
		return this;
	}

	public abstract JsonData getSort();

	public abstract boolean isAggregated();

	public abstract void setAggregated(boolean aggregated);

	public boolean isHttpRequest()
	{
		return this.httpRequest;
	}

	public boolean isIncludeDuplicates()
	{
		return this.includeDuplicates;
	}

	public void setIncludeDuplicates(boolean includeDuplicates)
	{
		this.includeDuplicates=includeDuplicates;
	}

	public String getCurlSearch()
	{
		return String.format("curl -XGET %s?pretty -d '%s';echo;", this.getUrl(BaseQueryBuilder.API._search), this.toString());
	}

	public URL getUrl(BaseQueryBuilder.API api)
	{
		return Environment.getInstance().getIndexStore().getEngine().getIndexUrl(this.getStore(), this.getPartition(), api);
	}

	@Override
	public String toString()
	{
		return (this.getQuery() instanceof JsonData) ? this.getQuery().toString() : this.getClass().getName();
	}

	public Class<? extends DataVertex> getStore()
	{
		return this.store;
	}

	public Class<? extends DataVertex> getPartition()
	{
		return this.partition;
	}

	public abstract Object getQuery();

	public abstract void setQuery(Object query);

	public void setPartition(Class<? extends DataVertex> partition)
	{
		this.partition=partition;
	}

	public void setStore(Class<? extends DataVertex> store)
	{
		this.store=store;
	}

	public abstract Object getRawQuery();

	public abstract Object getDeleteQuery();

	public String getCurlCount()
	{
		return String.format("curl -XGET %s?pretty -d '%s';echo;", this.getUrl(BaseQueryBuilder.API._count), this.toString());
	}

	public Map<String, Object> getParams()
	{
		return this.params;
	}

	public void setParams(Map<String, Object> params)
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.params=params;
	}

	public void setReturnEdge(boolean returnEdge)
	{
		this.returnEdge=returnEdge;
	}

	public void setIncludeEdge(boolean includeEdge)
	{
		this.includeEdge=includeEdge;
	}

	public abstract Object getLastFilterApplied();
}