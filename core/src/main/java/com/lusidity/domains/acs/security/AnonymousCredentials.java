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

package com.lusidity.domains.acs.security;

import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;
import org.restlet.Request;
import org.restlet.data.ClientInfo;

import java.net.URI;

public class AnonymousCredentials implements UserCredentials
{
	private BasePrincipal principal=null;

// Overrides
	@Override
	public Boolean isRegistered()
	{
		return this.getPrincipal().isAdmin(true);
	}

	@Override
	public Boolean isValidated()
	{
		return this.getPrincipal().isAdmin(true);
	}

	@Override
	public Boolean isAuthenticated()
	{
		return true;
	}

	@Override
	public Boolean isServerCertificate()
	{
		return false;
	}

	@Override
	public JsonData toJson()
	{
		return null;
	}

	private LogEntry.OperationTypes activity = UserActivity.OperationTypes.none;

	@Override
	public UserActivity.OperationTypes getActivity()
	{
		return this.activity;
	}

	@Override
	public void setActivity(UserActivity.OperationTypes activity)
	{
		this.activity = activity;
	}

	@Override
	public Request getRequest()
	{
		return null;
	}

	@Override
	public Identity getIdentity()
	{
		return null;
	}

	@Override
	public ClientInfo getClientInfo()
	{
		return null;
	}

	@Override
	public String getCommonName()
	{
		return "Anonymous";
	}

	@Override
	public String getFirstName()
	{
		return "Anonymous";
	}

	@Override
	public String getIdentifier()
	{
		return "AnonymousAnonymousAnonymous";
	}

	@Override
	public String getLastName()
	{
		return "user";
	}

	@Override
	public String getMiddleName()
	{
		return null;
	}

	@Override
	public String getCountry()
	{
		return "US";
	}

	@Override
	public String getOrigin()
	{
		return "System";
	}

	@Override
	public String getReferrer()
	{
		return "System";
	}

	@Override
	public String getOrganizationalUnit1()
	{
		return "System";
	}

	@Override
	public String getOrganizationalUnit2()
	{
		return "System";
	}

	@Override
	public String getProvider()
	{
		return "local";
	}

	@Override
	public URI getPrincipalUri()
	{
		return null;
	}

	@Override
	public BasePrincipal getPrincipal()
	{
		if (null==this.principal)
		{
			this.principal=new AnonymousUser();
			this.principal.fetchTitle().setValue(this.getCommonName());
			this.principal.setCredentials(this);
		}
		return this.principal;
	}
}
