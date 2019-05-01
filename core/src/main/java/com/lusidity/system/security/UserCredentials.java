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
import com.lusidity.framework.json.JsonData;
import org.restlet.Request;
import org.restlet.data.ClientInfo;

import java.net.URI;

public interface UserCredentials
{

	Boolean isRegistered();

	Boolean isValidated();

	Boolean isAuthenticated();

	Boolean isServerCertificate();

	JsonData toJson();

	UserActivity.OperationTypes getActivity();

	void setActivity(UserActivity.OperationTypes activity);

	// Getters and setters
	Request getRequest();

	Identity getIdentity();

	@SuppressWarnings("UnusedDeclaration")
	ClientInfo getClientInfo();

	String getCommonName();

	String getFirstName();

	String getIdentifier();

	String getLastName();

	String getMiddleName();

	@SuppressWarnings("UnusedDeclaration")
	String getCountry();

	String getOrigin();

	String getReferrer();

	@SuppressWarnings("UnusedDeclaration")
	String getOrganizationalUnit1();

	@SuppressWarnings("UnusedDeclaration")
	String getOrganizationalUnit2();

	@SuppressWarnings("UnusedDeclaration")
	String getProvider();

	URI getPrincipalUri();

	BasePrincipal getPrincipal();
}
