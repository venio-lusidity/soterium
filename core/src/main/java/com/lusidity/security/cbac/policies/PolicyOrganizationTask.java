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
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.organization.Organization;
import com.lusidity.domains.organization.PersonnelPosition;

import java.util.Objects;
import java.util.concurrent.Callable;

public class PolicyOrganizationTask implements Callable<Boolean>
{
	private final Organization original;
	private final PolicyOrganization.ScanDirection scan;
	private final BasePrincipal principal;

	// Constructors
	public PolicyOrganizationTask(Organization organization, BasePrincipal principal, PolicyOrganization.ScanDirection scan)
	{
		super();
		this.original = organization;
		this.scan = scan;
		this.principal = principal;
	}

	// Overrides
	@Override
	public Boolean call()
		throws Exception
	{
		return this.handle(this.original);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod"
	})
	private boolean handle(Organization organization)
	{
		boolean result=false;

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

				if (!result && (this.scan!=PolicyOrganization.ScanDirection.none))
				{
					if (this.scan==PolicyOrganization.ScanDirection.up)
					{
						for (Organization parent : organization.getParents())
						{
							result=this.handle(parent);
							if (result)
							{
								break;
							}
						}
					}
					else if (this.scan==PolicyOrganization.ScanDirection.down)
					{
						for (Organization child : organization.getOrganizations())
						{
							result=this.handle(child);
							if (result)
							{
								break;
							}
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning("Policy Organization: The below error can be preventing users from accessing data.");
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}
}
