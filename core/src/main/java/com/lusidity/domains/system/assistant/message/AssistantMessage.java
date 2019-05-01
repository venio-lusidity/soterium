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

package com.lusidity.domains.system.assistant.message;

import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDataHandlerSaveAfterSet;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.framework.time.TimeX;

import java.util.Objects;

@SuppressWarnings("EqualsAndHashcode")
@AtSchemaClass(name="Assistant Message", discoverable=false)
public class AssistantMessage extends BaseDomain
{

	public enum Status
	{
		failed,
		processed,
		processing,
		recycle,
		waiting
	}
	
	private KeyData<String> objectId=null;
	private KeyData<AssistantMessage.Status> status=null;
	private KeyData<Class> workerClass=null;
	private KeyData<String> message=null;
	private transient Stopwatch stopwatch=null;

// Constructors
	/**
	 * Constructor.
	 * <p/>
	 * IMPORTANT: You MUST call build() after instantiating an Entity-derived object in order for the Entity
	 * to be usable.
	 *
	 * @param dso Underlying data store object.
	 */
	public AssistantMessage(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public AssistantMessage()
	{
		super();
	}

	public AssistantMessage(String objectId)
	{
		super();
		this.fetchObjectId().setValue(objectId);
	}

	public AssistantMessage(String title, String id)
	{
		super();
		this.fetchTitle().setValue(title);
		this.fetchObjectId().setValue(id);
	}

// Overrides
	@Override
	public boolean equals(Object o)
	{
		boolean result=false;
		if (o instanceof AssistantMessage)
		{
			if (!this.fetchObjectId().isNullOrEmpty())
			{
				AssistantMessage that=(AssistantMessage) o;
				result=(Objects.equals(this.fetchObjectId().getValue(), that.fetchObjectId().getValue()))
				       && (Objects.equals(this.fetchWorkerClass().getValue(), that.fetchWorkerClass().getValue()));
			}
		}
		return result;
	}

	public KeyData<String> fetchObjectId()
	{
		if(null==this.objectId){
			this.objectId = new KeyData<>(this, "objectId", String.class, false, null);
		}
		return this.objectId;
	}

	public KeyData<Class> fetchWorkerClass()
	{
		if(null==this.workerClass){
			// Expects Class<? extends BaseAssistantWorker> could write a handler to enforce class type
			this.workerClass = new KeyData<>(this, "workerClass", Class.class, false, null);
		}
		return this.workerClass;
	}

	@Override
	public JsonData toJson(boolean storing, String... languages)
	{
		JsonData result=super.toJson(storing, languages);
		try
		{
			result.update("message", this.fetchMessage().getValue());
			result.update("started", (null!=this.getStopwatch()) ? this.getStopwatch().getStartedWhen() : null);
			result.update("stopped", (null!=this.getStopwatch()) ? this.getStopwatch().getStoppedWhen() : null);
			result.update("took", (null!=this.getStopwatch()) ? TimeX.fromMillis(this.getStopwatch().elapsed().getMillis()) : null);
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning(ex);
		}
		return result;
	}

	public KeyData<String> fetchMessage()
	{
		if(null==this.message){
			this.message = new KeyData<>(this, "message", String.class, false, null);
		}
		return this.message;
	}

	public KeyData<AssistantMessage.Status> fetchStatus()
	{
		if(null==this.status){
			this.status = new KeyData<>(this, "status", AssistantMessage.Status.class, false, AssistantMessage.Status.waiting, new KeyDataHandlerSaveAfterSet());
		}
		return this.status;
	}

	public Stopwatch getStopwatch()
	{
		return this.stopwatch;
	}

	public void start()
	{
		this.stopwatch=new Stopwatch();
		this.stopwatch.start();
	}

	public void stop()
	{
		if ((null!=this.stopwatch) && this.stopwatch.isStarted())
		{
			this.stopwatch.stop();
		}
	}

	public boolean isProcessed()
	{
		return (Objects.equals(this.fetchStatus().getValue(), AssistantMessage.Status.processed));
	}
}
