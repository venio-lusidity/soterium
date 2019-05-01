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

import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.domains.data.ProcessStatus;

public interface IQueryResultHandler
{
	/**
	 * Process a vertex;
	 * @param queryResult The current IQueryResult.
	 * @param processStatus A ProcessStatus
	 * @param on The vertex on starting at 0.
	 * @param hits How many vertices found.
	 * @param start Used for paging.
	 * @param limit Max number of vertices to process.
	 * @return True to cancel iteration.
	 */
	boolean handle(IQueryResult queryResult, ProcessStatus processStatus, int on, int hits, int start, int limit);
}
