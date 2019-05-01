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

package com.lusidity.blockers;

import com.lusidity.framework.text.StringX;

import java.util.ArrayList;
import java.util.Collection;

public class StringBlocker
{

	private final Collection<String> queue=new ArrayList<>();

	// Constructors
	public StringBlocker()
	{
		super();
	}

	public void start(String value)
	{
		if (!StringX.isBlank(value))
		{
			this.blockAndQueue(value);
		}
	}

	/**
	 * Add the specified string value to the transaction queue, blocking as needed. This is a protected, atomic operation.
	 *
	 * @param value String value to block
	 */
	private void blockAndQueue(String value)
	{
		boolean acquired=false;

		while (!acquired)
		{
			acquired=this.addToQueue(value);
		}
	}

	private synchronized boolean addToQueue(String objectId)
	{
		boolean result=!this.getQueue().contains(objectId);
		if (result)
		{
			this.getQueue().add(objectId);
		}
		return result;
	}

	public Collection<String> getQueue()
	{
		return this.queue;
	}

	/**
	 * Remove a string from the transaction queue.
	 *
	 * @param value String value to release from being blocked
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void finished(String value)
	{
		if (!StringX.isBlank(value))
		{
			this.removeFromQueue(value);
		}
	}

	private synchronized void removeFromQueue(String value)
	{
		this.getQueue().remove(value);
	}

	/**
	 * Is the specified string currently in the transaction queue?
	 *
	 * @param value  String being blocked.
	 * @return true if the specified string is currently in the transaction queue.
	 */
	@SuppressWarnings("unused")
	public boolean isQueued(String value)
	{
		//noinspection SuspiciousMethodCalls
		return this.getQueue().contains(value);
	}
}
