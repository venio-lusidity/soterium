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

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.object.edge.ScopedEdge;
import com.lusidity.domains.organization.Organization;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.domains.people.Person;

import java.util.*;

public class UserPositions
{
	private final Person person;
	private LinkedHashMap<PersonnelPosition, List<Organization>> positions = new LinkedHashMap<>();
	private LinkedHashMap<PersonnelPosition, List<Organization>> scopedPositions = new LinkedHashMap<>();

	@SuppressWarnings("unused")
	public UserPositions(Person person){
		super();
		this.person = person;
		this.load();
	}

	public Person getPerson(){
		return this.person;
	}

	public LinkedHashMap<PersonnelPosition, List<Organization>> getPositions(){
		return this.positions;
	}

	public Map<PersonnelPosition, List<Organization>> getScopedPositions()
	{
		return this.scopedPositions;
	}

	private void load()
	{
		List<PersonnelPosition> positions = new ArrayList<>();
		for(BasePrincipal principal: this.person.getParentPrincipals())
		{
			if (principal instanceof PersonnelPosition)
			{
				positions.add((PersonnelPosition)principal);
			}
		}
		positions.sort(new Comparator<PersonnelPosition>()
		{
			@Override
			public int compare(PersonnelPosition o1, PersonnelPosition o2)
			{
				return o1.fetchTitle().getValue().compareToIgnoreCase(o2.fetchTitle().getValue());
			}
		});

		for(PersonnelPosition position: positions){
			if(!this.positions.containsKey(position)){
				for(Organization organization: position.getParentOrganizations()){
					List<Organization> organizations = this.positions.get(position);
					if(null==organizations){
						organizations = new ArrayList<>();
						this.positions.put(position, organizations);
					}
					organizations.add(organization);
					organizations.sort(new Comparator<Organization>()
					{
						@Override
						public int compare(Organization o1, Organization o2)
						{
							return o1.fetchTitle().getValue().compareToIgnoreCase(o2.fetchTitle().getValue());
						}
					});
				}
			}
			if(!this.scopedPositions.containsKey(position))
			{
				this.scoped(position);
			}
		}
	}

	private void scoped(PersonnelPosition position)
	{
		String id = position.fetchId().getValue();
		String key = ClassHelper.getPropertyKey(PersonnelPosition.class, "scopedPositions");
		BaseQueryBuilder qb = Environment.getInstance().getIndexStore().getQueryBuilder(ScopedEdge.class, Organization.class, 0, 1000);
		qb.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, key);
		qb.filter(BaseQueryBuilder.Operators.must, Endpoint.KEY_TO_EP_ID, BaseQueryBuilder.StringTypes.raw, id);
		QueryResults qrs = qb.execute();
		if(!qrs.isEmpty())
		{
			for (IQueryResult qr : qrs)
			{
				Organization organization=qr.getOtherEnd(id);
				if(null!=organization){
					List<Organization> organizations = this.scopedPositions.get(position);
					if(null==organizations){
						organizations = new ArrayList<>();
						this.scopedPositions.put(position, organizations);
					}
					organizations.add(organization);
					organizations.sort(new Comparator<Organization>()
					{
						@Override
						public int compare(Organization o1, Organization o2)
						{
							return o1.fetchTitle().getValue().compareToIgnoreCase(o2.fetchTitle().getValue());
						}
					});
				}
			}
		}
	}

	public String toHtml(){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<PersonnelPosition, List<Organization>> entry: this.getPositions().entrySet()){
			PersonnelPosition position = entry.getKey();
			List<Organization> organizations = entry.getValue();
			sb.append("<br/>");
			sb.append("<span><strong>Positions:</strong>&nbsp;").append(position.fetchTitle().getValue()).append("</span>");
			for(Organization organization: organizations){
				sb.append("<br/><span style=\"margin-left: 20px;\"><strong>Organization:</strong>&nbsp;</span>");
				sb.append("<a href=\"").append(organization.getUri()).append("\" target=\"_blank\">").append(organization.fetchTitle().getValue()).append("</a>");
			}
		}

		for(Map.Entry<PersonnelPosition, List<Organization>> entry: this.getScopedPositions().entrySet()){
			PersonnelPosition position = entry.getKey();
			List<Organization> organizations = entry.getValue();
			sb.append("<br/>");
			sb.append("<span class=\"pos-scoped-position\" data-scope-position-lid=\"")
			  .append(position.fetchId())
			  .append("\"><strong>Scoped Position:</strong>&nbsp;")
			  .append(position.fetchTitle().getValue())
			  .append("</span>");
			sb.append("<br/><span style=\"margin-left: 20px;\"><strong>Organizations:</strong>&nbsp;</span>");
			sb.append("<br/>");
			for(Organization organization: organizations){
				sb.append("<span class=\"org-scoped-positions\" style=\"margin-left: 20px;\" data-organization-lid=\"")
				  .append(organization.fetchId())
				  .append("\" ><a href=\"")
				  .append(organization.getUri())
				  .append("\" target=\"_blank\">")
				  .append(organization.fetchTitle().getValue())
				  .append("</a></span>");
				sb.append("<br/>");
			}
		}
		return (sb.length()>0) ? sb.toString() : null;
	}
}
