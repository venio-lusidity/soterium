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

package com.lusidity.framework.json;

import com.lusidity.framework.system.LineHandler;

public class JsonDataLineHandler implements LineHandler
{
	public enum HandlerTypes{
		count,
		process
	}

	private final JsonDataLineHandler.HandlerTypes handlerType;
	private final IJsonDataCallBack caller;
	private int linesRead=0;
	private JsonData firstItem = null;

	// Constructors
	public JsonDataLineHandler(IJsonDataCallBack caller, JsonDataLineHandler.HandlerTypes handlerType){
		super();
		this.caller = caller;
		this.handlerType = handlerType;
	}

	// Overrides
	@Override
	public synchronized void incrementLinesRead()
	{
		// the callback handler depends on the line count so it must be counted in the increment()
	}

	@Override
	public synchronized int getLinesRead()
	{
		return this.linesRead;
	}

	@Override
	public Object getValue()
	{
		return null;
	}

	@Override
	public boolean handle(String line)
	{
		JsonData item = JsonData.create(line);
		this.increment();
		if(this.getLinesRead()==1){
			this.firstItem = item;
		}
		return this.caller.jdCallBack(this, item);
	}

	private synchronized void increment()
	{
		this.linesRead++;
	}

	@Override
	public boolean handle(String[] values)
	{
		return false;
	}

	// Getters and setters
	public JsonDataLineHandler.HandlerTypes getHandlerType()
	{
		return this.handlerType;
	}

	public JsonData getFirstItem()
	{
		return this.firstItem;
	}
}
