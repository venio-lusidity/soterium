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

package com.lusidity.security;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.common.PhoneNumber;
import com.lusidity.domains.people.Person;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({
	"UtilityClassWithoutPrivateConstructor",
	"NonFinalUtilityClass"
})
public class IdentityHelper
{
	// Methods
	public static Identity getOrCreateApiIdentity(JsonData data, String apiKey, String provider, String identifier)
		throws
		ApplicationException
	{
		Identity result=null;
		if (Identity.isMatch(apiKey, "x509", identifier))
		{
			Person person=VertexFactory.getInstance().get(Person.class, data.getUri("principalUri"));
			if (null!=person)
			{
				result=IdentityHelper.getOrCreateApiIdentity(person, apiKey, provider, identifier);
			}
		}
		return result;
	}

	@SuppressWarnings("UnusedParameters")
	public static Identity getOrCreateApiIdentity(Person person, String apiKey, String provider, String identifier)
		throws ApplicationException
	{
		Identity result=null;
		if (Identity.isMatch(apiKey, "x509", identifier))
		{
			for (Identity identity : person.getIdentities())
			{
				String check=identity.fetchIdentifier().getValue();
				if (StringX.equals(check, apiKey))
				{
					result=identity;
					break;
				}
			}

			if (null==result)
			{
				result=Identity.create("x509", apiKey, Identity.LoginType.apiKey);
				if (!result.getPrincipals().add(person))
				{
					result.delete();
					result=null;
				}
			}
		}
		return result;
	}

	public static Person getOrCreate(JsonData data, Identity.LoginType loginType)
		throws ApplicationException
	{
		Collection<BaseContactDetail> contactDetails=new ArrayList<>();
		JsonData categories=data.getFromPath("categories");
		if ((null!=categories) && categories.isJSONArray())
		{
			for (Object o : categories)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=JsonData.create(o);
					BaseContactDetail.CategoryTypes category=item.getEnum(BaseContactDetail.CategoryTypes.class, "category");
					String value=item.getString("value");
					//noinspection SwitchStatementWithTooManyBranches
					switch (category)
					{

						case home_email:
						case work_email:
							Email email=new Email(category, value);
							contactDetails.add(email);
							break;
						case work_cell:
						case home_cell:
						case work_dsn:
						case home_fax:
						case work_fax:
						case home_mobile:
						case work_mobile:
						case home_phone:
						case work_phone:
							String ext=data.getString("ext");
							PhoneNumber phoneNumber=new PhoneNumber(category, value, ext);
							contactDetails.add(phoneNumber);
							break;
					}
				}
			}
		}
		return IdentityHelper.getOrCreate(
			data.getString("provider"),
			data.getString("identifier"),
			data.getString("title"),
			data.getString("firstName"),
			data.getString("middleName"),
			data.getString("lastName"),
			contactDetails,
			loginType);
	}

	/**
	 * Create a new user and associated identity.
	 *
	 * @param provider       Identity provider (e.g., "Google").
	 * @param identifier     Provider-specific identifier.
	 * @param title          A User's prefix to their name.
	 * @param firstName      User's human-readable name.
	 * @param middleName     User's human-readable name.
	 * @param lastName       User's human-readable name.
	 * @param contactDetails Contact information for a user..
	 * @return Newly-created user.
	 */
	@SuppressWarnings({
		"FeatureEnvy",
		"OverlyComplexMethod"
		,
		"OverlyLongMethod"
	})
	public static Person getOrCreate(String provider, String identifier, String title, String firstName, String middleName,
	                                 String lastName, Collection<BaseContactDetail> contactDetails, Identity.LoginType loginType)
		throws ApplicationException
	{

		Person result;
		try
		{
			//  Use existing identity if possible
			Identity identity=Identity.get(provider, identifier);

			//  Get existing user associated with entity
			IPrincipal principal=null;
			if (null!=identity)
			{
				principal=identity.getPrincipal();
			}
			BaseContactDetail email = null;
			// If the identity does not have an associated principal there could be a user with the specified email address.
			if ((null==principal) && (null!=contactDetails))
			{
				for (BaseContactDetail detail : contactDetails)
				{
					if ((detail.fetchCategory().getValue()==BaseContactDetail.CategoryTypes.home_email) ||
					    (detail.fetchCategory().getValue()==BaseContactDetail.CategoryTypes.work_email))
					{
						email = detail;
						principal=Person.Queries.getByEmail(detail.fetchValue().getValue());
						if (null!=principal)
						{
							break;
						}
					}
				}
			}

			if (null==principal)
			{
				if (StringX.isBlank(firstName))
				{
					//noinspection ThrowCaughtLocally
					throw new ApplicationException("The user's firstName cannot be empty.");
				}

				result=new Person();
			}
			else
			{
				result=ClassHelper.as((DataVertex) principal, Person.class);
				result.getContactDetails().clearAndDelete();
				if(null!=email)
				{
					result.getContactDetails().add(email);
				}
			}

			if(!result.hasId())
			{
				result.fetchFirstName().setValue(firstName);
				result.fetchMiddleName().setValue(middleName);
				result.fetchLastName().setValue(lastName);
				result.save();
			}

			if (null==identity)
			{
				//  Create a new identity if needed
				identity=Identity.create(provider, identifier, loginType);
			}

			result.getIdentities().add(identity);

			JsonData groups=Environment.getInstance().getConfig().getDefaultGroups();
			if (null!=groups)
			{
				for (Object obj : groups)
				{
					if (obj instanceof Double)
					{
						Double itemId=(Double) obj;
						Group r=Group.Queries.getGroup(itemId);
						if (!Group.isInGroup(result, r))
						{
							r.getPrincipals().add(result);
						}
					}
				}
			}

			if ((null==principal) && (null!=contactDetails))
			{
				for (BaseContactDetail detail : contactDetails)
				{
					detail.save();
					result.getContactDetails().add(detail);
				}
			}
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}

		return result;
	}

	public static Person getOrCreate(JsonData data, Collection<BaseContactDetail> contactDetails, Identity.LoginType loginType)
		throws ApplicationException
	{
		return IdentityHelper.getOrCreate(data.getString("provider"), data.getString("identifier"), data.getString("title"), data.getString("firstName"),
			data.getString("middleName"), data.getString("lastName"), contactDetails, loginType
		);
	}
}
