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

package com.lusidity.apollo.elasticSearch;


import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseNestedQueryBuilder;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.IndexHelper;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;

import java.util.Objects;

public class EsQueryBuilder extends BaseQueryBuilder
{
	// Fields
	public static final long NUMERICAL_NULL_VALUE=-999999999;
	private JsonData sort=JsonData.createArray();
	private JsonData startsWith=null;
	private JsonData must=JsonData.createArray();
	private JsonData should=JsonData.createArray();
	private JsonData mustNot=JsonData.createArray();
	private JsonData range=JsonData.createObject();
	private JsonData matchAll=null;
	private JsonData query=null;
	private Class<? extends DataVertex> propertyPartition=null;
	private JsonData aggregations=null;
	private JsonData aggTerms=null;
	private JsonData aggVertex=null;
	private boolean aggregated=false;
	private JsonData sorted=null;
	private JsonData lastFilterApplied;

// Constructors

	/**
	 * Build an ElasticSearch index query.
	 *
	 * @param store     The Class store.
	 * @param partition The index type
	 * @param start     paging start
	 * @param limit     max number of items
	 */
	public EsQueryBuilder(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, int start, int limit)
	{
		super(store, partition, BaseQueryBuilder.API._search, start, limit);
		this.fix();
	}

	private void fix()
	{
		if ((null!=this.getPartition()) && !ClassX.isKindOf(this.getStore(), Edge.class) && !Objects.equals(this.getStore(), this.getPartition()))
		{
			this.propertyPartition=this.getPartition();
			this.setPartition(this.getStore());
		}
	}

	public EsQueryBuilder(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, BaseQueryBuilder.API api, int start, int limit)
	{
		super(store, partition, api, start, limit);
		this.fix();
	}

	// Overrides
	@Override
	public void range(String propertyName, Integer min, Integer max)
	{
		JsonData data=this.range.getFromPath(propertyName);
		if (null!=data)
		{
			this.range.remove(propertyName);
		}
		data=JsonData.createObject().put(BaseQueryBuilder.Operators.gte, min).put(BaseQueryBuilder.Operators.lte, max);
		this.range.put(propertyName, data);
	}

	@Override
	public void range(String propertyName, DateTime min, DateTime max)
	{
		JsonData data=this.range.getFromPath(propertyName);
		if (null!=data)
		{
			this.range.remove(propertyName);
		}
		data=JsonData.createObject().put(BaseQueryBuilder.Operators.gte, min).put(BaseQueryBuilder.Operators.lte, max);
		this.range.put(propertyName, data);
	}

	@Override
	public BaseQueryBuilder sort(String propertyName, BaseQueryBuilder.Sort direction)
	{
		JsonData result=JsonData.createObject().put(propertyName, JsonData.createObject()
		                                                                  .put("order", direction.toString()).put("missing", "_last"));//.put("ignore_unmapped", true));
		this.sort.put(result);
		return this;
	}

	@Override
	public BaseQueryBuilder filter(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.StringTypes type, Object value)
	{
		if (!StringX.isBlank(propertyName))
		{
			Object finalValue=IndexHelper.getValueForIndex(value);
			switch (type)
			{
				case starts_with:
					if (null!=this.startsWith)
					{
						Environment.getInstance().getReportHandler().severe("There can only by one starts with.");
					}
					else if (!(finalValue instanceof String))
					{
						Environment.getInstance().getReportHandler().severe("The value must be a string in order to use the type starts_with.");
					}
					else
					{
						this.addStartsWith(propertyName, finalValue);
					}
					break;
				case folded:
					if (finalValue instanceof String)
					{
						finalValue=((String) finalValue).toLowerCase();
					}
					this.add(operator, propertyName, (finalValue instanceof String) ? type : BaseQueryBuilder.StringTypes.na, finalValue, null);
					break;
				default:
					this.add(operator, propertyName, (finalValue instanceof String) ? type : BaseQueryBuilder.StringTypes.na, finalValue, null);
					break;
			}
		}
		return this;
	}

	@Override
	public BaseQueryBuilder filter(Operators operator, String propertyName, StringTypes type, Object value, Operators oprtr)
	{
		if (!StringX.isBlank(propertyName))
		{
			Object finalValue=IndexHelper.getValueForIndex(value);
			switch (type)
			{
				case folded:
					if (finalValue instanceof String)
					{
						finalValue=((String) finalValue).toLowerCase();
					}
					this.add(operator, propertyName, (finalValue instanceof String) ? type : BaseQueryBuilder.StringTypes.na, finalValue, oprtr);
					break;
				default:
					this.add(operator, propertyName, (finalValue instanceof String) ? type : BaseQueryBuilder.StringTypes.na, finalValue, oprtr);
					break;
			}
		}
		return this;
	}

