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

import com.lusidity.framework.json.JsonData;

public abstract class BaseQueryDateFormat
{
	public abstract JsonData getToday();

	public abstract JsonData getLastDay();

	public abstract JsonData getSameCalendarDay();

	public abstract JsonData getLastHour();

	public abstract JsonData getSameCalendarWeek();

	public abstract JsonData getLastWeek();

	public abstract JsonData getSameCalendarMonth();

	public abstract JsonData getLastMonth();

	public abstract JsonData getSameCalendarYear();

	public abstract JsonData getLastYear();

	public abstract JsonData getBetween();

	public abstract JsonData getBefore();
	public abstract JsonData getOnOrBefore();

	public abstract JsonData getAfter();
	public abstract JsonData getOnOrAfter();
}
