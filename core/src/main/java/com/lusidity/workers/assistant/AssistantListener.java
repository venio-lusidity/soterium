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

package com.lusidity.workers.assistant;

import com.lusidity.Environment;
import com.lusidity.domains.system.assistant.worker.BaseAssistantWorker;
import com.lusidity.system.SystemInfo;

public class AssistantListener implements IAssistantListener
{

	private final BaseAssistantWorker assistantWorker;
	private boolean stopped=false;

// Constructors
	public AssistantListener(BaseAssistantWorker assistantWorker)
	{
		super();
		this.assistantWorker=assistantWorker;
	}

// Overrides
	@Override
	public Boolean call()
		throws Exception
	{
		if (this.assistantWorker.isStarted())
		{
			try
			{
				Thread.sleep(this.assistantWorker.getDelay());
			}
			catch (Exception ignored)
			{
			}
			Environment.getInstance().getReportHandler().info("%s listener has started.", this.assistantWorker.getClass().getSimpleName());
			try
			{
				if (!Thread.interrupted())
				{
					this.listen();
				}
			}
			catch (Exception ignored)
			{
				if (!this.assistantWorker.getQueue().isEmpty())
				{
					this.assistantWorker.recycle(this.assistantWorker.getQueue().get());
				}

				Environment.getInstance().getReportHandler().warning("%s listener is restarting.", this.assistantWorker.getClass().getSimpleName());
				this.assistantWorker.restart();
			}
		}
		return true;
	}

	@Override
	public synchronized void stop()
	{
		this.stopped=true;
	}

	@Override
	public void listen()
	{
		while (!Thread.interrupted() && this.assistantWorker.isStarted() && !this.stopped)
		{
			if (!this.assistantWorker.isProcessing() && !Environment.getInstance().getDataStore().isOffline())
			{
				Double load=SystemInfo.getInstance().getProcessorLoad();
				if (((load<this.assistantWorker.getIdleThreshold()) && !this.assistantWorker.getQueue().isEmpty()))
				{
					this.assistantWorker.doWork(0);
				}
			}
			try
			{
				Thread.sleep(this.assistantWorker.getIdleThreshold());
			}
			catch (Exception ignored)
			{
			}
		}
	}
}
