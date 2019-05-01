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

package com.lusidity.system.security.cbac;

import com.lusidity.data.DataVertex;
import com.lusidity.system.security.UserCredentials;

import java.util.concurrent.Callable;

public class PolicyDecisionPointTask implements Callable<DataVertex>
{
	private final DataVertex vertex;
	private final UserCredentials credentials;

	// Constructors
	public PolicyDecisionPointTask(DataVertex vertex, UserCredentials credentials)
	{
		super();
		this.vertex = vertex;
		this.credentials = credentials;
	}

	// Overrides
	@Override
	public DataVertex call()
		throws Exception
	{
		DataVertex result = this.vertex;
		if (this.vertex.enforcePolicy() && !PolicyDecisionPoint.isInScope(this.vertex, this.credentials))
		{
			result=null;
		}
		return result;
	}
}
