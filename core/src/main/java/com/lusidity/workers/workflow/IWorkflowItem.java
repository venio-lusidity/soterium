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


import com.lusidity.data.field.KeyData;

public interface IWorkflowItem
{
	/* use https://camunda.org/bpmn/reference/ as a reference */

	boolean onStart(IWorkflow workflow);

	void onExpired(IWorkflow workflow);

	void onApproved(IWorkflow workflow);

	void onDisapproved(IWorkflow workflow);

	void onCancelled(IWorkflow workflow);

	void onError(Exception ex);

	// Getters and setters
	KeyData fetchApproved();

	KeyData fetchCompleted();

	KeyData fetchReason();

	KeyData fetchDateApproved();
}
