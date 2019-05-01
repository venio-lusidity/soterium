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

package com.lusidity.domains.book.record.log;

import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import org.restlet.data.Method;

import java.util.Objects;


/* WARNING!!! do not use any of the collection classes for this class, you could create an endless recursive loop that creates log entries.*/
@SuppressWarnings("unused")
@AtSchemaClass(name="Log Entry", discoverable=false)
public class LogEntry extends BaseDomain
{
	public enum OperationTypes
	{
		/*
			A comment.
		 */
		comment,
		/*
			User logged in.
		 */
		login,
		/*
			User read data.
		 */
		read,
		/*
			User create data.
		 */
		create,
		/*
			User updated data.
		 */
		update,
		/*
			User delete request.
		 */
		delete,
		/*
			User get request.
		 */
		get,
		/*
			User post request.
		 */
		post,
		/*
			User put request.
		 */
		put,
		/*
			Used for accounts approved
		 */
		approved,
		/*
			Used for accounts disapproved
		 */
		disapproved,
		/*
			Used for accounts that have been inactivated
		 */
		inactivated,
		/*
			Used when a user has registered for a new account.
		 */
		registered,
		/*
			Events that deal with accounts when adding, removing and or updating a user to a group or a personnel position.
		 */
		assignment,
		/*
			Events that deal with adding, removing or updating a personnel position areas of responsibility.
		 */
		scoping,
		/*
			Events that deal with adding, removing or updating a named personnel position within an organization as the position hired for.
		 */
		placement,
		/*
			Events that deal with adding, removing or updating an authorization to a vertex.
		 */
		authorization,
		/**
		 * Used when the system verifies a user account.
		 */
		verified,
		none;

// Methods
		@SuppressWarnings("IfStatementWithIdenticalBranches")
		public static LogEntry.OperationTypes valueOf(Method method)
		{
			LogEntry.OperationTypes result=null;
			if (Objects.equals(method, Method.GET))
			{
				result=LogEntry.OperationTypes.get;
			}
			else if (Objects.equals(method, Method.PUT))
			{
				result=LogEntry.OperationTypes.put;
			}
			else if (Objects.equals(method, Method.POST))
			{
				result=LogEntry.OperationTypes.post;
			}
			else if (Objects.equals(method, Method.DELETE))
			{
				result=LogEntry.OperationTypes.delete;
			}
			return result;
		}
	}

	private KeyData<String> whoId=null;
	private KeyData<String> whatId=null;
	private KeyData<String> what=null;
	private KeyData<String> whatType=null;
	private KeyData<String> origin=null;
	private KeyData<String> referrer=null;
	private KeyData<String> comment=null;
	private KeyData<LogEntry.OperationTypes> operationType=null;

	private boolean success=false;

// Constructors
	public LogEntry()
	{
		super();
	}

	public LogEntry(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Methods
	public static LogEntry getEntry(ApolloVertex what, LogEntry.OperationTypes operationType, String comment, boolean success)
	{
		LogEntry result=null;
		Class cls=what.getClass();
		if (ClassX.isKindOf(cls, Edge.class))
		{
			Edge edge=(Edge) what;
			Endpoint endpoint=edge.fetchEndpointTo().getValue();
			cls=endpoint.getRelatedClass();
		}
		if (!cls.equals(LogEntry.class))
		{
			try
			{
				result=new LogEntry();
				result.fetchWhoId().setValue((null!=what.getCredentials()) ? what.getCredentials().getIdentity().fetchId().getValue() : null);
				result.fetchOperationType().setValue(operationType);
				result.fetchWhatId().setValue(what.fetchId().getValue());
				result.fetchWhat().setValue(what.fetchTitle().getValue());
				result.fetchTitle().setValue(what.fetchTitle().getValue());
				result.fetchWhatType().setValue(what.fetchVertexType().getValue());
				result.fetchOrigin().setValue((null!=what.getCredentials()) ? what.getCredentials().getOrigin() : null);
				result.fetchReferrer().setValue((null!=what.getCredentials()) ? what.getCredentials().getReferrer() : null);
				result.setSuccess(success);

			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	public KeyData<String> fetchWhoId()
	{
		if (null==this.whoId)
		{
			this.whoId=new KeyData<>(this, "whoId", String.class, false, null);
		}
		return this.whoId;
	}

	public KeyData<LogEntry.OperationTypes> fetchOperationType()
	{
		if (null==this.operationType)
		{
			this.operationType=new KeyData<>(this, "operationType", LogEntry.OperationTypes.class, false, null);
		}
		return this.operationType;
	}

	public KeyData<String> fetchWhatId()
	{
		if (null==this.whatId)
		{
			this.whatId=new KeyData<>(this, "whatId", String.class, false, null);
		}
		return this.whatId;
	}

	public KeyData<String> fetchWhat()
	{
		if (null==this.what)
		{
			this.what=new KeyData<>(this, "what", String.class, false, null);
		}
		return this.what;
	}

	public KeyData<String> fetchWhatType()
	{
		if (null==this.whatType)
		{
			this.whatType=new KeyData<>(this, "whatType", String.class, false, null);
		}
		return this.whatType;
	}

	public KeyData<String> fetchOrigin()
	{
		if (null==this.origin)
		{
			this.origin=new KeyData<>(this, "origin", String.class, false, null);
		}
		return this.origin;
	}

	public KeyData<String> fetchReferrer()
	{
		if (null==this.referrer)
		{
			this.referrer=new KeyData<>(this, "referrer", String.class, false, null);
		}
		return this.referrer;
	}

    /* WARNING!!! do not use any of the collection classes for this class, you could create an endless recursive loop.*/

	public KeyData<String> fetchComment()
	{
		if (null==this.comment)
		{
			this.comment=new KeyData<>(this, "comment", String.class, false, null);
		}
		return this.comment;
	}

// Getters and setters
	public boolean isSuccess()
	{
		return this.success;
	}

	public void setSuccess(boolean success)
	{
		this.success=success;
	}
}
