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

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;
import org.restlet.Request;
import org.restlet.data.ClientInfo;

import java.net.URI;

public class PersonCredentials implements UserCredentials
{
	private final Person principal;
	private final Identity identity;

	public PersonCredentials(Person person, Identity identity)
	{
		this.principal = person;
		this.identity = identity;
	}

	@Override
	public Boolean isRegistered()
	{
		return false;
	}

	@Override
	public Boolean isValidated()
	{
		return false;
	}

	@Override
	public Boolean isAuthenticated()
	{
		return false;
	}

	@Override
	public Boolean isServerCertificate()
	{
		return null;
	}

	@Override
	public JsonData toJson()
	{
		return null;
	}

	@Override
	public UserActivity.OperationTypes getActivity()
	{
		return LogEntry.OperationTypes.none;
	}

	@Override
	public Request getRequest()
	{
		return null;
	}

	@Override
	public Identity getIdentity()
	{
		return this.identity;
	}

	@Override
	public ClientInfo getClientInfo()
	{
		return null;
	}

	@Override
	public String getCommonName()
	{
		return null;
	}

	@Override
	public String getFirstName()
	{
		return this.principal.fetchFirstName().getValue();
	}

	@Override
	public String getIdentifier()
	{
		return this.principal.fetchId().getValue();
	}

	@Override
	public String getLastName()
	{
		return this.principal.fetchLastName().getValue();
	}

	@Override
	public String getMiddleName()
	{
		return this.principal.fetchMiddleName().getValue();
	}

	@Override
	public String getCountry()
	{
		return null;
	}

	@Override
	public String getOrigin()
	{
		return null;
	}

	@Override
	public String getReferrer()
	{
		return null;
	}

	@Override
	public String getOrganizationalUnit1()
	{
		return null;
	}

	@Override
	public String getOrganizationalUnit2()
	{
		return null;
	}

	@Override
	public String getProvider()
	{
		return "System";
	}

	@Override
	public URI getPrincipalUri()
	{
		return this.principal.getUri();
	}

	@Override
	public BasePrincipal getPrincipal()
	{
		return this.principal;
	}

	@Override
	public void setActivity(UserActivity.OperationTypes activity)
	{

	}
}
