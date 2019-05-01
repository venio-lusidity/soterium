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

import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.BaseQueryDateFormat;
import com.lusidity.framework.json.JsonData;
import org.joda.time.DateTime;

public class EsQueryDateFormat extends BaseQueryDateFormat
{
	public enum DateMath
	{
		y,
		M,
		w,
		d,
		h,
		H,
		m,
		s,
		now
	}

	private DateTime targetDate=null;
	private DateTime dateTimeFrom=null;
	private DateTime dateTimeTo=null;

	// Constructors
	public EsQueryDateFormat()
	{
		super();
	}

	public EsQueryDateFormat(DateTime dateTime)
	{
		super();
		this.targetDate=dateTime;
	}

	public EsQueryDateFormat(DateTime dateTimeFrom, DateTime dateTimeTo)
	{
		super();
		this.dateTimeFrom=dateTimeFrom;
		this.dateTimeTo=dateTimeTo;
	}

	// Overrides
	@Override
	public JsonData getToday()
	{
		/*returns results for today (calendar day - NOT last 24 hours)*/
		JsonData result=JsonData.createObject();
		String key=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*now/d gives you the start of this calendar day*/
		String value=EsQueryDateFormat.DateMath.now+"/"
		             +EsQueryDateFormat.DateMath.d;
		result.put(key, value);
		return result;
	}

	@Override
	public JsonData getLastDay()
	{
		/*returns results for last 24 hours*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*now-24h gives you the last 24 hours*/
		String value1=EsQueryDateFormat.DateMath.now+"-24"+EsQueryDateFormat.DateMath.h;
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lte);
		String value2=String.valueOf(EsQueryDateFormat.DateMath.now);
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getSameCalendarDay()
	{
		/*returns results for same day as parameter date (calendar day - NOT last 24 hours)*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*when used with gte, paramDate||/d gives you the start of the calendar day*/
		String value1=this.targetDate.toString()+"||/"+EsQueryDateFormat.DateMath.d;
		/*when used with lt, paramDate||+1d gives you the start of next calendar day*/
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lt);
		String value2=this.targetDate.toString()+"||+1"+EsQueryDateFormat.DateMath.d
		              +"/"+EsQueryDateFormat.DateMath.d;
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getLastHour()
	{
		/*returns results for last 60 minutes*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*now-60m gives you the last 60 minutes*/
		String value1=EsQueryDateFormat.DateMath.now+"-60"+EsQueryDateFormat.DateMath.m;
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lte);
		String value2=String.valueOf(EsQueryDateFormat.DateMath.now);
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getSameCalendarWeek()
	{
		/*returns results for same calendar week as parameter date (NOT last 7 days)*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*when used with gte, paramDate||/w gives you the start of the calendar week*/
		String value1=this.targetDate.toString()+"||/"+EsQueryDateFormat.DateMath.w;
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lt);
		/*when used with lt, paramDate||+1w/w gives you the start of next calendar week*/
		String value2=this.targetDate.toString()+"||+1"+EsQueryDateFormat.DateMath.w
		              +"/"+EsQueryDateFormat.DateMath.w;
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getLastWeek()
	{
		/*returns results for last 7 days*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*now-7d gives you the last seven days*/
		String value1=EsQueryDateFormat.DateMath.now+"-7"+EsQueryDateFormat.DateMath.d;
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lte);
		String value2=String.valueOf(EsQueryDateFormat.DateMath.now);
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getSameCalendarMonth()
	{
		/*returns results for same calendar month as parameter date (NOT last 30 days)*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*when used with gte, paramDate||/M gives you the start of the calendar month*/
		String value1=this.targetDate.toString()+"||/"+EsQueryDateFormat.DateMath.M;
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lt);
		/*when used with lt, paramDate||+1M/M gives you the start of next calendar month*/
		String value2=this.targetDate.toString()+"||+1"+EsQueryDateFormat.DateMath.M
		              +"/"+EsQueryDateFormat.DateMath.M;
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getLastMonth()
	{
		/*returns results for same last 30days*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*now-30d gives you last 30 days. Can also use now-1M */
		String value1=EsQueryDateFormat.DateMath.now+"-30"+EsQueryDateFormat.DateMath.d;
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lte);
		String value2=String.valueOf(EsQueryDateFormat.DateMath.now);
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getSameCalendarYear()
	{
		/*returns results for same calendar year as parameter date (NOT last 365 days)*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*when used with gte, paramDate||/y gives you the start of the calendar year*/
		String value1=this.targetDate.toString()+"||/"+EsQueryDateFormat.DateMath.y;
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lt);
		/*when used with lt, paramDate||+1y/y gives you the start of next calendar year*/
		String value2=this.targetDate.toString()+"||+1"+EsQueryDateFormat.DateMath.y
		              +"/"+EsQueryDateFormat.DateMath.y;
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getLastYear()
	{
		/*returns results for last year*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		/*now-1y give you results for last year. Use instead of now-365d to account for leap year*/
		String value1=String.format("%s-1%s", EsQueryDateFormat.DateMath.now, EsQueryDateFormat.DateMath.y);
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lte);
		String value2=String.valueOf(EsQueryDateFormat.DateMath.now);
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getBetween()
	{
		/*returns all results between the two parameter dates*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		String value1=this.dateTimeFrom.toString();
		String nextKey=String.valueOf(BaseQueryBuilder.RangeTypes.lte);
		String value2=this.dateTimeTo.toString();
		result.put(firstKey, value1);
		result.put(nextKey, value2);
		return result;
	}

	@Override
	public JsonData getBefore()
	{
		/*returns results for all BEFORE the parameter date*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.lt);
		String value1=this.targetDate.toString();
		result.put(firstKey, value1);
		return result;
	}

	@Override
	public JsonData getOnOrBefore()
	{
		/*returns results for all BEFORE the parameter date*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.lte);
		String value1=this.targetDate.toString();
		result.put(firstKey, value1);
		return result;
	}

	@Override
	public JsonData getAfter()
	{
		/*returns results for all AFTER the parameter date*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gt);
		String value1=this.targetDate.toString();
		result.put(firstKey, value1);
		return result;
	}

	@Override
	public JsonData getOnOrAfter()
	{
		/*returns results for all AFTER the parameter date*/
		JsonData result=JsonData.createObject();
		String firstKey=String.valueOf(BaseQueryBuilder.RangeTypes.gte);
		String value1=this.targetDate.toString();
		result.put(firstKey, value1);
		return result;
	}


	public void setTargetDate(DateTime targetDate)
	{
		this.targetDate=targetDate;
	}

	public void setDateTimeFrom(DateTime dateTimeFrom)
	{
		this.dateTimeFrom=dateTimeFrom;
	}

	public void setDateTimeTo(DateTime dateTimeTo)
	{
		this.dateTimeTo=dateTimeTo;
	}

}
