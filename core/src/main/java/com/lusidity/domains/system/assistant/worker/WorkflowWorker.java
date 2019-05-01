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

package com.lusidity.domains.system.assistant.worker;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.system.assistant.message.AssistantMessage;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.jobs.IJob;
import com.lusidity.workers.assistant.AssistantListener;
import com.lusidity.workers.assistant.IAssistantListener;
import com.lusidity.workers.assistant.message.WorkflowMessage;
import com.lusidity.workers.workflow.WorkflowEngine;

@AtSchemaClass(name="Workflow Worker", discoverable=false)
public class WorkflowWorker extends BaseAssistantWorker
{
	@AtSchemaProperty(name="Queue", expectedType=WorkflowMessage.class)
	private ElementEdges<AssistantMessage> queue=null;
	private IAssistantListener listener=null;

	// Constructors
	@SuppressWarnings("unused")
	public WorkflowWorker()
	{
		super();
	}

	@SuppressWarnings("unused")
	public WorkflowWorker(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public boolean stop()
	{
		this.setStarted(false);
		boolean result=super.stop();
		this.setProcessing(false);
		return result;
	}

	@Override
	public void reset()
	{

	}

	@Override
	public synchronized IAssistantListener getListener()
	{
		if (null==this.listener)
		{
			this.listener=new AssistantListener(this);
		}
		return this.listener;
	}

	@Override
	public synchronized void setListener(IAssistantListener listener)
	{
		this.listener=listener;
	}

	@Override
	protected boolean requiresListener()
	{
		return true;
	}

	@Override
	public synchronized ElementEdges<AssistantMessage> getQueue()
	{
		if (null==this.queue)
		{
			this.buildProperty("queue");
			// reset all messages to waiting.
			for (AssistantMessage msg : this.queue)
			{
				try
				{
					if (msg.fetchStatus().getValue()!=AssistantMessage.Status.waiting)
					{
						msg.fetchStatus().setValue(AssistantMessage.Status.waiting);
						msg.save();
					}
				}
				catch (Exception ignored)
				{
				}
			}
		}
		return this.queue;
	}

	@SuppressWarnings("OverlyComplexMethod")
	@Override
	public void doWork(int tries)
	{
		WorkflowMessage message=null;
		try
		{
			this.setProcessing(true);
			AssistantMessage msg=this.getQueue().get();
			if (msg instanceof WorkflowMessage)
			{
				message=(WorkflowMessage) msg;
				message.fetchStatus().setValue(AssistantMessage.Status.processing);
				message.start();
				if (null!=message.getWorkflowStep())
				{
					WorkflowEngine.getInstance().next(message.getWorkflowStep());
				}
			}
			else
			{
				msg.fetchStatus().setValue(AssistantMessage.Status.processed);
			}
		}
		catch (Exception ex)
		{
			if (null!=message)
			{
				message.fetchStatus().setValue(AssistantMessage.Status.failed);
				message.fetchMessage().setValue(ex.getMessage());
			}
		}
		finally
		{
			if (null!=message)
			{
				message.stop();
				if (message.fetchStatus().getValue()!=AssistantMessage.Status.processed)
				{
					message.fetchStatus().setValue(AssistantMessage.Status.failed);
				}
				try
				{
					JobHistory history=JobHistory.create("Workflow", message.fetchWorkerClass().getValue(), message.getStopwatch(), new ProcessStatus(), IJob.Status.processed);
					history.save();
				}
				catch (Exception ignored){}
			}
			try
			{
				boolean removed=this.getQueue().remove(message);
				if (removed && (null!=message))
				{
					message.delete();
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
			this.completed();
			this.setProcessing(false);
		}
	}

	@Override
	public Class<? extends AssistantMessage> getExpectedMessageType()
	{
		return WorkflowMessage.class;
	}

	@Override
	public String getName()
	{
		return "workflow_worker";
	}

	@Override
	public boolean add(AssistantMessage assistantMessage)
	{
		boolean result=false;
		try
		{
			if (null!=assistantMessage)
			{
				if (assistantMessage.fetchId().isNullOrEmpty())
				{
					assistantMessage.save();
				}

				if (!assistantMessage.fetchId().isNullOrEmpty())
				{
					result=this.getQueue().add(assistantMessage);
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}
}
