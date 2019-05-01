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

package com.lusidity.domains.acs.security.loging;

import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.SystemUser;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.ClientInfo;
import com.lusidity.system.security.UserCredentials;
import org.joda.time.DateTime;
import org.restlet.data.Reference;

import java.util.Objects;

@AtSchemaClass(name="User Activity", discoverable=false,
	description="Maintains a record of a users activity within Soterium by keeping a " +
	            "record of all any of the following create, update, delete and gets.")
public class UserActivity extends LogEntry
{
	private KeyData<String> server=null;
	private KeyDataCollection<ClientInfo> clientInfo=null;

// Constructors
	public UserActivity()
	{
		super();
	}

	@SuppressWarnings("unused")
	public UserActivity(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Methods
	@SuppressWarnings({
		"BooleanParameter",
		"OverlyComplexMethod"
	})
	public static void logActivity(ApolloVertex what, LogEntry.OperationTypes operationType, String comment, boolean success)
	{
		if (!ClassX.isKindOf(what.getClass(), LogEntry.class) && (null!=what.getCredentials()))
		{
			try
			{
				boolean login =((null!=what.getCredentials()) && (what.getCredentials().getActivity()==LogEntry.OperationTypes.login));
				BasePrincipal who=(null!=what.getCredentials()) ? what.getCredentials().getPrincipal() : null;
				LogEntry.OperationTypes ot = operationType;
				if ((null!=who) && BasePrincipal.isUser(who))
				{
					boolean whoId = true;
					if(who.equals(what)){
						if(!Environment.getInstance().isOpened() || login){
							who =SystemUser.getInstance();
							ot = LogEntry.OperationTypes.verified;
							whoId = false;
						}
					}
					Identity identity= (null==what.getCredentials()) ? null : what.getCredentials().getIdentity();
					UserActivity result=new UserActivity();
					if(whoId && (null!=who.getUri()))
					{
						result.fetchWhoId().setValue(String.format("%s::%s", who.getUri().toString(),  (null==identity) ? "unknown" : identity.getUri().toString()));
					}
					result.fetchTitle().setValue(who.fetchTitle().getValue());
					result.fetchOperationType().setValue(ot);
					result.fetchWhatId().setValue(what.getUri().toString());
					result.fetchWhat().setValue(what.fetchTitle().getValue());
					result.fetchServer().setValue(Environment.getInstance().getConfig().getServerName());
					result.fetchWhatType().setValue(what.fetchVertexType().getValue());

					if ((null!=what.getCredentials()) && (null!=what.getCredentials().getOrigin()))
					{
						result.fetchOrigin().setValue((null!=what.getCredentials()) ? what.getCredentials().getOrigin() : null);
					}

					if ((null!=what.getCredentials()) && (null!=what.getCredentials().getReferrer()))
					{
						result.fetchReferrer().setValue((null!=what.getCredentials()) ? what.getCredentials().getReferrer() : null);
					}

					result.setSuccess(success);
					if ((null!=what.getCredentials()) && (null!=what.getCredentials().getRequest()))
					{
						ClientInfo clientInfo=ClientInfo.create(what.getCredentials().getRequest());
						result.fetchClientInfo().add(clientInfo);
					}
					String msg=comment;
					if (Objects.equals(operationType, LogEntry.OperationTypes.login) && StringX.isBlank(comment))
					{
						msg=String.format("%s logged in at %s", who.fetchTitle().getValue(), (null==identity) ? DateTime.now().toString("dd MMM YYYY HH:mm:ss") : identity.getLastLoggedIn().toString("dd MMM YYYY HH:mm:ss"));
					}
					if (StringX.isBlank(msg))
					{
						msg=String.format("User %s %s a vertex.", result.fetchWhoId(), result.fetchOperationType());
					}
					result.fetchComment().setValue(msg);

					who.logActivity(result);
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
	}

	public KeyData<String> fetchServer()
	{
		if (null==this.server)
		{
			this.server=new KeyData<>(this, "server", String.class, false, null);
		}
		return this.server;
	}

	public KeyDataCollection<ClientInfo> fetchClientInfo()
	{
		if ((null==this.clientInfo))
		{
			this.clientInfo = new KeyDataCollection<>(this, "clientInfo", ClientInfo.class, false, false, false, null);
		}
		return this.clientInfo;
	}

	public static void logActivity(String comment, UserCredentials who, LogEntry.OperationTypes operationType, Reference reference, boolean success)
	{
		try
		{
			if (null!=who)
			{
				BasePrincipal principal=who.getPrincipal();
				if ((null!=principal) && BasePrincipal.isUser(principal))
				{
					Identity identity=who.getIdentity();
					UserActivity result=new UserActivity();
					result.fetchWhoId().setValue(String.format("%s::%s", principal.getUri().toString(), (null==identity) ? "unknown" : identity.getUri().toString()));
					result.fetchOperationType().setValue(operationType);
					result.fetchWhatId().setValue(reference.toString());
					result.fetchTitle().setValue(principal.fetchTitle().getValue());
					result.fetchServer().setValue(Environment.getInstance().getConfig().getServerName());
					result.fetchWhatType().setValue(reference.getScheme());
					result.fetchOrigin().setValue(reference.getHostDomain());
					result.fetchReferrer().setValue(who.getReferrer());
					result.setSuccess(success);
					if (null!=who.getRequest())
					{
						ClientInfo clientInfo=ClientInfo.create(who.getRequest());
						result.fetchClientInfo().add(clientInfo);
					}
					String cmt=String.format("Method: %s, Protocol: %s, Domain: %s, Port: %d, Referrer: %s\n\r%s",
						result.fetchOperationType().toString(),
						reference.getScheme(),
						reference.getHostDomain(),
						reference.getHostPort(),
						who.getReferrer(),
						comment
					);
					result.fetchComment().setValue(cmt);
					principal.logActivity(result);
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	public static void logActivity(UserCredentials who, LogEntry.OperationTypes operationType, Reference reference, boolean success)
	{
		try
		{
			if (null!=who)
			{
				BasePrincipal principal=who.getPrincipal();
				if ((null!=principal) && BasePrincipal.isUser(principal))
				{
					Identity identity=who.getIdentity();
					UserActivity result=new UserActivity();
					result.fetchWhoId().setValue(String.format("%s::%s", principal.getUri().toString(), (null==identity) ? "unknown" : identity.getUri().toString()));
					result.fetchOperationType().setValue(operationType);
					result.fetchWhatId().setValue(reference.toString());
					result.fetchTitle().setValue(principal.fetchTitle().getValue());
					result.fetchServer().setValue(Environment.getInstance().getConfig().getServerName());
					result.fetchWhatType().setValue(reference.getScheme());
					result.fetchOrigin().setValue(reference.getHostDomain());
					result.fetchReferrer().setValue(who.getReferrer());
					result.setSuccess(success);
					if (null!=who.getRequest())
					{
						ClientInfo clientInfo=ClientInfo.create(who.getRequest());
						result.fetchClientInfo().add(clientInfo);
					}
					String comment=String.format("Method: %s, Protocol: %s, Domain: %s, Port: %d, Referrer: %s",
						result.fetchOperationType().toString(),
						reference.getScheme(),
						reference.getHostDomain(),
						reference.getHostPort(),
						who.getReferrer()
					);
					result.fetchComment().setValue(comment);
					principal.logActivity(result);
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}
}
