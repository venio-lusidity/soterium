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

package com.lusidity.domains.system.event;

import com.lusidity.Environment;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDatServerEventClassHandler;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="Server Message", discoverable = false, description = "A message used to inform other servers that this server did something or its time to do something.")
public class ServerEvent extends BaseDomain
{
	public enum Status{
		processing,
		completed,
		failed,
		waiting,
		none
	}

	private KeyData<String> serverName = null;
	private KeyData<ServerEvent.Status> status = null;
	private KeyData<Class> handler= null;

	// Constructors
	public ServerEvent(){
		super();

	}

	public ServerEvent(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	// Methods
	public static UriValue getUriValue()
	{
		return new UriValue(String.format("/%s", Environment.getInstance().getConfig().getServerName()));
	}

	public KeyData<ServerEvent.Status> fetchStatus(){
		if(null==this.status){
			this.status = new KeyData<>(this, "status", ServerEvent.Status.class, false, ServerEvent.Status.none);
		}
		return this.status;
	}

	public KeyData<Class> fetchHandler(){
		if (null==this.handler)
		{
			this.handler=new KeyData<>(this, "handler", Class.class, false, new KeyDatServerEventClassHandler());
		}
		return this.handler;
	}

	public KeyData<String> fetchServerName(){
		if(null==this.serverName){
			this.serverName = new KeyData<>(this, "serverName", String.class, false, null);
		}
		return this.serverName;
	}

}