	@Override
	public BaseQueryBuilder filter(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.RangeTypes type, Object value)
	{
		JsonData item=this.range.getFromPath(propertyName);
		if ((null==item) || !item.isValid())
		{
			item=JsonData.createObject();
			this.range.put(propertyName, item);
		}
		if (item.hasKey(type))
		{
			item.remove(type);
		}
		item.put(type, value);
		return this;
	}

	@Override
	public BaseQueryBuilder filter(BaseNestedQueryBuilder nb)
	{
		JsonData data=(JsonData) nb.getQuery();
		switch (nb.getOperator())
		{
			case must:
				this.must.put(data);
				break;
			case must_not:
				this.mustNot.put(data);
				break;
			case should:
				this.should.put(data);
				break;
		}
		return this;
	}

	@Override
	public void filter(BaseQueryBuilder.Operators operator, String propertyName, Integer value)
	{
		JsonData data=this.range.getFromPath(propertyName);
		if (null!=data)
		{
			this.range.remove(propertyName);
		}
		data=JsonData.createObject().put(operator, value);
		this.range.put(propertyName, data);
	}

	@Override
	public void filter(BaseQueryBuilder.Operators operator, String propertyName, DateTime value)
	{
		JsonData data=this.range.getFromPath(propertyName);
		if (null!=data)
		{
			this.range.remove(propertyName);
		}
		data=JsonData.createObject().put(operator, value);
		this.range.put(propertyName, data);
	}

	@Override
	public QueryResults execute()
	{
		return Environment.getInstance().getQueryFactory().get(this);
	}

	@Override
	public boolean delete()
	{
		return Environment.getInstance().getQueryFactory().delete(this);
	}

	@Override
	public void matchAll()
	{
		this.matchAll=JsonData.createObject().put("match_all", JsonData.createObject());
	}

	@Override
	public BaseQueryBuilder filerBoxBegin()
	{
		return this;
	}

	@Override
	public BaseQueryBuilder filerBoxEnd()
	{
		return this;
	}

	@Override
	public void nullifySort()
	{
		this.sort=null;
	}

	/**
	 * Aggregate the data selecting distinct values, start position is at 0 as it returns all results.
	 *
	 * @param property         The property to aggregate on.
	 * @param type             The BaseQueryBuilder.StringTypes.
	 * @param limit            Max aggregate items/buckets returned.
	 * @param includeVertex    Return the JSON data.
	 * @param includeIfMissing Include result if property aggregated is missing as its own bucket.
	 */
	@Override
	public void aggregations(String property, BaseQueryBuilder.StringTypes type, Integer limit, boolean includeVertex, BaseQueryBuilder.AggTypes includeIfMissing)
	{
		if (!StringX.isBlank(property) && (null==this.aggregations))
		{
			this.setStart(0);
			this.setLimit(0);
			this.useHttpRequest(true);
			this.setAggregated(true);
			this.aggregations=JsonData.createObject();
			JsonData aggFields=JsonData.createObject();
			this.aggTerms=JsonData.createObject();
			this.aggregations.put("agg_result", this.aggTerms);

			this.aggTerms.put("terms", aggFields);

			if ((null!=limit))
			{
				aggFields.put("size", limit);
			}

			aggFields.put("field", (type==BaseQueryBuilder.StringTypes.na) ? property : String.format("%s.%s", property, type.toString()));
			if (!Objects.equals(includeIfMissing, BaseQueryBuilder.AggTypes.doNotInclude))
			{
				//noinspection EnumSwitchStatementWhichMissesCases
				switch (includeIfMissing)
				{
					case number:
						aggFields.put("missing", EsQueryBuilder.NUMERICAL_NULL_VALUE);
						break;
					case string:
					default:
						aggFields.put("missing", "Null Value");
						break;
				}
			}
		}

		if (includeVertex && (null==this.aggVertex))
		{
			this.aggVertex=JsonData.createObject();
			this.aggTerms.put("aggs", this.aggVertex);
			JsonData post=JsonData.createObject();
			this.aggVertex.put("only_one_post", post);
			JsonData top=JsonData.createObject();
			post.put("top_hits", top);
			top.put("size", "1");
		}
	}

	@Override
	public void aggregations(JsonData aggs, boolean includeVertices)
	{
		String field=aggs.getString("agg_result", "terms", "field");
		String type="raw";
		if (StringX.contains(field, "."))
		{
			String tmp=StringX.getFirst(field, ".");
			type=StringX.getLast(field, ".");
			field=tmp;
		}
		BaseQueryBuilder.StringTypes stringType=(StringX.equalsIgnoreCase(type, "folded") ? BaseQueryBuilder.StringTypes.folded : BaseQueryBuilder.StringTypes.raw);

		int size=aggs.getInteger(0, "agg_result", "terms", "size");
		this.aggregations(field, stringType, size, includeVertices, BaseQueryBuilder.AggTypes.doNotInclude);
	}

