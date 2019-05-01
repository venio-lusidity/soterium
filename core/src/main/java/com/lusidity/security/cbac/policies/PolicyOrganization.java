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

package com.lusidity.security.cbac.policies;

import com.lusidity.Environment;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.object.edge.ScopedEdge;
import com.lusidity.domains.organization.Organization;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.cbac.ISecurityPolicy;

import java.util.Objects;

public class PolicyOrganization implements ISecurityPolicy
{
	public enum ScanDirection
	{
		down,
		up,
		none
	}

	private final BasePrincipal principal;
	private final Organization context;
	private PolicyOrganization.ScanDirection scan=PolicyOrganization.ScanDirection.none;

	// Constructors
	@SuppressWarnings("unused")
	public PolicyOrganization(BasePrincipal principal, Organization context)
	{
		super();
		this.principal=principal;
		this.context=context;
	}

	public PolicyOrganization(BasePrincipal principal, Organization context, PolicyOrganization.ScanDirection scan)
	{
		super();
		this.principal=principal;
		this.context=context;
		this.scan=scan;
	}

	// Overrides
	@Override
	public boolean canDelete()
	{
		// Use ContextBasedAccessControl to manage this.
		return this.isInScope();
	}

	@Override
	public boolean canRead()
	{
		// Use ContextBasedAccessControl to manage this.
		return this.isInScope();
	}

	@Override
	public boolean canWrite()
	{
		// Use ContextBasedAccessControl to manage this.
		return this.isInScope();
	}

	@Override
	public boolean shouldMultiThread()
	{
		return true;
	}

	@Override
	public boolean isAuthorized()
	{
		return this.isInScope();
	}

	@Override
	public boolean isDenied()
	{
		// Use ContextBasedAccessControl to manage this.
		return !this.isInScope();
	}

	@Override
	public boolean isInScope()
	{
		return this.handle(this.context);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod"
	})
	private boolean handle(Organization organization)
	{
		boolean result=this.isPowerUser();

		if (!result)
		{

			try
			{
				result=((null!=organization) && (Objects.equals(organization, Organization.getManagedTagIssues()) || Objects.equals(organization, Organization.getOwnedTagIssues())));
				if (!result && (null!=organization))
				{
					for (PersonnelPosition position : organization.getScopedPositions())
					{
						result=Group.isInGroup(this.principal, position);
						if (result)
						{
							break;
						}
					}

					if (!result && (this.principal instanceof Person))
					{
						Person person=((Person) this.principal);
						result=this.isPartOf(organization, person);
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning("Policy Organization: The below error can be preventing users from accessing data.");
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		return result;
	}

	private boolean isPowerUser()
	{
		return (ScopedConfiguration.getInstance().isEnabled()) && ScopedConfiguration.getInstance().isPowerUser(this.principal);
	}

	@SuppressWarnings("OverlyComplexMethod")
	private boolean isPartOf(Organization expected, Person person)
	{
		boolean result=false;
		for (BasePrincipal bp : person.getParentPrincipals())
		{
			if (bp instanceof PersonnelPosition)
			{
				PersonnelPosition position=(PersonnelPosition) bp;
				String id=position.fetchId().getValue();
				String key=ClassHelper.getPropertyKey(PersonnelPosition.class, "scopedPositions");
				BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(ScopedEdge.class, Organization.class, 0, 1000);
				qb.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, key);
				qb.filter(BaseQueryBuilder.Operators.must, Endpoint.KEY_TO_EP_ID, BaseQueryBuilder.StringTypes.raw, id);
				QueryResults qrs=qb.execute();
				if (!qrs.isEmpty())
				{
					for (IQueryResult qr : qrs)
					{
						Organization actual=qr.getOtherEnd(id);
						if ((this.scan==PolicyOrganization.ScanDirection.none) || (this.scan==PolicyOrganization.ScanDirection.down))
						{
							result=StringX.contains(actual.fetchPrefixTree().getValue(), expected.fetchPrefixTree().getValue());
						}
						if (!result && ((this.scan==PolicyOrganization.ScanDirection.none) || (this.scan==PolicyOrganization.ScanDirection.up)))
						{
							result=StringX.contains(expected.fetchPrefixTree().getValue(), actual.fetchPrefixTree().getValue());
						}
						if (result)
						{
							break;
						}
					}
				}
			}
			if (result)
			{
				break;
			}
		}

		return result;
	}
}
