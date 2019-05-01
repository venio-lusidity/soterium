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

package com.lusidity.domains.acs.security.authorization;

import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@SuppressWarnings("EqualsAndHashcode")
@AtSchemaClass(name="Permission", discoverable=false, description="An approval of a mode of access to a resource.")
public class Permission extends BaseDomain
{
	public enum Types{
		read,
		write,
		delete,
		scope,
		denied
	}
	private KeyData<Double> identifier=null;

// Constructors
	public Permission()
	{
		super();
	}

	@SuppressWarnings("unused")
	public Permission(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Overrides
	@Override
	public boolean equals(Object o)
	{
		boolean result=super.equals(o);
		if (!result)
		{
			if (o instanceof Permission)
			{
				Permission other=(Permission) o;
				result=this.fetchIdentifier().equals(other.fetchIdentifier());
			}
		}
		return result;
	}

	public KeyData<Double> fetchIdentifier()
	{
		if (null==this.identifier)
		{
			this.identifier=new KeyData<>(this, "identifier", Double.class, false, null);
		}
		return this.identifier;
	}

// Methods
	public static Permission create(Double id, String title)
	{
		Permission permission=new Permission();
		permission.fetchIdentifier().setValue(id);
		permission.fetchTitle().setValue(title);
		return permission;
	}
}
