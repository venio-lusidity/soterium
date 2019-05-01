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

package com.lusidity.collections;


import com.lusidity.Environment;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.edge.PrincipalEdge;

import java.util.Collection;

@SuppressWarnings("ThrowCaughtLocally")
public class PrincipalEdges<T extends BasePrincipal> extends ElementEdges<T>
{
// Constructors
	public PrincipalEdges(PropertyAttributes propertyAttributes)
	{
		super(propertyAttributes);
	}

	@SuppressWarnings("unused")
	public boolean removePermission(BasePrincipal principal, Permission permission)
	{
		boolean result=false;
		try
		{
			if (this.contains(principal))
			{
				PrincipalEdge edge=this.getVertex().getEdgeHelper().getEdge(this.getEdgeType(), principal, this
					.getKey(), this.getDirection());
				if (null!=edge)
				{
					if (edge.getPermissions().contains(permission))
					{
						result=edge.getPermissions().remove(permission);
						if (result)
						{
							edge.save();
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	@Override
	public Class<? extends Edge> getEdgeType()
	{
		return PrincipalEdge.class;
	}

	@SuppressWarnings("unused")
	public boolean hasPermissions(BasePrincipal principal, Collection<Permission> permissions)
	{
		boolean result=false;
		if (null!=permissions)
		{
			for (Permission permission : permissions)
			{
				result=this.hasPermission(principal, permission);
				if (!result)
				{
					break;
				}
			}
		}
		return result;
	}

	public boolean hasPermission(BasePrincipal principal, Permission permission)
	{
		boolean result=false;
		if ((null!=permission) && (null!=principal) && this.contains(principal))
		{
			PrincipalEdge edge=this.getVertex().getEdgeHelper().getEdge(this.getEdgeType(), principal, this
				.getKey(), this.getDirection());
			if (null!=edge)
			{
				for (Permission p : edge.getPermissions())
				{
					if (p.equals(permission))
					{
						result=true;
						break;
					}
				}
			}
		}
		return result;
	}
}