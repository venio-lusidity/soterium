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

public class UserSession
{

	private Long id=null;
	private UserCredentials credentials=null;

// Constructors
	public UserSession(Long id, UserCredentials credentials)
	{
		super();
		this.id=id;
		this.credentials=credentials;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result=(prime*result)
		       +((this.id==null) ? 0 : this.id.hashCode());
		return result;
	}

// Overrides
	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if (obj instanceof Long)
		{
			Long other=(Long) obj;
			result=this.getId().equals(other);
		}
		else if (obj instanceof UserSession)
		{
			UserSession other=(UserSession) obj;
			result=this.getId().equals(other.getId());
		}
		return result;
	}

	public Long getId()
	{
		return this.id;
	}

	public void setId(Long id)
	{
		this.id=id;
	}

// Getters and setters
	public UserCredentials getCredentials()
	{
		return this.credentials;
	}

	public void setCredentials(UserCredentials credentials)
	{
		this.credentials=credentials;
	}
}
