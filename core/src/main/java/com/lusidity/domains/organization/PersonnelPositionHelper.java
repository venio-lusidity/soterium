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

package com.lusidity.domains.organization;

import com.lusidity.data.types.names.WesternName;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.common.PhoneNumber;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

@SuppressWarnings({
	"UtilityClassWithoutPrivateConstructor",
	"NonFinalUtilityClass"
})
public class PersonnelPositionHelper
{
	// Methods
	public static void addSupervisor(PersonnelPosition position, Organization organization, JsonData data)
		throws Exception
	{
		String email=data.getString("supervisorEmail");
		PersonnelPositionHelper.addSupervisor(position, organization, email, data);
	}

	public static void addSupervisor(PersonnelPosition subordinate, Organization organization, String email, JsonData data)
		throws Exception
	{
		Person supervisor=Person.Queries.getByEmail(email);
		if (null==supervisor)
		{
			WesternName wn=WesternName.fromEmail(email);
			supervisor=new Person();
			String fn = PersonnelPositionHelper.getValue("svFirstName", wn.getFirstName(), data);
			String mn = PersonnelPositionHelper.getValue("svMiddleName", wn.getMiddleName(), data);
			String ln = PersonnelPositionHelper.getValue("svLastName", wn.getLastName(), data);
			supervisor.fetchFirstName().setValue(fn);
			supervisor.fetchMiddleName().setValue(mn);
			supervisor.fetchLastName().setValue(ln);
			supervisor.save();

			Email emailInfo=new Email(BaseContactDetail.CategoryTypes.work_email, email);
			supervisor.getContactDetails().add(emailInfo);
			String phone = data.getString("svPhone");
			String ext = data.getString("svExtension");
			if(!StringX.isBlank(phone)){
				PhoneNumber phoneNumber = new PhoneNumber(BaseContactDetail.CategoryTypes.work_phone, phone, ext);
				supervisor.getContactDetails().add(phoneNumber);
			}
		}

		PersonnelPosition supervisorPosition=null;
		for (BasePrincipal basePrincipal : supervisor.getParentPrincipals())
		{
			if (ClassX.isKindOf(basePrincipal, PersonnelPosition.class))
			{
				supervisorPosition=(PersonnelPosition) basePrincipal;
				break;
			}
		}

		if (null==supervisorPosition)
		{
			supervisorPosition=new PersonnelPosition();
			supervisorPosition.fetchTitle().setValue("Placeholder");
			supervisorPosition.save();

			organization.getPositions().add(supervisorPosition);
		}

		supervisorPosition.getPrincipals().add(supervisor);
		supervisorPosition.getSubordinates().add(subordinate);
	}

	private static String getValue(String key, String defaultValue, JsonData data)
	{
		String result = data.getString(key);
		if(StringX.isBlank(result)){
			result = defaultValue;
		}
		return result;
	}
}
