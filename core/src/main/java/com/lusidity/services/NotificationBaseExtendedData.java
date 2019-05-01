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
import com.lusidity.domains.electronic.Notification;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.process.workflow.WorkflowNotification;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class NotificationBaseExtendedData extends BaseExtendedData
{
	// Constructors
	public NotificationBaseExtendedData(UserCredentials userCredentials){
		super();
	}

	public NotificationBaseExtendedData(){
		super();
	}

	@Override
	public void getExtendedData(UserCredentials userCredentials, DataVertex context, JsonData result, Map<String, Object> options)
	{
		if (ClassX.isKindOf(context, Notification.class))
		{
			Notification notification=(Notification) context;
			result.put(notification.getInitiators().getKey(), notification.getInitiators().toJson(false, 1000));
			Collection<Person> people=notification.getPeople();
			if (!people.isEmpty())
			{
				JsonData item=JsonData.createObject();
				JsonData items=JsonData.createArray();
				for (Person person : people)
				{
					items.put(person.toJson(false));
				}
				item.put("count", items.length());
				item.put("collectionType", Collection.class.getName());
				item.put("expectedType", Person.class.getName());
				item.put("results", items);
				result.put("/acs/security/base_principal/peoples", item);
			}
			result.put(notification.getTargets().getKey(), notification.getTargets().toJson(false, 1000));
		}
	}

	@Override
	public Collection<Class<? extends DataVertex>> forTypes()
	{
		Collection<Class<? extends DataVertex>> result = new ArrayList<>();
		result.add(WorkflowNotification.class);
		return result;
	}
}
