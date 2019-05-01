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

package com.lusidity.discover.generic;

import com.lusidity.data.DataVertex;
import com.lusidity.discover.interfaces.BaseItem;
import com.lusidity.system.security.UserCredentials;

public class GenericItem extends BaseItem
{
// Constructors
	public GenericItem(String phrase, DataVertex vertex, UserCredentials credentials, String key, Object value, int hits)
	{
		super(phrase, vertex, credentials, key, value, hits);
	}
}
