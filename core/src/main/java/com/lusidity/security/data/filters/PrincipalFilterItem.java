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

package com.lusidity.security.data.filters;

import com.lusidity.domains.acs.security.BasePrincipal;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Objects;

public class PrincipalFilterItem
{
	private final DateTime cached;
	private final Collection<? extends BasePrincipalFilter> filters;
	private final Class<? extends BasePrincipalFilter> cls;
	private final BasePrincipal basePrincipal;
	private final String key;

	public PrincipalFilterItem(String key, BasePrincipal basePrincipal, Class<? extends BasePrincipalFilter> cls, Collection<? extends BasePrincipalFilter> filters){
		super();
		this.cls = cls;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.filters = filters;
		this.cached = DateTime.now();
		this.basePrincipal= basePrincipal;
		this.key = key;
	}

	public String getKey()
	{
		return this.key;
	}

	public DateTime getCached()
	{
		return this.cached;
	}

	public Collection<? extends BasePrincipalFilter> getFilters()
	{
		return this.filters;
	}

	public Class<? extends BasePrincipalFilter> getCls()
	{
		return this.cls;
	}

	public boolean isExpired(int hoursTTL){
		return this.cached.plusHours(hoursTTL).isBefore(DateTime.now());
	}

	@SuppressWarnings("RedundantMethodOverride")
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj instanceof PrincipalFilterItem){
			result = Objects.equals(this.getCls(), ((PrincipalFilterItem)obj).getCls());
		}
		return result;
	}

	public BasePrincipal getBasePrincipal()
	{
		return this.basePrincipal;
	}
}
