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

package com.lusidity.apollo.elasticSearch.tasks;

import com.lusidity.data.DataVertex;
import com.lusidity.framework.json.JsonData;

public class ResponseHandler
{
	private final DataVertex vertex;
	private final JsonData response;
	private Boolean ok=null;

	// Constructors
	public ResponseHandler(JsonData response, DataVertex vertex)
	{
		super();
		this.response=response;
		this.vertex=vertex;
	}

	// Getters and setters
	public boolean isOk()
	{
		if ((null==this.ok) && (null!=this.response))
		{
			this.ok=this.response.getBoolean("http_ok");
		}
		return this.ok;
	}

	public JsonData getResponse()
	{
		return this.response;
	}

	public DataVertex getVertex()
	{
		return this.vertex;
	}
}
