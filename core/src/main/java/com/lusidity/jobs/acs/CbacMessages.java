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

package com.lusidity.jobs.acs;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePosition;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.system.assistant.message.CbacMessage;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.authorization.IAuthorizationPolicy;

import java.util.Collection;

@AtSchemaClass(name="CBAC Messages", discoverable=false, description="")
public class CbacMessages extends BaseDomain
{
	public static CbacMessages instance=null;
	@AtSchemaProperty(name="CBAC Messages", expectedType=CbacMessage.class)
	private ElementEdges<CbacMessage> messages = null;

// Constructors
	public CbacMessages()
	{
		super();
	}

	public CbacMessages(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		Collection<CbacMessages> results=VertexFactory.getInstance().getAll(CbacMessages.class, 0, 10);
		CbacMessages result;
		if (results.isEmpty())
		{
			result=new CbacMessages();
			result.fetchTitle().setValue(CbacMessages.class.getSimpleName());
			result.save();
		}
		else
		{
			result=CollectionX.getFirst(results);
		}
		CbacMessages.instance=result;
	}

	@Override
	public int getInitializeOrdinal()
	{
		return 1000;
	}

// Methods
	public static CbacMessages getInstance()
	{
		return CbacMessages.instance;
	}

	public void add(DataVertex context, BasePosition position, Class<? extends IAuthorizationPolicy> policy, LogEntry.OperationTypes operationType)
		throws Exception
	{
		CbacMessage cbacMessage = new CbacMessage();
		cbacMessage.setCredentials((null!=context.getCredentials()) ? context.getCredentials() : position.getCredentials());
		cbacMessage.fetchContextRelatedId().setValue(context.getUri().toString());
		cbacMessage.fetchPositionRelatedId().setValue(position.getUri().toString());
		cbacMessage.fetchOperationType().setValue(operationType);
		cbacMessage.fetchPolicy().setValue(policy);
		cbacMessage.save();

		if(cbacMessage.hasId())
		{
			this.getMessages().add(cbacMessage);
		}
		else
		{
			Environment.getInstance().getReportHandler().info("Could not save message for CBAC Job, user trying to create the message may not be in the power user group.  Which is ok only users that are allowed to authorize accounts should have this permission.");
		}
	}

	public ElementEdges<CbacMessage> getMessages()
	{
		if (null==this.messages)
		{
			this.buildProperty("messages");
		}
		return this.messages;
	}

// Getters and setters
}
