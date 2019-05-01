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

package com.lusidity.domains.people.discovery;

import com.lusidity.discover.interfaces.BaseItem;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

public class PersonItem extends BaseItem
{
// Constructors
	public PersonItem(String phrase, Person person, UserCredentials credentials, String key, Object value, int hits)
	{
		super(phrase, person, credentials, key, value, hits);
	}

// Overrides
	@Override
	public JsonData toJson()
	{
		JsonData result=super.toJson();
		result.remove("description");

		Person person=(Person) this.getVertex();
		String desc=person.getBio();
		result.put("description", desc);
		return result;
	}
}
