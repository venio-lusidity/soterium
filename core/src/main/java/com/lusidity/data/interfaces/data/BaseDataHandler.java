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

package com.lusidity.data.interfaces.data;


import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.StatusCode;
import com.lusidity.framework.json.JsonData;

public abstract class BaseDataHandler
{

	private String property=null;
	private JsonData request=null;
	private JsonData response=JsonData.createObject();
	private IDataStore.Operations operation=null;

// Constructors
	public BaseDataHandler(JsonData request, IDataStore.ObjectTypes objectType)
	{
		super();
		this.request=request;

		this.load();
		this.property=objectType.toString();
	}

	protected void load()
	{
		JsonData result=JsonData.createObject();
		if (null==this.getOperation())
		{
			this.operation=IDataStore.Operations.error;
			this.response=StatusCode.getInfo(StatusCode.EXIT_CODE.BadRequest);
			this.response.put("operation", IDataStore.Operations.error);
			this.response.put("error", "The operation property is missing.");
		}
	}

	public IDataStore.Operations getOperation()
	{
		if (null==this.operation)
		{
			this.operation=this.request.getEnum(IDataStore.Operations.class, "operation");
		}
		return this.operation;
	}

	public void doWork()
	{
		this.load();
		if (this.getOperation()!=IDataStore.Operations.error)
		{
			boolean processed=false;

			try
			{
				this.validate();
				switch (this.getOperation())
				{
					case create:
						this.create();
						processed=true;
						break;
					case update:
						this.update();
						processed=true;
						break;
					case delete:
						this.delete();
						processed=true;
						break;
				}
			}
			catch (Exception ex)
			{
				this.setResponse(StatusCode.getInfo(StatusCode.EXIT_CODE.InternalServerError));
				this.getResponse().put("operation", IDataStore.Operations.error);
				this.getResponse().put("error", ex.toString());
				processed=true;
			}

			if (!processed)
			{
				this.setResponse(StatusCode.getInfo(StatusCode.EXIT_CODE.BadRequest));
				if (this.getResponse().hasValue("operation"))
				{
					this.getResponse().remove("operation");
				}
				this.getResponse().put("operation", IDataStore.Operations.error);
				this.getResponse().put("error", "Invalid operation, expects create, update or delete.");
			}
		}
	}

	private void validate()
		throws ApplicationException
	{
		if ((null==this.getRequest()) || !this.getRequest().isValid())
		{
			throw new ApplicationException("The data received is either invalid or corrupted.");
		}

		if (!this.getRequest().hasValue(this.getProperty()))
		{
			throw new ApplicationException("The %s does not have any JsonData to process.", this.getClass().getSimpleName());
		}
	}

	public abstract void create()
		throws ApplicationException;

	public abstract void update()
		throws ApplicationException;

	public abstract void delete()
		throws ApplicationException;

	public JsonData getResponse()
	{
		return this.response;
	}

	public JsonData getRequest()
	{
		return this.request;
	}

	public String getProperty()
	{
		return this.property;
	}

	protected void setResponse(JsonData response)
	{
		this.response=response;
	}

	public abstract void create(Object object);

	public abstract boolean delete(Object object)
		throws ApplicationException;

// Getters and setters
	public JsonData getRequestItems()
	{
		JsonData working=this.getRequest().getFromPath(this.getProperty());
		if (!working.isJSONArray())
		{
			JsonData temp=this.getRequest().getFromPath(this.getProperty());
			working=JsonData.createArray();
			working.put(temp);
		}
		return working;
	}
}
