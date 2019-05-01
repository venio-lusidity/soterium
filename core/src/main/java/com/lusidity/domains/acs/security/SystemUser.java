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

import com.lusidity.data.DataVertex;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

@AtSchemaClass(name="System User", discoverable=false, writable=false)
public class SystemUser extends BasePrincipal
{
	// Constructors
	public SystemUser()
	{
		super();
	}
	public SystemUser(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof SystemUser);
	}

	@Override
	public UserCredentials getCredentials()
	{
		if(null==this.credentials){
			this.setCredentials(new SystemCredentials());
		}
		return this.credentials;
	}

	// Methods
	public static SystemUser getInstance()
	{
		SystemUser result = new SystemUser();
		result.fetchTitle().setValue("System");
		return result;
	}

	public boolean isAdmin(DataVertex context, boolean validateCredentials)
	{
		return true;
	}

	public boolean canRead(DataVertex context, boolean validateCredentials)
	{
		return true;
	}

	public boolean canWrite(DataVertex context, boolean validateCredentials)
	{
		return true;
	}

	public boolean canDelete(DataVertex context, boolean validateCredentials)
	{
		return true;
	}


}
