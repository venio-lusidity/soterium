

package com.lusidity.test.data.tasks;


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
	public RequestInsertTask(DataVertex vertex, URL url, HttpClientX.Methods methods, String referrer, String... headers)
	{
		super();
		this.url = url;
		this.methods = methods;
		this.referrer = referrer;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.headers = headers;
		this.vertex = vertex;
	}

	// Overrides
	@Override
	public ResponseHandler call()
		throws Exception
	{
		String content = this.vertex.toJson(true).toString();
		JsonData response = HttpClientX.getResponse(content, this.url, this.methods, this.referrer, this.headers);
		return new ResponseHandler(response, this.vertex);
	}
}
