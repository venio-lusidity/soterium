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

package com.lusidity.domains.organization;

import com.lusidity.Environment;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;

import java.security.InvalidParameterException;

public class OrganizationHelper
{
// Methods
	public static void register(BasePrincipal principal, JsonData data)
		throws Exception
	{
		if (!data.hasValue("organization"))
		{
			throw new InvalidParameterException("Null values are not allowed for the key \"organization\".");
		}
		if (!data.hasValue("organizationPosition"))
		{
			throw new InvalidParameterException("Null values are not allowed for the key \"organizationPosition\".");
		}
		String id=data.getString("organization");
		Organization organization=VertexFactory.getInstance().get(id);
		if (null==organization)
		{
			throw new ApplicationException("The id, %s, did not return an organization.", id);
		}

		String pTitle=data.getString("organizationPosition");
		pTitle=StringX.toTitle(pTitle);

		BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(PersonnelPosition.class, PersonnelPosition.class, 0, 10000);
		builder.filter(BaseQueryBuilder.Operators.must, "title", BaseQueryBuilder.StringTypes.starts_with, pTitle);
		builder.setApi(BaseQueryBuilder.API._count);

		int total=builder.execute().getCount();
		total+=1;

		PersonnelPosition position=null;
		for (BasePrincipal basePrincipal : principal.getParentPrincipals())
		{
			if (ClassX.isKindOf(basePrincipal, PersonnelPosition.class))
			{
				position=(PersonnelPosition) basePrincipal;
				break;
			}
		}

		boolean isNew=(null==position);

		if (isNew)
		{
			position=new PersonnelPosition();
		}
		position.fetchTitle().setValue(String.format("%s %d", pTitle, total));
		position.save();
		position.getPrincipals().add(principal);
		if (!isNew && !organization.getPositions().contains(position))
		{
			int size=position.getParentOrganizations().size();
			if (size==1)
			{
				Organization org=position.getParentOrganizations().get();
				org.getPositions().remove(position);
			}
		}

		organization.getPositions().add(position);

		JsonData items=data.getFromPath("/organization/personnel_position/scopedPositions");

		if ((null!=items) && items.isJSONArray())
		{
			for (Object o : items)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=new JsonData(o);
					String rTitle=item.getString("role");
					if (!StringX.isBlank(rTitle))
					{
						Group group=Group.getGroup(rTitle);
						if (null==group)
						{
							group=new Group();
							group.fetchTitle().setValue(rTitle);
							group.save();
						}
						if (group.hasId())
						{
							group.getPrincipals().add(principal);
							Group.getRootGroup().getPrincipals().add(group);
						}
					}
					String orgId = item.getString("scope");
					Organization org = VertexFactory.getInstance().get(orgId);
					if(null!=org)
					{
						org.setCredentials(SystemCredentials.getInstance());
						org.getScopedPositions().add(position);
					}
				}
			}
		}

		PersonnelPositionHelper.addSupervisor(position, organization, data);
	}
}
