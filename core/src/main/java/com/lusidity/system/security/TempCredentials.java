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

package com.lusidity.system.security;

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.json.JsonData;
import org.restlet.Request;
import org.restlet.data.ClientInfo;

import java.net.URI;

public class TempCredentials implements UserCredentials
{
	private final Identity identity;
	private final BasePrincipal principal;

	// Constructors
	public TempCredentials(Identity identity, BasePrincipal principal){
		super();
		this.identity = identity;
		this.principal = principal;
	}

	@Override
	public Boolean isRegistered()
	{
		return (null!=this.identity);
	}

	@Override
	public Boolean isValidated()
	{
		return this.isRegistered();
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
		return null;
	}

	@Override
	public String getIdentifier()
	{
		return null;
	}

	@Override
	public String getLastName()
	{
		return null;
	}

	@Override
	public String getMiddleName()
	{
		return null;
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
		return null;
	}

	@Override
	public URI getPrincipalUri()
	{
		return (null==this.principal) ? null : this.getPrincipal().getUri();
	}

	@Override
	public BasePrincipal getPrincipal()
	{
		if(null!=this.principal){
			this.principal.setCredentials(this);
		}
		return this.principal;
	}
}
