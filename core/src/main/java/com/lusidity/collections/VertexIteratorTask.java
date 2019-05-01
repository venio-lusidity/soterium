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

package com.lusidity.collections;

import com.lusidity.data.DataVertex;
import com.lusidity.domains.data.ProcessStatus;

import java.util.concurrent.Callable;

public class VertexIteratorTask implements Callable<Boolean>
{
	private final ProcessStatus processStatus;
	private final DataVertex vertex;
	private final int on;
	private final int hits;
	private final int start;
	private final int limit;
	private final IVertexHandler vertexHandler;

	// Constructors
	@SuppressWarnings("ConstructorWithTooManyParameters")
	public VertexIteratorTask(IVertexHandler vertexHandler, DataVertex vertex, ProcessStatus processStatus, int on, int hits, int start, int limit)
	{
		super();
		this.vertexHandler = vertexHandler;
		this.vertex = vertex;
		this.processStatus = processStatus;
		this.on = on;
		this.hits = hits;
		this.start = start;
		this.limit = limit;
	}

	// Overrides
	@Override
	public Boolean call()
		throws Exception
	{
		return this.vertexHandler.handle(this.vertex, this.processStatus, this.on, this.hits, this.start, this.limit);
	}
}
