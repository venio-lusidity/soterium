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

import com.lusidity.Environment;
import com.lusidity.Initializer;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.UserCredentials;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PrincipalFilterEngine implements Initializer
{
	private static Map<Class<? extends DataVertex>, Class<? extends BasePrincipalFilter>> cache=new HashMap<>();

	// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		Set<Class<? extends BasePrincipalFilter>> subtypes=Environment.getInstance().getReflections().getSubTypesOf(BasePrincipalFilter.class);
		for (Class<? extends BasePrincipalFilter> subtype : subtypes)
		{
			Constructor constructor=subtype.getConstructor();
			BasePrincipalFilter bpf=(BasePrincipalFilter) constructor.newInstance();
			Collection<Class<? extends DataVertex>> classes=bpf.forClasses();
			if (null==classes)
			{
				continue;
			}
			for (Class<? extends DataVertex> cls : classes)
			{
				PrincipalFilterEngine.cache.put(cls, subtype);
			}
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return 0;
	}

	// Methods
	public static PrincipalFilterEngine getInstance()
	{
		return new PrincipalFilterEngine();
	}

	/**
	 * If the given DataVertex class has a BasePrincipalFilter apply the filters to the given BaseQueryBuilder.
	 *
	 * @param cls             The DataVertex class that a principal filter may apply to.
	 * @param userCredentials The UserCredentials of the user requesting data.
	 * @param qb              The BaseQueryBuilder to apply the filters to.
	 * @param isNative        Is the BaseQueryBuilder using a native and/or direct query statement.
	 */
	public void applyFor(Class<? extends DataVertex> cls, UserCredentials userCredentials, BaseQueryBuilder qb, boolean isNative)
	{
		BasePrincipalFilter bpf=this.getFilterFor(cls, userCredentials);
		if (null!=bpf)
		{
			boolean includeTagIssues=this.hasTagIssues(qb);
			bpf.apply(cls, qb, "includeTagIssues", includeTagIssues);
			if (isNative)
			{
				qb.applyMust(qb.getRawQuery());
				qb.applyMustNot(qb.getRawQuery());
				qb.applyShould(qb.getRawQuery());
			}
		}
	}

	/**
	 * @param cls             The DataVertex class that a principal filter may apply to.
	 * @param userCredentials The UserCredentials of the user requesting data.
	 * @param <T>             The BasePrincipal filter type expected.
	 * @return A BasePrincipalFilter.
	 */
	public <T extends BasePrincipalFilter> T getFilterFor(Class<? extends DataVertex> cls, UserCredentials userCredentials)
	{
		T result=null;
		Class<? extends BasePrincipalFilter> subtype=PrincipalFilterEngine.cache.get(cls);
		if (null!=subtype)
		{
			try
			{
				Constructor constructor=subtype.getConstructor(Class.class, UserCredentials.class);
				BasePrincipalFilter bpf=(BasePrincipalFilter) constructor.newInstance(cls, userCredentials);
				//noinspection unchecked
				result=(T) bpf;
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		return result;
	}

	private boolean hasTagIssues(BaseQueryBuilder qb)
	{
		// Hard coded for RMK specifically.
		boolean result=true;

		JsonData data=(JsonData) qb.getQuery();
		JsonData must=data.getFromPath("query", "filtered", "filter", "bool", "must");
		if (null!=must)
		{
			String str=must.toString();
			result=StringX.contains(str, "prefixDitpr", "prefixLocation", "prefixManaged", "prefixOwned");
		}

		return result;
	}
}
