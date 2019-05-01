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

import com.lusidity.annotations.AtIndexedField;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.TermEdges;
import com.lusidity.domains.acs.security.BasePosition;
import com.lusidity.domains.location.Location;
import com.lusidity.domains.object.edge.ScopedEdge;
import com.lusidity.domains.object.edge.TermEdge;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.json.JsonData;

@AtIndexedField(key="slots", type=Integer.class)
@AtSchemaClass(name="Personnel Position", discoverable=false, writable=true)
public class PersonnelPosition extends BasePosition
{
	@AtSchemaProperty(name="Personnel Positions", edgeType=TermEdge.class, expectedType=PersonnelPosition.class,
		description="Child positions that this position is the authority of.")
	private TermEdges<PersonnelPosition> subordinates=null;
	@AtSchemaProperty(name="Personnel Positions",
		fieldName="positions",
		labelType=PersonnelPosition.class,
		edgeType=TermEdge.class,
		expectedType=Organization.class,
		direction=Common.Direction.IN,
		description="Organizations that this position belongs to.")
	private TermEdges<Organization> parentOrganizations=null;
	@AtSchemaProperty(name="Personnel Positions",
		fieldName="scopedPositions",
		labelType=PersonnelPosition.class,
		edgeType=ScopedEdge.class,
		expectedType=Organization.class,
		direction=Common.Direction.IN,
		description="Organizations that this position is scoped to.")
	private TermEdges<Organization> parentScopedOrganizations=null;
	@AtSchemaProperty(name="Personnel Positions",
		fieldName="scopedPositions",
		labelType=PersonnelPosition.class,
		edgeType=ScopedEdge.class,
		expectedType=Location.class,
		direction=Common.Direction.IN,
		description="Organizations that this position is scoped to.")
	private TermEdges<Location> parentScopedLocations=null;

	// Constructors
	public PersonnelPosition()
	{
		super();
	}

	@SuppressWarnings("unused")
	public PersonnelPosition(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Getters and setters
	public TermEdges<PersonnelPosition> getSubordinates()
	{
		if (null==this.subordinates)
		{
			this.buildProperty("subordinates");
		}
		return this.subordinates;
	}

	public TermEdges<Organization> getParentOrganizations()
	{
		if (null==this.parentOrganizations)
		{
			this.buildProperty("parentOrganizations");
		}
		return this.parentOrganizations;
	}

	public TermEdges<Organization> getParentScopedOrganizations()
	{
		if (null==this.parentScopedOrganizations)
		{
			this.buildProperty("parentScopedOrganizations");
		}
		return this.parentScopedOrganizations;
	}

	public TermEdges<Location> getParentScopedLocations()
	{
		if (null==this.parentScopedLocations)
		{
			this.buildProperty("parentScopedLocations");
		}
		return this.parentScopedLocations;
	}
}
