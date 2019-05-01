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


import com.lusidity.Environment;
import com.lusidity.Initializer;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;

import java.util.*;

public class AuthorizationRule implements Initializer
{
	public enum RuleTypes
	{
		read,
		write,
		delete,
		comment,
		deny,
		admin
	}
	public enum Category
	{
		group,
		permissions
	}
// Fields
	public static final int INIT_ORDINAL=1111;
	@SuppressWarnings("MapReplaceableByEnumMap")
	private static Map<AuthorizationRule.RuleTypes, Collection<AuthorizationRule>> AUTH_RULES=new HashMap<>();
	private final AuthorizationRule.Category type;
	private final Double id;

// Constructors
	public AuthorizationRule(AuthorizationRule.Category type, double id)
	{
		super();
		this.type=type;
		this.id=id;
	}

	@SuppressWarnings("unused")
	public AuthorizationRule()
	{
		super();
		this.type=null;
		this.id=null;
	}

// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		JsonData items=Environment.getInstance().getConfig().getRules();
		if ((null!=items) && items.isJSONObject())
		{
			for (String key : items.keys())
			{
				AuthorizationRule.RuleTypes ruleType=AuthorizationRule.RuleTypes.valueOf(key);
				JsonData item=items.getFromPath(key);
				if (null!=item)
				{
					Collection<AuthorizationRule> rules=new ArrayList<>();

					JsonData roles=item.getFromPath(AuthorizationRule.Category.group.toString());
					AuthorizationRule.addAllRules(roles, AuthorizationRule.Category.group, rules);
					JsonData permissions=item.getFromPath(AuthorizationRule.Category.permissions.toString());
					AuthorizationRule.addAllRules(permissions, AuthorizationRule.Category.permissions, rules);
					if (!rules.isEmpty())
					{
						AuthorizationRule.AUTH_RULES.put(ruleType, rules);
					}
				}
			}
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return AuthorizationRule.INIT_ORDINAL;
	}

	private static void addAllRules(JsonData items, AuthorizationRule.Category type, Collection<AuthorizationRule> rules)
	{
		if ((null!=items) && items.isJSONArray())
		{
			for (Object o : items)
			{
				if (o instanceof Double)
				{
					Double id=(Double) o;
					AuthorizationRule rule=AuthorizationRule.create(type, id);
					rules.add(rule);
				}
			}
		}
	}

	public static AuthorizationRule create(AuthorizationRule.Category type, double id)
	{
		return new AuthorizationRule(type, id);
	}

// Methods
	@SuppressWarnings("OverlyNestedMethod")
	public static boolean validate(BasePrincipal principal, AuthorizationRule.RuleTypes key)
	{
		boolean result=false;

		if (AuthorizationRule.AUTH_RULES.containsKey(key))
		{
			Collection<AuthorizationRule> rules=AuthorizationRule.AUTH_RULES.get(key);
			for (BasePrincipal o : principal.getParentPrincipals())
			{
				if (ClassX.isKindOf(o, Group.class))
				{
					Group group=(Group) o;
					result=AuthorizationRule.contains(AuthorizationRule.Category.group, rules, group.fetchIdentifier().getValue());
					if (!result)
					{
						for (Permission permission : group.getPermissions())
						{
							result=
								AuthorizationRule.contains(AuthorizationRule.Category.permissions, rules, permission.fetchIdentifier().getValue());

							if (result)
							{
								break;
							}
						}
					}
					if (result)
					{
						break;
					}
				}
			}
		}

		return result;
	}

	private static boolean contains(AuthorizationRule.Category category, Collection<AuthorizationRule> rules, Double identifier)
	{
		boolean result=false;
		for (AuthorizationRule ar : rules)
		{
			result=((ar.getCategory()==category) && Objects.equals(ar.getId(), identifier));
			if (result)
			{
				break;
			}
		}
		return result;
	}

	public AuthorizationRule.Category getCategory()
	{
		return this.type;
	}

	public Double getId()
	{
		return this.id;
	}

// Getters and setters
	@SuppressWarnings("unused")
	private static Map<AuthorizationRule.RuleTypes, Collection<AuthorizationRule>> getAuthRules()
	{
		return AuthorizationRule.AUTH_RULES;
	}
}
