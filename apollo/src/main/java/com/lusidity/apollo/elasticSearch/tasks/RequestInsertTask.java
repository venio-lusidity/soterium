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


import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;

import java.net.URL;
import java.util.concurrent.Callable;

public class RequestInsertTask implements Callable<ResponseHandler>
{
	private final URL url;
	private final HttpClientX.Methods methods;
	private final String referrer;
	private final String[] headers;
	private final DataVertex vertex;

	// Constructors
	public RequestInsertTask(DataVertex vertex, URL url, String... headers)
	{
		super();
		this.url=url;
		this.methods=HttpClientX.Methods.POST;
		this.referrer=Environment.getInstance().getConfig().getReferer();
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.headers=headers;
		this.vertex=vertex;
	}

	// Overrides
	@Override
	public ResponseHandler call()
		throws Exception
	{
		String content=this.vertex.toJson(true).toString();
		JsonData response=HttpClientX.getResponse(content, this.url, this.methods, this.referrer, this.headers);
		return new ResponseHandler(response, this.vertex);
	}
}
