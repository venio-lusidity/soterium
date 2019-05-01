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
import com.lusidity.domains.system.assistant.message.AssistantMessage;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.workers.assistant.AssistantListener;
import com.lusidity.workers.assistant.IAssistantListener;

import java.util.Objects;

@SuppressWarnings("UnusedDeclaration")
@AtSchemaClass(name="Assistant Manager", discoverable=false)
public class ExampleWorker extends BaseAssistantWorker
{
	// Fields
	public static final String QUEUE_KEY="/system/assistant/message/updates";
	private static ExampleWorker INSTANCE=null;
	@AtSchemaProperty(name="Queue", expectedType=AssistantMessage.class, limit=1000)
	private ElementEdges<AssistantMessage> queue=null;
	private IAssistantListener listener=null;

	// Constructors
	public ExampleWorker()
	{
		super();
		ExampleWorker.INSTANCE=this;
	}

	public ExampleWorker(JsonData dso, Object indexId)
	{
		super(dso, indexId);
		ExampleWorker.INSTANCE=this;
	}

	@Override
	public void reset()
	{

	}

	@Override
	public synchronized IAssistantListener getListener()
	{
		if(null==this.listener){
			this.listener = new AssistantListener(this);
		}
		return this.listener;
	}

	@Override
	public synchronized void setListener(IAssistantListener listener)
	{
		this.listener = listener;
	}

	// Overrides
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
					if (!Objects.equals(msg.fetchStatus().getValue(), AssistantMessage.Status.waiting))
					{
						msg.fetchStatus().setValue(AssistantMessage.Status.waiting);
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
		AssistantMessage msg=null;
		try
		{
			this.setProcessing(true);
			msg=this.getQueue().get();
			if (null!=msg)
			{
				msg.fetchStatus().setValue(AssistantMessage.Status.processing);

			}
			else
			{
				Environment.getInstance().getReportHandler().severe("The message was null, %s.", this.getClass().getName());
			}
		}
		catch (Exception ex)
		{
			if ((null!=msg) && this.isStarted())
			{
				msg.fetchStatus().setValue(AssistantMessage.Status.failed);
				msg.fetchMessage().setValue(ex.getMessage());
			}
		}
		finally
		{
			if (this.isStarted())
			{
				if (null!=msg)
				{
					if (!Objects.equals(msg.fetchStatus().getValue(), AssistantMessage.Status.processed))
					{
						msg.fetchStatus().setValue(AssistantMessage.Status.failed);
					}
				}
				try
				{
					this.completed();
					boolean removed=this.getQueue().remove(msg);
					if (removed && (null!=msg))
					{
						msg.delete();
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
				this.setProcessing(false);
			}
			else if (null!=msg)
			{
				msg.fetchStatus().setValue(AssistantMessage.Status.waiting);
				msg.fetchMessage().setValue("Worker was interupted and will be recycled.");
			}
		}
	}

	@Override
	public Class<? extends AssistantMessage> getExpectedMessageType()
	{
		return AssistantMessage.class;
	}

	@Override
	public String getName()
	{
		return "Example";
	}

	@Override
	public synchronized boolean add(AssistantMessage assistantMessage)
	{
		return this.getQueue().add(assistantMessage);
	}

	// Methods
	public static ExampleWorker getInstance()
	{
		return ExampleWorker.INSTANCE;
	}
}
