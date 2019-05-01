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
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.system.assistant.message.AssistantMessage;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.workers.assistant.IAssistantListener;
import com.lusidity.workers.assistant.IAssistantWorker;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@AtSchemaClass(name="Base Assistant Worker", discoverable=false)
public abstract class BaseAssistantWorker extends BaseDomain implements IAssistantWorker, Closeable
{
	// Fields
	public static final int DEFAULT_IDLE_THRESHOLD=50;
	public static final int DEFAULT_DELAY=500;
	private static final long AWAIT_TERMINATION=60 /* seconds */;
	private transient volatile boolean started=false;
	private transient volatile ExecutorService executorService=null;
	private transient volatile boolean processing=false;
	private transient volatile int delay=BaseAssistantWorker.DEFAULT_DELAY;
	private transient volatile int idleThreshold=BaseAssistantWorker.DEFAULT_IDLE_THRESHOLD;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private volatile int processed=0;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private volatile int failed=0;

	// Constructors
	public BaseAssistantWorker()
	{
		super();
	}

	public BaseAssistantWorker(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	@SuppressWarnings("UnusedDeclaration")
	public BaseAssistantWorker(JsonData dso)
	{
		super(dso, null);
	}

// Overrides

	/**
	 * Send a message to the end of the queue.
	 *
	 * @param assistantMessage An AssistantMessage.
	 * @return true if the message was sent to the end of the queue.
	 */
	@Override
	public boolean recycle(Object assistantMessage)
	{
		boolean result=false;
		if (assistantMessage instanceof AssistantMessage)
		{
			AssistantMessage msg=(AssistantMessage) assistantMessage;
			AssistantMessage newMessage=new AssistantMessage(msg.fetchObjectId().getValue());
			result=this.remove(msg);
			if (result)
			{
				result=this.add(newMessage);
			}
		}
		return result;
	}

	/**
	 * Stop the listener if it is stopped and then start the listener.
	 *
	 * @return true if restarted.
	 */
	@Override
	public boolean restart()
	{
		this.stop();
		if (!this.isStarted())
		{
			this.start(this.getDelay(), this.getIdleThreshold());
		}
		return this.isStarted();
	}

	/**
	 * The time to wait between messages being processed.
	 *
	 * @param delay         Time in milliseconds to wait.
	 * @param idleThreshold If the processor utilization exceeds the specified threshold, idle the processor until resources are freed up.
	 * @return true if the worker has started.
	 */
	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public boolean restart(int delay, int idleThreshold)
	{
		this.setDelay(delay);
		this.setIdleThreshold(idleThreshold);
		return this.restart();
	}

	/**
	 * The time to wait between messages being processed.
	 *
	 * @param delay         Time in milliseconds to wait.
	 * @param idleThreshold If the processor utilization exceeds the specified threshold, idle the processor until resources are freed up.
	 * @return true if the worker has started.
	 */
	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public boolean start(int delay, int idleThreshold)
	{
		try
		{
			this.setDelay(delay);
			this.setIdleThreshold(idleThreshold);
			this.stop();
			if (!this.started)
			{
				if (null!=this.executorService)
				{
					this.executorService.shutdown();
					this.executorService=null;
				}
				this.executorService=Executors.newSingleThreadExecutor();
				if (!this.requiresListener())
				{
					this.getQueue().add(new AssistantMessage("startup", null));
				}
				this.started=true;
				this.executorService.submit(this.getListener());
				Environment.getInstance().getReportHandler().info("Assistant worker %s, started.", this.getClass().getSimpleName());
			}
			else
			{
				Environment.getInstance().getReportHandler().info("Assistant worker is already started, %s.", this.getClass().getSimpleName());
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning("The worker could not be started, %s. \n\n %s", this.getClass().getSimpleName(), ex);
		}
		return this.isStarted();
	}

	/**
	 * Stop the worker.
	 *
	 * @return true if the worker was stopped.
	 */
	@Override
	public boolean stop()
	{
		this.started=false;
		try
		{
			if (null!=this.executorService)
			{
				this.getListener().stop();
				// Ensure that no new tasks are added.
				this.executorService.shutdown();
				// Allow one minute for clean up.
				this.executorService.awaitTermination(BaseAssistantWorker.AWAIT_TERMINATION, TimeUnit.SECONDS);
			}
		}
		catch (Exception ignored)
		{
			Environment.getInstance().getReportHandler().warning("The assistant worker did not properly shutdown.");
		}
		finally
		{
			this.executorService=null;
			this.setListener(null);
		}
		return !this.isStarted();
	}

	/**
	 * Is a message currently processing?
	 *
	 * @return true if a message is currently processing.
	 */
	@Override
	public boolean isProcessing()
	{
		return this.processing;
	}

	public abstract void reset();

	protected void setProcessing(Boolean value)
	{
		this.processing=value;
	}

	/**
	 * Is the worker listening?
	 *
	 * @return true if the worker is listening.
	 */
	@Override
	public synchronized boolean isStarted()
	{
		return this.started;
	}

	@Override
	public  synchronized void setStarted(boolean started){
		this.started = started;
	}

	public int getDelay()
	{
		return this.delay;
	}

	public int getIdleThreshold()
	{
		return this.idleThreshold;
	}

	public abstract IAssistantListener getListener();

	public abstract void setListener(IAssistantListener listener);

	protected abstract boolean requiresListener();

	public void setIdleThreshold(int idleThreshold)
	{
		this.idleThreshold=(idleThreshold<=0) ? BaseAssistantWorker.DEFAULT_IDLE_THRESHOLD : idleThreshold;
	}

	public void setDelay(int delay)
	{
		this.delay=delay;
	}

	@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
	public boolean remove(AssistantMessage msg)
	{
		return this.getQueue().remove(msg);
	}

	public abstract ElementEdges<AssistantMessage> getQueue();

	@Override
	public void close()
		throws IOException
	{
		this.stop();
	}

	// Methods
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod"
	})
	public static BaseAssistantWorker getOrCreate(Class<? extends BaseAssistantWorker> clsWorker)
		throws ApplicationException
	{
		BaseAssistantWorker result=null;
		AtSchemaClass atSchemaClass=clsWorker.getAnnotation(AtSchemaClass.class);
		if (null==atSchemaClass)
		{
			throw new ApplicationException("The worker does not have a SchemaClassAnnotation.");
		}

		String key=ClassHelper.getClassKey(clsWorker);
		if (StringX.isBlank(key))
		{
			throw new ApplicationException("The worker does not have a SchemaClassAnnotation id.");
		}

		QueryResults queryResults=Environment.getInstance().getQueryFactory().matchAll(clsWorker, clsWorker, null, 0, 10);
		if (queryResults.size()>1)
		{
			int found=0;
			for (IQueryResult queryResult : queryResults)
			{
				BaseAssistantWorker assistantWorker=queryResult.getVertex();
				if (assistantWorker.getQueue().isEmpty())
				{
					assistantWorker.delete();
				}
				else
				{
					if (found==0)
					{
						result=assistantWorker;
					}
					else
					{
						assistantWorker.delete();
					}
					found++;
				}
			}
			Environment.getInstance().getReportHandler()
			           .warning("For each type of worker there can only be one, found %d %s.  All empty workers have been deleted.", queryResults.size(), clsWorker.getName());
		}
		IQueryResult queryResult=CollectionX.getFirst(queryResults);
		if (null!=queryResult)
		{
			result=queryResult.getVertex();
		}

		if (null==result)
		{
			try
			{
				Constructor constructor=clsWorker.getConstructor();
				//noinspection unchecked
				result=(BaseAssistantWorker) constructor.newInstance();
				result.fetchIdentifiers().add(new UriValue(key));
				if (!StringX.isBlank(atSchemaClass.name()))
				{
					result.fetchTitle().setValue(atSchemaClass.name());
				}
				else
				{
					String domainKey=ClassHelper.getClassKey(clsWorker);
					String name=StringX.getLast(domainKey, "/");
					result.fetchTitle().setValue(StringX.insertSpaceAtCapitol(name));
				}
				result.save();
			}
			catch (Exception e)
			{
				throw new ApplicationException(e);
			}
		}

		return result;
	}

