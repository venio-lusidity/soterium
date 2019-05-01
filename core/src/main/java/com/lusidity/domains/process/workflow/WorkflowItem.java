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
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.common.IExpires;
import com.lusidity.domains.document.form.Form;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.object.edge.LogEdge;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.workers.workflow.IWorkflowItem;
import com.lusidity.workers.workflow.WorkflowEngine;
import org.joda.time.DateTime;

@SuppressWarnings("ClassWithTooManyFields")
@AtSchemaClass(name="Workflow", discoverable=false, writable=true, description="Consists of an orchestrated and repeatable "+
                                                                               "pattern of business "+
                                                                               "activity enabled by the systematic organization of resources into processes that step3 materials, provide "+
                                                                               "services, or process information.")
public abstract class WorkflowItem extends Form implements IExpires, IWorkflowItem
{
	private KeyData<Boolean> approved=null;
	private KeyData<DateTime> dateApproved=null;
	private KeyData<Boolean> completed=null;
	private KeyData<DateTime> expiresOn=null;
	private KeyData<String> reason=null;
	private KeyData<Integer> span=null;
	private KeyData<DateTimeX.UnitOfTime> unitOfTime=null;

	@AtSchemaProperty(name="Actions", expectedType=WorkflowEntry.class, edgeType=LogEdge.class,
		description="Determines if the user had approved, disapproved and/or reviewed.",
		limit=Environment.COLLECTIONS_DEFAULT_LIMIT)
	private ElementEdges<WorkflowEntry> actions=null;
	@AtSchemaProperty(name="On Steps", expectedType=WorkflowStep.class,
		description="The steps currently being processed.", sortKey=Endpoint.KEY_FROM_EP_ORDINAL)
	private ElementEdges<WorkflowStep> steps=null;
	@AtSchemaProperty(name="Initiator", description="The person who initiated the POA&M.",
		expectedType=Person.class, isSingleInstance=true)
	private ElementEdges<Person> initiators=null;
	@AtSchemaProperty(name="Workflows", expectedType=Workflow.class, isSingleInstance=true,
		description="The processes in which this POA&M will be reviewed and approved or disapproved.")
	private ElementEdges<Workflow> workflows=null;
	@AtSchemaProperty(name="Targets", expectedType=DataVertex.class, description="The vertices that this workflow item is processing.")
	private ElementEdges<DataVertex> targets=null;

	// Constructors
	public WorkflowItem()
	{
		super();
	}

	public WorkflowItem(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public KeyData<Boolean> fetchApproved()
	{
		if (null==this.approved)
		{
			this.approved=new KeyData<>(this, "approved", Boolean.class, false, false);
		}
		return this.approved;
	}


	@Override
	public KeyData<Boolean> fetchCompleted()
	{
		if (null==this.completed)
		{
			this.completed=new KeyData<>(this, "completed", Boolean.class, false, false);
		}
		return this.completed;
	}

	@Override
	public KeyData<String> fetchReason()
	{
		if (null==this.reason)
		{
			this.reason=new KeyData<>(this, "reason", String.class, false, null);
		}
		return this.reason;
	}

	@Override
	public KeyData<DateTime> fetchDateApproved()
	{
		if (null==this.dateApproved)
		{
			this.dateApproved=new KeyData<>(this, "dateApproved", DateTime.class, false, null);
		}
		return this.dateApproved;
	}

	@Override
	public void afterEdgeUpdate(LogEntry.OperationTypes operationType, DataVertex other, Edge edge, boolean success)
	{
		//noinspection SwitchStatementWithoutDefaultBranch,EnumSwitchStatementWhichMissesCases
		switch (operationType)
		{
			case create:
				if (success && ClassX.isKindOf(other, Workflow.class))
				{
					try
					{
						if ((null!=this.getCredentials()) && (null!=this.getCredentials().getPrincipal()))
						{
							Person person=(Person) this.getCredentials().getPrincipal();
							this.getInitiators().add(person);
							WorkflowEngine.getInstance().start(this);
						}
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().severe(ex);
					}
				}
				break;
			case update:
				break;
			case delete:
				break;
		}
		super.afterEdgeUpdate(operationType, other, edge, success);
	}

	public ElementEdges<Person> getInitiators()
	{
		if (null==this.initiators)
		{
			this.buildProperty("initiators");
		}
		return this.initiators;
	}

	@Override
	public KeyData<DateTimeX.UnitOfTime> fetchUnitOfTime()
	{
		if (null==this.unitOfTime)
		{
			this.unitOfTime=new KeyData<>(this, "unitOfTime", DateTimeX.UnitOfTime.class, false, DateTimeX.UnitOfTime.days);
		}
		return this.unitOfTime;
	}

	@Override
	public KeyData<Integer> fetchSpan()
	{
		if (null==this.span)
		{
			this.span=new KeyData<>(this, "span", Integer.class, false, DateTimeX.NUM_DAYS_IN_YEAR);
		}
		return this.span;
	}

	@Override
	public KeyData<DateTime> fetchExpiresOn()
	{
		if (null==this.expiresOn)
		{
			this.expiresOn=new KeyData<>(this, "expiresOn", DateTime.class, false, null);
		}
		return this.expiresOn;
	}

	// Getters and setters
	public ElementEdges<WorkflowEntry> getActions()
	{
		if (null==this.actions)
		{
			this.buildProperty("actions");
		}
		return this.actions;
	}

	public ElementEdges<WorkflowStep> getSteps()
	{
		if (null==this.steps)
		{
			this.buildProperty("steps");
		}
		return this.steps;
	}

	public ElementEdges<Workflow> getWorkflows()
	{
		if (null==this.workflows)
		{
			this.buildProperty("workflows");
		}
		return this.workflows;
	}

	public ElementEdges<DataVertex> getTargets()
	{
		if (null==this.targets)
		{
			this.buildProperty("targets");
		}
		return this.targets;
	}
}
