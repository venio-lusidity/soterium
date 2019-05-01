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
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.security.authorization.IAuthorizedUpdateHandler;

import java.util.List;

@AtSchemaClass(name="Scoped Edge", discoverable=false)
public class ScopedEdge extends TermEdge
{

// Constructors
	public ScopedEdge()
	{
		super();
	}

	public ScopedEdge(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public ScopedEdge(EdgeData edgeData, Endpoint fromEndpoint, Endpoint toEndpoint)
	{
		super(edgeData, fromEndpoint, toEndpoint);
	}

	// Overrides
	@Override
	public void afterUpdate(LogEntry.OperationTypes operationType, boolean success)
	{
		super.afterUpdate(operationType, success);
		List<IAuthorizedUpdateHandler> handlers = Environment.getInstance().getAuthorizedUpdateHandlers();

		for(IAuthorizedUpdateHandler handler: handlers){
			handler.handle(operationType, success, this);
		}

		BasePrincipal principal = (null==this.getCredentials())? null : this.getCredentials().getPrincipal();
		if((null!=principal) && principal.isAdmin(true)){
			ApolloVertex from =(ApolloVertex) this.fetchEndpointFrom().getValue().getVertex();
			DataVertex what = this.fetchEndpointTo().getValue().getVertex();
			if((null!=what) && ClassX.isKindOf(what, PersonnelPosition.class))
			{
				what.setCredentials(this.getCredentials());


				String pType="Scoped Position";
				String eType=StringX.insertSpaceAtCapitol(from.getClass().getSimpleName());
				String comment=null;
				if (operationType==LogEntry.OperationTypes.create)
				{
					comment=String.format("%s %s to %s (%s %s).", success ? "Added" : "Attempted to add", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType, pType);
				}
				else if (operationType==LogEntry.OperationTypes.update)
				{
					comment=String
						.format("%s %s permissions within %s (%s %s).", success ? "Updated" : "Attempted to update", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType,

							pType
						);
				}
				else if (operationType==LogEntry.OperationTypes.delete)
				{
					comment=
						String.format("%s %s from %s (%s %s).", success ? "Removed" : "Attempted to remove", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType, pType);
				}

				if (!StringX.isBlank(comment))
				{
					PersonnelPosition pp = (PersonnelPosition)what;
					StringBuilder sb = new StringBuilder();
					for(Person person: pp.getPeople()){
						if(sb.length()>0){
							sb.append(",");
						}
						sb.append(person.fetchTitle().getValue());
					}
					if(sb.length()>0)
					{
						comment+="[::]"+sb.toString();
					}

					UserActivity.logActivity(pp, LogEntry.OperationTypes.scoping, comment, success);
				}
			}
		}
	}
}
