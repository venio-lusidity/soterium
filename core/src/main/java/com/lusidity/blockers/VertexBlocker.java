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

import com.lusidity.data.DataVertex;

import java.util.ArrayList;
import java.util.Collection;

public class VertexBlocker
{

	private final Collection<String> queue=new ArrayList<>();

	// Constructors
	public VertexBlocker()
	{
		super();
	}

	public void start(DataVertex vertex)
	{
		if ((null!=vertex) && (null!=vertex.fetchId().getValue()))
		{
			this.blockAndQueue(vertex);
		}
	}

	/**
	 * Add the specified object ID to the transaction queue, blocking as needed. This is a protected, atomic operation.
	 *
	 * @param vertex DataVertex
	 *               Object ID to enqueue.
	 */
	private void blockAndQueue(DataVertex vertex)
	{
		boolean acquired=false;

		while (!acquired)
		{
			acquired=this.addToQueue(vertex.fetchId().getValue());
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
	 * Remove an object ID from the transaction queue.
	 *
	 * @param vertex A DataVertex
	 *               element.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void finished(DataVertex vertex)
	{
		if ((null!=vertex) && vertex.fetchId().isNotNullOrEmpty())
		{
			this.removeFromQueue(vertex.fetchId().getValue());
		}
	}

	private synchronized void removeFromQueue(String objectId)
	{
		this.getQueue().remove(objectId);
	}

	/**
	 * Is the specified DataVertex currently in the transaction queue?
	 *
	 * @param vertex DataVertex
	 *               the objectId.
	 * @return true if the specified object ID is currently in the transaction queue.
	 */
	@SuppressWarnings("unused")
	public boolean isQueued(DataVertex vertex)
	{
		//noinspection SuspiciousMethodCalls
		return this.getQueue().contains(vertex.fetchId().getValue());
	}
}
