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

package com.lusidity.system.security;

import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import org.restlet.Request;

@AtSchemaClass(name="Client Info", discoverable=false, description="Contains as much information about a client that has connected to this system.")
public class ClientInfo extends BaseDomain
{
	private String address=null;
	private String agentName=null;
	private String agent=null;
	private String from=null;
	private int port=0;

// Constructors
	public ClientInfo()
	{
		super();
	}

	public ClientInfo(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Methods
	public static ClientInfo create(Request request)
	{
		ClientInfo result=new ClientInfo();

		org.restlet.data.ClientInfo info=request.getClientInfo();
		result.setAddress(info.getAddress());
		result.setAgentName(info.getAgentName());
		result.setAgent(info.getAgent());
		result.setFrom(info.getFrom());
		result.setPort(info.getPort());

		return result;
	}

// Getters and setters
	public String getAddress()
	{
		return this.address;
	}

	public void setAddress(String address)
	{
		this.address=address;
	}

	public String getAgentName()
	{
		return this.agentName;
	}

	public void setAgentName(String agentName)
	{
		this.agentName=agentName;
	}

	public String getAgent()
	{
		return this.agent;
	}

	public void setAgent(String agent)
	{
		this.agent=agent;
	}

	public String getFrom()
	{
		return this.from;
	}

	public void setFrom(String from)
	{
		this.from=from;
	}

	public int getPort()
	{
		return this.port;
	}

	public void setPort(int port)
	{
		this.port=port;
	}
}
