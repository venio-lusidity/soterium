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

package com.lusidity.domains.object.edge;

import com.lusidity.Environment;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.organization.Organization;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.io.ScopedDirectory;
import com.lusidity.system.security.UserPositions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@AtSchemaClass(name="Principal Edge", discoverable=false)
public class PrincipalEdge extends TermEdge
{
	private KeyDataCollection<Permission> permissions=null;

// Constructors
	@SuppressWarnings("unused")
	public PrincipalEdge()
	{
		super();
	}

	@SuppressWarnings("unused")
	public PrincipalEdge(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public PrincipalEdge(EdgeData edgeData, Endpoint fromEndpoint, Endpoint toEndpoint)
	{
		super(edgeData, fromEndpoint, toEndpoint);
		this.load();
	}

	private void load()
	{
		if (ClassX.isKindOf(this.getEdgeData(), PrincipalEdgeData.class))
		{
			try
			{
				PrincipalEdgeData ped=(PrincipalEdgeData) this.getEdgeData();
				for (Permission permission : ped.getPermissions())
				{
					this.getPermissions().add(permission);
				}
				this.save();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
	}

	public KeyDataCollection<Permission> getPermissions()
	{
		if (null==this.permissions)
		{
			this.permissions = new KeyDataCollection<>(this, "permissions", Permission.class, false, false, false, null);
		}
		return this.permissions;
	}

// Overrides
	@Override
	public Class<? extends EdgeData> getEdgeDataType()
	{
		return PrincipalEdgeData.class;
	}

	@SuppressWarnings("NestedConditionalExpression")
	@Override
	public void afterUpdate(LogEntry.OperationTypes operationType, boolean success)
	{
		super.afterUpdate(operationType, success);

		BasePrincipal principal = (null==this.getCredentials())? null : this.getCredentials().getPrincipal();
		if((null!=principal) && principal.isAdmin(true)){
			ApolloVertex from =(ApolloVertex) this.fetchEndpointFrom().getValue().getVertex();
			DataVertex what = this.fetchEndpointTo().getValue().getVertex();
			if(null!=what){
				what.setCredentials(this.getCredentials());
			}

			String eType = StringX.insertSpaceAtCapitol(from.getClass().getSimpleName());

			String comment = null;
			if(operationType==LogEntry.OperationTypes.create){
				comment = String.format("%s %s to %s (%s).", success ? "Added": "Attempted to add", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType);
			}
			else if(operationType==LogEntry.OperationTypes.update){
				comment = String.format("%s %s permissions within %s (%s).", success ? "Updated": "Attempted to update", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType);
			}
			else if(operationType==LogEntry.OperationTypes.delete){
				comment = String.format("%s %s from %s (%s).", success ? "Removed": "Attempted to remove", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType);
			}

			if(!StringX.isBlank(comment))
			{
				UserActivity.logActivity((ApolloVertex) what, LogEntry.OperationTypes.assignment, comment, success);
			}
		}

		if(ScopedConfiguration.getInstance().isEnabled())
		{
			DataVertex vertex=this.fetchEndpointTo().getValue().getVertex();
			//noinspection ChainOfInstanceofChecks
			if (vertex instanceof PersonnelPosition)
			{
				PersonnelPosition pp=(PersonnelPosition) vertex;
				for (BasePrincipal bp : pp.getPrincipals())
				{
					Collection<ScopedDirectory> directories=ScopedConfiguration.getInstance().getScopedDirectories();
					UserDirFilterHandler ffh=new UserDirFilterHandler("cbac", directories, bp);
					Environment.getInstance().getPrincipalFilterCache().reset(bp, ffh);
				}
			}
			else if (vertex instanceof Person)
			{
				Person person=(Person) vertex;
				UserPositions userPositions=new UserPositions(person);
				Map<PersonnelPosition, List<Organization>> positions=userPositions.getPositions();
				if (null!=positions)
				{
					for (Map.Entry<PersonnelPosition, List<Organization>> entry : positions.entrySet())
					{
						PersonnelPosition pp=entry.getKey();
						for (BasePrincipal bp : pp.getPrincipals())
						{
							Collection<ScopedDirectory> directories=ScopedConfiguration.getInstance().getScopedDirectories();
							UserDirFilterHandler ffh=new UserDirFilterHandler("cbac", directories, bp);
							Environment.getInstance().getPrincipalFilterCache().reset(bp, ffh);
						}
					}
				}
			}
		}
	}
}
