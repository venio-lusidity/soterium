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
import com.lusidity.data.interfaces.data.query.BaseNestedQueryBuilder;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.IndexHelper;

import java.security.InvalidParameterException;
import java.util.Objects;

public class EsNestedQueryBuilder extends BaseNestedQueryBuilder
{
	private BaseQueryBuilder.Operators operator=null;
	private JsonData startsWith=null;
	private JsonData must=JsonData.createArray();
	private JsonData should=JsonData.createArray();
	private JsonData mustNot=JsonData.createArray();
	private JsonData range=JsonData.createObject();

	// Overrides
	@Override
	public BaseNestedQueryBuilder filter(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.StringTypes type, Object value)
	{
		if ((null==operator))
		{
			throw new InvalidParameterException("The operator must not be null.");
		}

		if (Objects.equals(operator, BaseQueryBuilder.Operators.matchAll))
		{
			throw new InvalidParameterException("Operator matchAll is not used in nested queries.");
		}

		if (null==type)
		{
			throw new InvalidParameterException("The type must not be null.");
		}
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
				this.add(operator, propertyName, (finalValue instanceof String) ? type : BaseQueryBuilder.StringTypes.na, finalValue);
				break;
			default:
				this.add(operator, propertyName, (finalValue instanceof String) ? type : BaseQueryBuilder.StringTypes.na, finalValue);
				break;
		}

		return this;
	}

	@Override
	public Object getQuery()
	{
		JsonData result=JsonData.createObject();
		JsonData query=JsonData.createObject();
		JsonData filtered=JsonData.createObject();
		JsonData filter=JsonData.createObject();
		if (null!=this.startsWith)
		{
			query.put("match_phrase_prefix", this.startsWith);
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
		}

		if (!this.range.isEmpty())
		{
			if (bool.isEmpty())
			{
				filter.put("range", this.range);
				JsonData score=JsonData.createObject();
				score.put("filter", filter);
				query.put("constant_score", score);
			}
			else
			{
				bool.put("range", this.range);
			}
		}
		JsonData nested=JsonData.createObject();
		nested.put("query", query);
		result.put("nested", nested);
		return result;
	}

	@Override
	public JsonData getMust()
	{
		JsonData result=null;
		if (!this.must.isEmpty())
		{
			JsonData.createObject();
			JsonData bool=JsonData.createObject();
			JsonData items=JsonData.createObject();
			items.put("must", this.must);
			bool.put("bool", items);
			result.put("query", result);
		}
		return result;
	}

	@Override
	public JsonData getMustNot()
	{
		return this.mustNot;
	}

	@Override
	public JsonData getShould()
	{
		return this.should;
	}

	@Override
	public BaseQueryBuilder.Operators getOperator()
	{
		return this.operator;
	}

	private void addStartsWith(String propertyName, Object value)
	{
		this.startsWith=JsonData.createObject();
		JsonData term=JsonData.createObject();
		term.put("query", value);
		term.put("type", "phrase_prefix");
		//term.put("max_expansions", (this.getLimit()>0) ? this.getLimit() : BaseQueryBuilder.MAX_EXPANSIONS);
		this.startsWith.put(String.format("%s.%s", propertyName, BaseQueryBuilder.StringTypes.starts_with), term);
	}

	private void add(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.StringTypes type, Object value)
	{
		if (!StringX.isBlank(propertyName))
		{
			JsonData match=JsonData.createObject();
			String stringType=((type==BaseQueryBuilder.StringTypes.na) ? "" : String.format(".%s", type));
			JsonData term=JsonData.createObject().put(String.format("%s%s", propertyName, stringType), value);
			match.put("match", term);
			switch (operator)
			{
				case must:
					this.must.put(match);
					break;
				case must_not:
					this.mustNot.put(match);
					break;
				case should:
					this.should.put(match);
			}
		}
	}
}