	public abstract void doWork(int tries);

	public boolean kill()
	{
		try
		{
			this.executorService.shutdownNow();
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		this.executorService=null;
		this.setListener(null);
		this.started=false;
		return !this.isStarted();
	}

	@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
	public boolean remove(DataVertex vertex)
	{
		boolean result=false;
		if (!vertex.fetchId().isNullOrEmpty())
		{
			AssistantMessage msg=null;
			for (AssistantMessage assistantMessage : this.getQueue())
			{
				if (assistantMessage.fetchObjectId().equals(vertex.fetchId()))
				{
					msg=assistantMessage;
					break;
				}
			}
			result=this.remove(msg);
			if (result && (null!=msg))
			{
				try
				{
					msg.delete();
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
		}
		return result;
	}

	public JsonData getReport(boolean detailed)
	{
		JsonData result=JsonData.createObject();
		if (detailed)
		{
			result.put("label", this.fetchTitle().getValue());
			result.put("started", JsonData.makeLabel("Worker Started", this.isStarted()));
			result.put("processed", JsonData.makeLabel("Processed", this.getProcessed()));
			result.put("queued", JsonData.makeLabel("Queued", (null!=this.getQueue()) ? this.getQueue().size() : null));
			result.put("delay", JsonData.makeLabel("Delay", this.getDelay()));
			result.put("idleThreshold", JsonData.makeLabel("Idle Threshold", this.getIdleThreshold()));
		}
		return result;
	}

	public int getProcessed()
	{
		return this.processed;
	}

	public void setProcessed(int processed)
	{
		this.processed=processed;
	}


	protected synchronized void completed()
	{
		this.processed++;
	}

	protected synchronized void failed()
	{
		this.failed++;
	}

	// Getters and setters
	@SuppressWarnings("unused")
	public abstract Class<? extends AssistantMessage> getExpectedMessageType();

	public abstract String getName();

	public int getFailed()
	{
		return this.failed;
	}

	public void setFailed(int failed)
	{
		this.failed=failed;
	}
}
