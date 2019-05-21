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

package com.lusidity.domains.cache;

import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="Test Class", discoverable=false, description="A class used to test caching.")
public class TestCache extends BaseDomain
{
// Constructors
	public TestCache()
	{
		super();
	}

	@SuppressWarnings("unused")
	public TestCache(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}
}