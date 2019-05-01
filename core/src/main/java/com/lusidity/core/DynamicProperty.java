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

package com.lusidity.core;

import com.lusidity.data.DataVertex;

public class DynamicProperty
{

	// ------------------------------ FIELDS ------------------------------

	private final boolean indexed;
	private final Class<? extends DataVertex> type;
	private String key;
	private String name;

// Constructors
	/**
	 * Constructor.
	 *
	 * @param key  Machine-readable key.
	 * @param name Human-readable name.
	 */
	public DynamicProperty(String key, String name, Class<? extends DataVertex> cls, boolean indexed)
	{
		super();

		this.key=key;
		this.name=name;
		this.type=cls;
		this.indexed=indexed;
	}

// --------------------- GETTER / SETTER METHODS ---------------------

// Overrides
	@Override
	public int hashCode()
	{
		return this.key.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		boolean result;

		if (this==o)
		{
			result=true;
		}
		else if (!(o instanceof DynamicProperty))
		{
			result=false;
		}
		else
		{
			DynamicProperty that=(DynamicProperty) o;
			result=this.key.equals(that.key);
		}
		return result;
	}

	@Override
	public String toString()
	{
		StringBuffer sb=new StringBuffer();
		sb.append("DynamicProperty");
		sb.append("{key='").append(this.key).append('\'');
		sb.append('}');
		return sb.toString();
	}

// Getters and setters
	public Class<? extends DataVertex> getType()
	{
		return this.type;
	}

	public String getKey()
	{
		return this.key;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setKey(String key)
	{
		this.key=key;
	}

// ------------------------ CANONICAL METHODS ------------------------

	public String getName()
	{
		return this.name;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setName(String name)
	{
		this.name=name;
	}

	@SuppressWarnings("UnusedDeclaration")
	public boolean isIndexed()
	{
		return this.indexed;
	}
}
