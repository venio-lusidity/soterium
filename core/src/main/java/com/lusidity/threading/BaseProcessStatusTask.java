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

package com.lusidity.threading;

import com.lusidity.domains.data.ProcessStatus;

import java.util.concurrent.Callable;

public abstract class BaseProcessStatusTask implements Callable<ProcessStatus>
{
	private final ProcessStatus processStatus;

// Constructors
	public BaseProcessStatusTask(ProcessStatus processStatus)
	{
		super();
		this.processStatus=processStatus;
	}

// Getters and setters
	public synchronized ProcessStatus getProcessStatus()
	{
		return this.processStatus;
	}
}
