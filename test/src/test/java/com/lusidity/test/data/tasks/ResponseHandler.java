package com.lusidity.test.data.tasks;

import com.lusidity.data.DataVertex;
import com.lusidity.framework.json.JsonData;

public class ResponseHandler
{
	private final DataVertex vertex;
	private Boolean ok = null;
	private final JsonData response;

	public ResponseHandler(JsonData response, DataVertex vertex){
		super();
		this.response = response;
		this.vertex = vertex;
	}

	public boolean isOk()
	{
		if((null==this.ok) && (null!=this.response)){
			this.ok = this.response.getBoolean("http_ok");
		}
		return this.ok;
	}

	public JsonData getResponse(){
		return this.response;
	}

	public DataVertex getVertex()
	{
		return this.vertex;
	}
}
