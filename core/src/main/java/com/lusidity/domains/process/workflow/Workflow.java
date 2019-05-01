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

import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="Workflow", discoverable=false, writable=true, description="Consists of an orchestrated and repeatable "+
                                                                               "pattern of business "+
                                                                               "activity enabled by the systematic organization of resources into processes that step3 materials, "+
                                                                               "provide services, or process information.")
public class Workflow extends BaseDomain
{
	private KeyData<Class> workflowType=null;

	@AtSchemaProperty(name="Workflow Steps", expectedType=WorkflowStep.class,
		description="The children steps of this workflow.", sortKey=Endpoint.KEY_FROM_EP_ORDINAL)
	private ElementEdges<WorkflowStep> steps=null;

	// Constructors
	public Workflow()
	{
		super();
	}

	@SuppressWarnings("unused")
	public Workflow(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Getters and setters
	@SuppressWarnings("unused")
	public KeyData<Class> fetchWorkflowType()
	{
		if(null==this.workflowType){
			// expects Class<? extends IWorkflowItem>, could write a handler to enforce.
			this.workflowType = new KeyData<>(this, "workflowType", Class.class, false, null);
		}
		return this.workflowType;
	}

	public ElementEdges<WorkflowStep> getSteps()
	{
		if (null==this.steps)
		{
			this.buildProperty("steps");
		}
		return this.steps;
	}
}
