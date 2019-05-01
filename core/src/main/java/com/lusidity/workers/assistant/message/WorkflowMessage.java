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

package com.lusidity.workers.assistant.message;


import com.lusidity.Environment;
import com.lusidity.annotations.AtIndexedField;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.process.workflow.WorkflowStep;
import com.lusidity.domains.system.assistant.message.AssistantMessage;
import com.lusidity.domains.system.assistant.worker.WorkflowWorker;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

@AtIndexedField(key="cls", type=String.class)
@AtIndexedField(key="workflowItemId", type=String.class)
@AtSchemaClass(name="FileInfo Import Message", discoverable=false, description="A message about a file used to import data.", writable=true)
public class WorkflowMessage extends AssistantMessage
{

	private Class<? extends DataVertex> cls=null;
	private String workflowItemId=null;

	private transient WorkflowStep workflowStep=null;

// Constructors
	@SuppressWarnings("unused")
	public WorkflowMessage()
	{
		super();
	}

	@SuppressWarnings("unused")
	public WorkflowMessage(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public WorkflowMessage(WorkflowStep workflowStep)
	{
		super();
		this.workflowStep=workflowStep;
		this.cls=this.workflowStep.getClass();
		this.workflowItemId=this.workflowStep.fetchId().getValue();
	}

// Methods
	public static WorkflowMessage create(WorkflowStep workflowStep)
	{
		WorkflowMessage result=null;
		try
		{
			if ((null!=workflowStep) && !StringX.isBlank(workflowStep.fetchId().getValue()))
			{
				result=new WorkflowMessage(workflowStep);
				result.save();

				WorkflowWorker worker=Environment.getInstance().getWorker(WorkflowWorker.class);
				worker.add(result);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

// Getters and setters
	public WorkflowStep getWorkflowStep()
	{
		if (null==this.workflowStep)
		{
			this.workflowStep=Environment.getInstance().getDataStore().getObjectById(
				this.getCls(), this.getCls(), this.getWorkflowItemId());
		}
		return this.workflowStep;
	}

	public Class<? extends DataVertex> getCls()
	{
		return this.cls;
	}

	public void setCls(Class<? extends DataVertex> cls)
	{
		this.cls=cls;
	}

	public String getWorkflowItemId()
	{
		return this.workflowItemId;
	}

	@SuppressWarnings("unused")
	public void setWorkflowItemId(String workflowItemId)
	{
		this.workflowItemId=workflowItemId;
	}
}
