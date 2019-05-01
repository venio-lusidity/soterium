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

package com.lusidity.data.json;


import com.lusidity.framework.system.EnumX;

/**
 * Level of detail for a data object.
 */
public enum DataLevel
{
	Tiny /* only title, types, and URI */,
	Summary /* title, types, description, URI - useful for search results view */,
	Complete /* complete dump of entire object (may produce a large JSON object) */,
	Partial, /* all properties but no relationships.*/
	State /* entity state only (for use in polling */;

// Methods
	public static DataLevel parse(String value)
	{
		return EnumX.parse(DataLevel.class, value);
	}
}
