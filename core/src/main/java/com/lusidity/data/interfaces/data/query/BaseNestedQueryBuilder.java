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

public abstract class BaseNestedQueryBuilder
{
	private BaseQueryBuilder.Operators operator = null;

	public abstract BaseNestedQueryBuilder filter(BaseQueryBuilder.Operators operator, String propertyName, BaseQueryBuilder.StringTypes type, Object value);

// Getters and setters
	public abstract Object getQuery();

	public abstract JsonData getMust();

	public abstract JsonData getMustNot();

	public abstract JsonData getShould();

	public abstract BaseQueryBuilder.Operators getOperator();
}
