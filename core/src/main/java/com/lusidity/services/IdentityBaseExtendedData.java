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

package com.lusidity.services;

import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class IdentityBaseExtendedData extends BaseExtendedData
{
	// Constructors
	public IdentityBaseExtendedData(){
		super();
	}

	@Override
	public void getExtendedData(UserCredentials userCredentials, DataVertex context, JsonData result, Map<String, Object> options)
	{
		if (context instanceof Identity)
		{
			Identity entity=(Identity) context;
			BasePrincipal principal=entity.getPrincipal();
			if (null!=principal)
			{
				result.remove("title");
				result.put("title", principal.fetchTitle().getValue());
				result.put("principalUri", principal.getUri());

				if (principal instanceof Person)
				{
					Person person=(Person) principal;
					JsonData items=JsonData.createArray();
					for (BaseContactDetail bcd : person.getContactDetails())
					{
						JsonData item=bcd.toJson(false);
						items.put(item);
					}
					result.put(person.getContactDetails().getKey(), items);
				}
			}
		}
	}

	@Override
	public Collection<Class<? extends DataVertex>> forTypes()
	{
		Collection<Class<? extends DataVertex>> result = new ArrayList<>();
		result.add(Identity.class);
		return result;
	}
}
