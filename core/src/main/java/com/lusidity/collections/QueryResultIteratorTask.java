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

import com.lusidity.Environment;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.domains.data.ProcessStatus;

import java.util.concurrent.Callable;

public class QueryResultIteratorTask implements Callable<Boolean>
{
	private final ProcessStatus processStatus;
	private final IQueryResult queryResult;
	private final int on;
	private final int hits;
	private final int start;
	private final int limit;
	private final IQueryResultHandler handler;

	// Constructors
	@SuppressWarnings("ConstructorWithTooManyParameters")
	public QueryResultIteratorTask(IQueryResultHandler handler, IQueryResult queryResult, ProcessStatus processStatus, int on, int hits, int start, int limit)
	{
		super();
		this.handler = handler;
		this.queryResult = queryResult;
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
		boolean result = false;
		try{
			result = this.handler.handle(this.queryResult, this.processStatus, this.on, this.hits, this.start, this.limit);
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}
}
