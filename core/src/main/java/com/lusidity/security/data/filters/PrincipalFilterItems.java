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

package com.lusidity.security.data.filters;

import java.util.HashMap;
import java.util.Map;

public class PrincipalFilterItems
{
	private Map<String, PrincipalFilterItem> underlying = new HashMap<>();

	public PrincipalFilterItems(){
		super();
	}

	public PrincipalFilterItem put(String key, PrincipalFilterItem item){
		return this.underlying.put(key, item);
	}

	public PrincipalFilterItem remove(String key){
		return this.underlying.remove(key);
	}

	public PrincipalFilterItem get(String key)
	{
		return this.underlying.get(key);
	}

	public Map<String, PrincipalFilterItem> getUnderlying()
	{
		return this.underlying;
	}
}
