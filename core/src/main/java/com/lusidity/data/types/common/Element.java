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

package com.lusidity.data.types.common;

import com.lusidity.data.json.DataLevel;
import com.lusidity.framework.json.JsonData;

import java.net.URI;

public interface Element
{
	/**
	 * Get a JSON representation of this element.
	 *
	 * @return JSON representation of this element.
	 */
	JsonData toJson(DataLevel dataLevel);

	/**
	 * Load this element from its JSON representation.
	 *
	 * @param jsonData JSON representation of this element (as returned by @link toJson).
	 */
	void fromJson(JsonData jsonData);

	/**
	 * Get an indexable representation of this element.
	 *
	 * @return Indexable representation of element, or null if this element cannot be indexed.
	 */
	Object toIndex();

	void fromJson(JsonData jsonData, URI sourceUri);
}
