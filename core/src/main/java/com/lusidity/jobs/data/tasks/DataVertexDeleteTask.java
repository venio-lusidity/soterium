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

package com.lusidity.jobs.data.tasks;

import com.lusidity.data.DataVertex;
import com.lusidity.domains.data.ProcessStatus;

import java.util.concurrent.Callable;

public class DataVertexDeleteTask implements Callable<Boolean>
{
	private final DataVertex vertex;
	private final ProcessStatus processStatus;
	private final boolean delete;

	// Constructors
	public DataVertexDeleteTask(DataVertex vertex, ProcessStatus processStatus, boolean delete)
	{
		super();
		this.vertex = vertex;
		this.processStatus = processStatus;
		this.delete = delete;
	}

	// Overrides
	@Override
	public Boolean call()
		throws Exception
	{
		try{
			if(this.delete)
			{
				boolean deleted=this.vertex.delete();
				if (deleted)
				{
					this.processStatus.fetchDeleted().getValue().increment();
				}
				else
				{
					this.processStatus.fetchErrors().getValue().increment();
				}
			}
			else{
				this.processStatus.setMessage(String.format("Deprecating %s", this.vertex.fetchId().getValue()));
				this.vertex.fetchDeprecated().setValue(true);
				this.vertex.save();
			}
		}
		catch (Exception ignored){}
		this.processStatus.fetchProcessed().getValue().increment();
		return true;
	}
}
