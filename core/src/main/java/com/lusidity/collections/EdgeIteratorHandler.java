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
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.domains.data.ProcessStatus;

import java.util.ArrayList;
import java.util.List;

public class EdgeIteratorHandler implements IQueryResultHandler
{
	private final List<DataVertex> results = new ArrayList<>();
	private final String id;

	public EdgeIteratorHandler(String id)
	{
		super();
		this.id = id;
	}

	@Override
	public boolean handle(IQueryResult queryResult, ProcessStatus processStatus, int on, int hits, int start, int limit)
	{
		DataVertex item = queryResult.getOtherEnd(this.id);
		if(null!=item){
			this.getResults().add(item);
		}
		return false;
	}

	public List<DataVertex> getResults(){
		return this.results;
	}
}