	@Override
	public void aggregations(JsonData aggs)
	{
		this.aggregations=aggs;
	}

	@Override
	public void resetSort()
	{
		this.sort=JsonData.createArray();
	}

	@Override
	public void propertyMissingOrNull(String key)
	{
		this.mustNot.put("exists", JsonData.createObject().put("field", key));
	}

	@Override
	public void applyShould(Object statement)
	{
		if ((null!=this.should) && !this.should.isEmpty() && (statement instanceof JsonData))
		{
			this.apply(statement, "should", this.should);
		}
	}

	@Override
	public void applyMust(Object statement)
	{
		if ((null!=this.must) && !this.must.isEmpty() && (statement instanceof JsonData))
		{
			this.apply(statement, "must", this.should);
		}
	}

	@Override
	public void applyMustNot(Object statement)
	{
		if ((null!=this.mustNot) && !this.mustNot.isEmpty() && (statement instanceof JsonData))
		{
			this.apply(statement, "must_not", this.should);
		}
	}

	@Override
	public JsonData getSorted()
	{
		return this.sorted;
	}

	@Override
	public void setSorted(JsonData sorted)
	{
		this.sorted=sorted;
	}

	@Override
	public int getFilterSize()
	{
		return this.must.length()+this.should.length()+this.mustNot.length();
	}

	@Override
	public BaseNestedQueryBuilder getNestedBuilder()
	{
		return new EsNestedQueryBuilder();
	}

	@Override
	public JsonData getJson()
	{
		return (JsonData) this.getQuery();
	}

	@Override
	public JsonData getSort()
	{
		return this.sort;
	}

	@Override
	public boolean isAggregated()
	{
		return this.aggregated;
	}

	@Override
	public void setAggregated(boolean aggregated)
	{
		this.aggregated=aggregated;
	}

	@Override
	public Object getQuery()
	{
		JsonData result=JsonData.createObject();

		if (null==this.query)
		{
			JsonData query=JsonData.createObject();
			if (null!=this.matchAll)
			{
				query=this.matchAll;
			}
			else
			{
				JsonData filtered=JsonData.createObject();
				JsonData filter=JsonData.createObject();
				if (null!=this.startsWith)
				{
					query.put("prefix", this.startsWith);
				}

				JsonData bool=JsonData.createObject();
				if (!this.must.isEmpty())
				{
					bool.put("must", this.must);
				}
				if (!this.should.isEmpty())
				{
					bool.put("should", this.should);
				}
				if (!this.mustNot.isEmpty())
				{
					bool.put("must_not", this.mustNot);
				}
				if (!bool.isEmpty())
				{
					if ((!this.must.isEmpty()) && (!this.should.isEmpty()))
					{
						filter.put("bool", bool);
						filtered.put("filter", filter);
						query.put("filtered", filtered);
						String q=query.toString();
						query=new JsonData(StringX.replace(q, "\"match\"", "\"term\""));
					}
					else
					{
						query.put("bool", bool);
					}

					if (!this.range.isEmpty())
					{
						bool.put("filter", JsonData.createObject().put("range", this.range));
					}
				}
				else if (!this.range.isEmpty())
				{
					query.put("range", this.range);
				}
			}
			result.put("query", query);

			if ((null!=this.sort) && !this.sort.isEmpty())
			{
				result.put("sort", this.sort);
			}
		}
		else
		{
			result=this.query;
		}
		if (this.getStart()>0)
		{
			result.put("from", this.getStart());
		}
		if (((this.getApi()!=BaseQueryBuilder.API._count) && (this.getApi()!=BaseQueryBuilder.API._delete_by_query)))
		{
			result.put("size", (this.getLimit()>0) ? this.getLimit() : BaseQueryBuilder.DEFAULT_LIMIT);
			if ((null!=this.sort) && (!this.sort.isEmpty()))
			{
				result.put("sort", this.sort);
			}
		}

		if (null!=this.aggregations)
		{
			result.update("size", 0);
			result.put("aggs", this.aggregations);
		}
		if (null!=this.getSorted())
		{
			result.put("sorted", this.getSorted());
		}
		return result;
	}

	@Override
	public void setQuery(Object query)
	{
		if (query instanceof JsonData)
		{
			this.query=(JsonData) query;
			this.setAggregated(this.query.hasKey("aggs"));
			if (this.isAggregated())
			{
				this.useHttpRequest(true);
			}
		}
	}

