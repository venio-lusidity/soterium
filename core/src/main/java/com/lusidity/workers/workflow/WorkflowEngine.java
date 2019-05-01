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

package com.lusidity.workers.workflow;

import com.lusidity.Environment;
import com.lusidity.collections.ElementEdges;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.process.workflow.*;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.workers.assistant.message.WorkflowMessage;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class WorkflowEngine implements Closeable
{
	private static WorkflowEngine INSTANCE=null;
	private Collection<Class<? extends IWorkflowItem>> workflowTypes=new ArrayList<>();

	private WorkflowEngine()
	{
		super();
	}

// Overrides
	@Override
	public void close()
	{
		WorkflowEngine.INSTANCE=null;
	}

// Methods
	public static synchronized WorkflowEngine next()
	{
		if (null==WorkflowEngine.getInstance())
		{
			WorkflowEngine.INSTANCE=new WorkflowEngine();
			Environment.getInstance().registerCloseable(WorkflowEngine.getInstance());
		}
		return WorkflowEngine.getInstance();
	}

	public static WorkflowEngine getInstance()
	{
		return WorkflowEngine.INSTANCE;
	}

	/**
	 * Initializes the workflow process or resets an existing one.
	 * If the next step requires monitoring pass it off to a background worker.
	 *
	 * @param workflowItem The item being handled by a workflow.
	 * @return true if everything went ok.
	 */
	public boolean start(WorkflowItem workflowItem)
	{
		boolean result=(null!=workflowItem);

		if (result)
		{
			try
			{
				ElementEdges<WorkflowStep> steps=workflowItem.getSteps();

				// This will allow a Workflowitem to start all over.
				if (!steps.isEmpty())
				{
					steps.clearAndDelete();
					WorkflowEntry.makeEntry(workflowItem, workflowItem.getInitiators().get(), "Workflow reset.",
						"The workflow has been reset."
					);
					steps.reload();
				}

				if (steps.isEmpty())
				{
					this.init(workflowItem);
				}

				// set the expire time.
				workflowItem.fetchExpiresOn().setValue(DateTimeX.addTimeFrom(workflowItem.fetchCreatedWhen().getValue(),
					workflowItem.fetchUnitOfTime().getValue(), workflowItem.fetchSpan().getValue()
				));

				// TODO use the WorkFflowItem.getExpiresOn() to set up a worker for monitoring.
				result=true;
			}
			catch (Exception ex)
			{
				result=false;
				Environment.getInstance().getReportHandler().severe(ex);
				workflowItem.onError(ex);
			}
		}

		return result;
	}

	private void init(WorkflowItem workflowItem)
		throws ApplicationException
	{
		this.moveNext(null, workflowItem);
	}

	private void moveNext(WorkflowStep step, WorkflowItem workflowItem)
		throws ApplicationException
	{
		boolean process=true;

		// If the steps are in parallel only process if all steps are completed
		// Also ensure that the last step is the step on to be able to move next.
		int size=workflowItem.getSteps().size();
		if (size>1)
		{
			for (WorkflowStep ws : workflowItem.getSteps())
			{
				process=ws.fetchCompleted().getValue();
				if (!process)
				{
					break;
				}
			}
			// Ensure that we are using the last step
			if (process)
			{
				WorkflowStep last=workflowItem.getSteps().getAt((size-1));
				if (null!=last)
				{
					step=last;
				}
			}
		}

		if (process)
		{
			Workflow workflow=workflowItem.getWorkflows().get();
			if (null!=workflow)
			{
				Collection<WorkflowStep> steps=new ArrayList<>();
				boolean found=(null==step);

				try
				{
					for (WorkflowStep ws : workflow.getSteps())
					{
						if (found)
						{
							if (!steps.isEmpty() && (ws.fetchStepType().getValue()==WorkflowStep.StepTypes.sequential))
							{
								break;
							}

							JsonData dso=ws.getVertexData().clone();
							dso.remove(Environment.getInstance().getDataStore().getDataStoreIdKey());
							WorkflowStep workflowStep=new WorkflowStep(dso, null);
							workflowStep.fetchOriginalId().setValue(ws.fetchId().getValue());
							workflowStep.save();

							for (BasePrincipal principal : ws.getAuditors())
							{
								workflowStep.getAuditors().add(principal);
							}

							for (WorkflowStep child : ws.getSteps())
							{
								workflowStep.getSteps().add(child);
							}

							boolean added=steps.add(workflowStep);
							if (added)
							{
								this.sendNotification(workflowItem, workflow, workflowStep);
							}

							if (ws.fetchStepType().getValue()==WorkflowStep.StepTypes.sequential)
							{
								break;
							}
						}

						if ((null!=step) && step.fetchOriginalId().getValue().equals(ws.fetchId().getValue()))
						{
							found=true;
						}
					}

					workflowItem.getSteps().clearAndDelete();

					long on=0L;
					for (WorkflowStep workflowStep : steps)
					{
						StringBuilder auditors=new StringBuilder();

						for (BasePrincipal principal : workflowStep.getAuditors())
						{
							if (auditors.length()>0)
							{
								auditors.append(",");
							}
							auditors.append((String) principal.fetchTitle().getValue());
						}

						WorkflowEntry.makeEntry(workflowItem, workflowItem.getInitiators().get(), "Step change",
							String.format("Workflow starting Auditors: %s", auditors)
						);

						EdgeData edgeData=new EdgeData();
						edgeData.setFromOrdinal(on);
						boolean added=workflowItem.getSteps().add(workflowStep, edgeData, null);
						if (added)
						{
							this.monitor(workflowStep);
						}
						else
						{
							throw new ApplicationException("Could not add step to workflow item, %s.",
								workflowItem.fetchId().getValue()
							);
						}
						on++;
					}
				}
				catch (Exception ex){
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
			else
			{
				throw new ApplicationException("The workflow item does not have any assigned workflows, %s.", workflowItem.fetchId().getValue());
			}
		}
	}

	private void sendNotification(WorkflowItem workflowItem, Workflow workflow, WorkflowStep workflowStep)
	{
		try
		{
			WorkflowNotification notification=new WorkflowNotification();
			if (workflowItem.fetchCompleted().getValue())
			{
				notification.fetchTitle().setValue(String.format("Workflow %s: %s", workflowItem.fetchApproved().getValue() ? "Approved" : "Disapproved", workflowItem.fetchTitle().getValue()));
			}
			else if ((null!=workflow) && (null!=workflowStep) && !workflowStep.getAuditors().isEmpty())
			{
				notification.fetchTitle().setValue(String.format("Action: %s", workflow.fetchTitle().getValue()));
			}

			notification.save();
			notification.getTargets().add(workflowItem);
			for (BasePrincipal principal : workflowItem.getInitiators())
			{
				if (ClassX.isKindOf(principal, Person.class))
				{
					Person person=(Person) principal;
					notification.getInitiators().add(person);
				}
			}

			if (null!=workflowStep)
			{
				notification.getTargets().add(workflowStep);
				for (BasePrincipal principal : workflowStep.getAuditors())
				{
					notification.getReceivers().add(principal);
				}
			}

			if (workflowItem.fetchCompleted().getValue())
			{
				notification.sendFinalNotice(BaseContactDetail.CategoryTypes.work_email);
			}
			else if ((null!=workflow) && (null!=workflowStep) && !workflowStep.getAuditors().isEmpty())
			{
				notification.send(BaseContactDetail.CategoryTypes.work_email);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	/**
	 * Deterimine if this workflow step requires monitoring.  If it does it will submit a workflow message.
	 *
	 * @param workflowStep The workflow step that might require monitoring.
	 */
	private void monitor(WorkflowStep workflowStep)
	{
		WorkflowItem workflowItem=workflowStep.getParentWorkflowItem().get();
		if ((null!=workflowItem) && workflowStep.fetchExpireAction().isNotNullOrEmpty())
		{
			// Only if monitoring is required.
			WorkflowMessage msg=WorkflowMessage.create(workflowStep);
			if ((null==msg) || !msg.hasId())
			{
				Environment.getInstance().getReportHandler().severe("Could not create message for workflow, %s.", workflowItem);
			}
			else
			{
				workflowStep.getMessages().add(msg);
			}
		}
	}

	/**
	 * Determine the next step of the workflow.
	 * If the next step requires monitoring pass it off to a background worker.
	 *
	 * @param step The step currently being handled by a workflow.
	 * @return true if everything went ok.
	 */
	public boolean next(WorkflowStep step)
	{
		boolean result=false;
		try
		{
			WorkflowItem workflowItem=step.getParentWorkflowItem().get();
			if (null!=workflowItem)
			{
				if (step.fetchModifiedWhen().getValue().isAfter(step.fetchCreatedWhen().getValue()))
				{
					result=true;
					WorkflowStep.ActionTypes test = step.fetchActionType().getValue();
					switch (test)
					{
						case approve:
							this.handleApproved(step, workflowItem);
							break;
						case notify:
							this.handleNotify(step, workflowItem);
							break;
						case review:
							this.handleReview(step, workflowItem);
							break;
					}
				}
			}
			else
			{
				Environment.getInstance().getReportHandler().say(
					"Could not find WorkflowItem for step %s.  The step will be deleted.", step.fetchId().getValue());
				step.delete();
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	private void handleApproved(WorkflowStep step, WorkflowItem workflowItem)
		throws Exception
	{
		String msg=String.format("This has been %s by %s.",
			step.fetchApproved().getValue() ? "approved" : "disapproved",
			((null==step.getCredentials())) ? "the system." : step.getCredentials().getPrincipal().fetchTitle().getValue()
		);

		WorkflowEntry.makeEntry(workflowItem, (null==step.getCredentials()) ? null : step.getCredentials().getPrincipal(), "Step change", msg);

		if (step.fetchApproved().getValue())
		{
			this.moveNext(step, workflowItem);
			if (workflowItem.getSteps().isEmpty())
			{
				this.finalize(workflowItem, true);
			}
		}
		else
		{
			this.finalize(workflowItem, false);
		}
	}

	private void handleNotify(WorkflowStep step, WorkflowItem workflowItem)
		throws ApplicationException
	{

	}

	private void handleReview(WorkflowStep step, WorkflowItem workflowItem)
		throws ApplicationException
	{

	}

	private void finalize(WorkflowItem workflowItem, boolean approved)
		throws Exception
	{
		// Clear all linked steps.
		workflowItem.getSteps().clearAndDelete();

		workflowItem.fetchApproved().setValue(approved);
		workflowItem.fetchCompleted().setValue(true);
		workflowItem.save();

		this.sendNotification(workflowItem, null, null);

		// TODO: delete any assistant messages when feature is created.
	}

// Getters and setters
	public Collection<Class<? extends IWorkflowItem>> getWorkflowTypes()
	{
		if (this.workflowTypes.isEmpty())
		{
			Set<Class<? extends IWorkflowItem>> subTypes=Environment.getInstance().getReflections().getSubTypesOf
				(IWorkflowItem.class);
			if (null!=subTypes)
			{
				for (Class<? extends IWorkflowItem> subType : subTypes)
				{
					this.workflowTypes.add(subType);
				}
			}
		}
		return this.workflowTypes;
	}
}
