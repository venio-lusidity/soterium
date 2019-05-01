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
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.io.ScopedDirectory;
import com.lusidity.security.authorization.IAuthorizedUpdateHandler;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

@AtSchemaClass(name = "Authorized Edge", discoverable = false, writable = true)
public class AuthorizedEdge extends Edge
{
	private KeyData<Boolean> read = null;
	private KeyData<Boolean> write = null;
	private KeyData<Boolean> delete = null;
	private KeyData<Boolean> denied= null;
	private KeyData<Boolean> customized= null;
	private KeyData<DateTime> expiresOn = null;

	// Constructors
	@SuppressWarnings("unused")
	public AuthorizedEdge()
	{
		super();
	}

	@SuppressWarnings("unused")
	public AuthorizedEdge(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public AuthorizedEdge(EdgeData edgeData, Endpoint fromEndpoint, Endpoint toEndpoint)
	{
		super(edgeData, fromEndpoint, toEndpoint);
	}

	// Overrides
	@Override
	public void afterUpdate(LogEntry.OperationTypes operationType, boolean success)
	{
		super.afterUpdate(operationType, success);
		List<IAuthorizedUpdateHandler> handlers=Environment.getInstance().getAuthorizedUpdateHandlers();

		for (IAuthorizedUpdateHandler handler : handlers)
		{
			handler.handle(operationType, success, this);
		}

		DataVertex vertex = this.fetchEndpointTo().getValue().getVertex();
		if(vertex instanceof PersonnelPosition)
		{
			PersonnelPosition pp = (PersonnelPosition)vertex;
			for(BasePrincipal bp: pp.getPrincipals())
			{
				Collection<ScopedDirectory> directories = ScopedConfiguration.getInstance().getScopedDirectories();
				UserDirFilterHandler ffh = new UserDirFilterHandler("cbac", directories, bp);
				Environment.getInstance().getPrincipalFilterCache().reset(bp, ffh);
			}

			BasePrincipal principal = (null==this.getCredentials())? null : this.getCredentials().getPrincipal();
			if((null!=principal) && principal.isAdmin(true)){
				ApolloVertex from =(ApolloVertex) this.fetchEndpointFrom().getValue().getVertex();
				DataVertex what = this.fetchEndpointTo().getValue().getVertex();
				if((null!=what) && ClassX.isKindOf(what, PersonnelPosition.class))
				{
					what.setCredentials(this.getCredentials());

					String pType="Personnel Position";
					String eType=StringX.insertSpaceAtCapitol(from.getClass().getSimpleName());
					String comment=null;
					if (operationType==LogEntry.OperationTypes.create)
					{
						comment=String.format("%s %s access to %s (%s %s).", success ? "Authorized" : "Attempted to authorize", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType, pType);
					}
					else if (operationType==LogEntry.OperationTypes.update)
					{
						comment=String
							.format("%s %s authorization within %s (%s %s).", success ? "Updated" : "Attempted to update", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType,

								pType
							);
					}
					else if (operationType==LogEntry.OperationTypes.delete)
					{
						comment=String.format("%s %s authorization from %s (%s %s).", success ? "Removed " : "Attempted to remove", ((ApolloVertex) what).fetchTitle().getValue(), from.fetchTitle().getValue(), eType, pType);
					}

					if (!StringX.isBlank(comment))
					{
						UserActivity.logActivity((ApolloVertex) what, LogEntry.OperationTypes.authorization, comment, success);
					}
				}
			}
		}
	}

	public KeyData<Boolean> fetchCustomized(){
		if(null==this.customized){
			this.customized = new KeyData<>(this, "customized", Boolean.class, false, false);
		}
		return this.customized;
	}

	public KeyData<DateTime> fetchExpiresOn(){
		if(null==this.expiresOn){
			this.expiresOn = new KeyData<>(this, "expiresOn", DateTime.class, false, false);
		}
		return this.expiresOn;
	}

	public KeyData<Boolean> fetchRead(){
		if(null==this.read){
			this.read = new KeyData<>(this, "read", Boolean.class, false, true);
		}
		return this.read;
	}

	public KeyData<Boolean> fetchWrite(){
		if(null==this.write){
			this.write = new KeyData<>(this, "write", Boolean.class, false, true);
		}
		return this.write;
	}

	public KeyData<Boolean> fetchDelete(){
		if(null==this.delete){
			this.delete = new KeyData<>(this, "delete", Boolean.class, false, true);
		}
		return this.delete;
	}

	public KeyData<Boolean> fetchDenied(){
		if(null==this.denied){
			this.denied= new KeyData<>(this, "denied", Boolean.class, false, false);
		}
		return this.denied;
	}
}