	@Override
	public Object getRawQuery()
	{
		return this.query;
	}

	@Override
	public Object getDeleteQuery()
	{
		throw new NotImplementedException("Will not be implemented");
	}

	@Override
	public Object getLastFilterApplied()
	{
		return this.lastFilterApplied;
	}

	private void apply(Object statement, String operator, JsonData params)
	{
		JsonData qry=(JsonData) statement;
		JsonData op=this.getOperator(qry, operator);
		if (null!=op)
		{
			op.merge(params);
		}
	}

	private JsonData getOperator(JsonData statement, String operator)
	{
		JsonData result=null;
		JsonData q=statement.getFromPath("query");
		if (null!=q)
		{
			JsonData fltd=q.getFromPath("filtered");
			if (null==fltd)
			{
				fltd=JsonData.createObject();
				q.put("filtered", fltd);
			}
			JsonData flt=fltd.getFromPath("filter");
			if (null==flt)
			{
				flt=JsonData.createObject();
				fltd.put("filter", flt);
			}
			JsonData b=flt.getFromPath("bool");
			if (null==b)
			{
				b=JsonData.createObject();
				flt.put("bool", b);
			}
			result=b.getFromPath(operator);
			if (null==result)
			{
				result=JsonData.createArray();
				b.put(operator, result);
			}
		}
		return result;
	}

	private void addStartsWith(String propertyName, Object value)
	{
		this.startsWith=JsonData.createObject();
		//JsonData term = JsonData.createObject();
		//term.put("query", value);
		//term.put("type", "phrase_prefix");
		String property=propertyName;
		if (null!=this.propertyPartition)
		{
			property=String.format("%s/%s", ClassHelper.getClassKey(this.propertyPartition), propertyName);
		}
		this.startsWith.put(String.format("%s.%s", property, BaseQueryBuilder.StringTypes.starts_with), value);
		this.lastFilterApplied=this.startsWith;
	}

	private void add(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.StringTypes type, Object value, BaseQueryBuilder.Operators wordOperator)
	{
		if ((null==this.matchAll) || this.matchAll.isEmpty())
		{
			this.matchAll=null;
			if (operator==BaseQueryBuilder.Operators.matchAll)
			{
				this.matchAll=JsonData.createObject().put("match_all", JsonData.createObject());
				this.lastFilterApplied=this.matchAll;
			}
			else if (!StringX.isBlank(propertyName))
			{
				JsonData match=JsonData.createObject();
				String stringType=((type==BaseQueryBuilder.StringTypes.na) ? "" : String.format(".%s", type));

				String property=propertyName;
				if (null!=this.propertyPartition)
				{
					property=String.format("%s/%s", ClassHelper.getClassKey(this.propertyPartition), propertyName);
				}
				JsonData term=JsonData.createObject().put(String.format("%s%s", property, stringType), value);

				match.put("match", term);
				//noinspection SwitchStatementWithoutDefaultBranch
				switch (operator)
				{
					case should_wildcard:
						this.should.put(JsonData.createObject().put("wildcard", term));
						this.lastFilterApplied=this.should;
						break;
					case should_not_wildcard:
						this.mustNot.put(JsonData.createObject().put("wildcard", term));
						this.lastFilterApplied=this.mustNot;
						break;
					case wildcard:
						this.must.put(JsonData.createObject().put("wildcard", term));
						this.lastFilterApplied=this.must;
						break;
					case must:
						if (null!=wordOperator)
						{
							match=JsonData.createObject()
							              .put("match", JsonData.createObject()
							                                    .put("title", JsonData.createObject()
							                                                          .put("query", value).put("operator", wordOperator)));
						}
						this.must.put(match);
						this.lastFilterApplied=this.must;
						break;
					case must_not:
						this.mustNot.put(match);
						this.lastFilterApplied=this.mustNot;
						break;
					case should:
						if (null!=wordOperator)
						{
							match=JsonData.createObject()
							              .put("match", JsonData.createObject()
							                                    .put("title", JsonData.createObject()
							                                                          .put("query", value).put("operator", wordOperator)));
						}
						this.should.put(match);
						this.lastFilterApplied=this.should;
						break;
				}
			}
		}
	}

// Methods
	/**
	 * Build an ElasticSearch index query.
	 *
	 * @param store     The Class store.
	 * @param partition The index type
	 * @param start     paging start
	 * @param limit     max number of items
	 * @return A query builder.
	 */
	@SuppressWarnings("unused")
	public static EsQueryBuilder getBuilder(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, int start, int limit)
	{
		return new EsQueryBuilder(store, partition, start, limit);
	}
}
