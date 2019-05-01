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

package com.lusidity.domains.process.workflow;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.workers.assistant.message.WorkflowMessage;
import com.lusidity.workers.workflow.IWorkflow;
import com.lusidity.workers.workflow.WorkflowEngine;
import org.joda.time.DateTime;

@SuppressWarnings({
	"unused",
	"ClassWithTooManyFields"
})
@AtSchemaClass(name="Workflow Step", discoverable=false, writable=true, description="Represents one part of the process in a workflow.")
public class WorkflowStep extends BaseDomain implements IWorkflow
{
	public enum ActionTypes
	{
		approve,
		notify,
		review
	}

	public enum ExpireActions
	{
		reject,
		previous,
		next
	}

	public enum StepTypes
	{
		parallel,
		sequential
	}

	private KeyData<Boolean> approved=null;
	private KeyData<Boolean> completed=null;
	private KeyData<WorkflowStep.ActionTypes> actionType=null;
	private KeyData<Integer> expiresIn=null;
	private KeyData<DateTimeX.UnitOfTime> expireType=null;
	private KeyData<WorkflowStep.ExpireActions> expireAction=null;
	private KeyData<WorkflowStep.StepTypes> stepType=null;
	private KeyData<String> originalId=null;

	@AtSchemaProperty(name="Workflow Steps", expectedType=WorkflowStep.class, sortKey="order",
		description="The children steps of this workflow.")
	private ElementEdges<WorkflowStep> steps=null;
	@AtSchemaProperty(name="Auditor", expectedType=BasePrincipal.class,
		description="A user or group of users that review a workflow step which may require them to perform"+
		            " some sort of action.", limit=Environment.COLLECTIONS_DEFAULT_LIMIT)
	private ElementEdges<BasePrincipal> auditors=null;
	@AtSchemaProperty(name="Parent Workflow Item", labelType=WorkflowStep.class,
		expectedType=WorkflowItem.class, fieldName="steps",
		direction=Common.Direction.IN, description="The steps currently being processed.")
	private ElementEdges<WorkflowItem> parentWorkflowItem=null;
	@AtSchemaProperty(name="Messages", expectedType=WorkflowMessage.class, isSingleInstance=true)
	private ElementEdges<WorkflowMessage> messages=null;

	// Constructors
	public WorkflowStep()
	{
		super();
	}

	public WorkflowStep(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public void workflowCancelled()
	{

	}

	@Override
	public void workflowDisapproved()
	{

	}

	@Override
	public void workflowApproved()
	{
	}

	@Override
	public void afterUpdate(LogEntry.OperationTypes operationType, boolean success)
	{
		//noinspection SwitchStatementWithoutDefaultBranch
		switch (operationType)
		{
			case create:
				break;
			case update:
				if (!this.getParentWorkflowItem().isEmpty())
				{
					WorkflowEngine.getInstance().next(this);
				}
				break;
			case delete:
				break;
		}
		super.afterUpdate(operationType, success);
	}

	public ElementEdges<WorkflowItem> getParentWorkflowItem()
	{
		if (null==this.parentWorkflowItem)
		{
			this.buildProperty("parentWorkflowItem");
		}
		return this.parentWorkflowItem;
	}

	public KeyData<Boolean> fetchApproved()
	{
		if (null==this.approved)
		{
			this.approved=new KeyData<>(this, "approved", Boolean.class, false, false);
		}
		return this.approved;
	}

	public KeyData<Boolean> fetchCompleted()
	{
		if (null==this.completed)
		{
			this.completed=new KeyData<>(this, "completed", Boolean.class, false, false);
		}
		return this.completed;
	}

	public KeyData<WorkflowStep.ExpireActions> fetchExpireAction()
	{
		if (null==this.expireAction)
		{
			this.expireAction=new KeyData<>(this, "expireAction", WorkflowStep.ExpireActions.class, false, null);
		}
		return this.expireAction;
	}

	public KeyData<String> fetchOriginalId()
	{
		if (null==this.originalId)
		{
			this.originalId=new KeyData<>(this, "originalId", String.class, false, null);
		}
		return this.originalId;
	}

	public KeyData<WorkflowStep.StepTypes> fetchStepType()
	{
		if (null==this.stepType)
		{
			this.stepType=new KeyData<>(this, "stepType", WorkflowStep.StepTypes.class, false, WorkflowStep.StepTypes.sequential);
		}
		return this.stepType;
	}

	// Getters and setters
	public DateTime getExpiresOn()
	{
		// TODO add to JSON WriteConsoleJob for web ui, store?.
		DateTime result=null;
		if (this.fetchExpireType().isNotNullOrEmpty() && this.fetchExpiresIn().isNotNullOrEmpty() &&
		    ((this.fetchExpiresIn().getValue())>0) && this.fetchActionType().isNotNullOrEmpty())
		{
			result=DateTimeX.addTimeFrom(this.fetchCreatedWhen().getValue(), this.fetchExpireType().getValue(), this.fetchExpiresIn().getValue());
		}
		return result;
	}

	public KeyData<DateTimeX.UnitOfTime> fetchExpireType()
	{
		if (null==this.expireType)
		{
			this.expireType=new KeyData<>(this, "expireType", DateTimeX.UnitOfTime.class, false, null);
		}
		return this.expireType;
	}

	public KeyData<Integer> fetchExpiresIn()
	{
		if (null==this.expiresIn)
		{
			this.expiresIn=new KeyData<>(this, "expiresIn", Integer.class, false, null);
		}
		return this.expiresIn;
	}

	public KeyData<WorkflowStep.ActionTypes> fetchActionType()
	{
		if (null==this.actionType)
		{
			this.actionType=new KeyData<>(this, "actionType", WorkflowStep.ActionTypes.class, false, null);
		}
		return this.actionType;
	}

	public ElementEdges<WorkflowStep> getSteps()
	{
		if (null==this.steps)
		{
			this.buildProperty("steps");
		}
		return this.steps;
	}

	public ElementEdges<BasePrincipal> getAuditors()
	{
		if (null==this.auditors)
		{
			this.buildProperty("auditors");
		}
		return this.auditors;
	}

	public ElementEdges<WorkflowMessage> getMessages()
	{
		if (null==this.messages)
		{
			this.buildProperty("messages");
		}
		return this.messages;
	}
}
