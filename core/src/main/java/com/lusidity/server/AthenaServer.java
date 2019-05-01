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

package com.lusidity.server;

import com.lusidity.ClientConfiguration;
import com.lusidity.Environment;
import com.lusidity.SoteriumResponse;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class AthenaServer
{
	private static Collection<AthenaServer> servers=new ArrayList<>();
	private final String title;
	private final URI host;
	private final String relativePath;
	private boolean available=false;

	// Constructors
	public AthenaServer(String title, URI host, String relativePath, boolean available){
		super();
		this.title = title;
		this.host = host;
		this.relativePath = relativePath;
		this.available = available;
	}

	// Overrides
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj instanceof AthenaServer){
			AthenaServer other = (AthenaServer)obj;
			result =(null!=this.getHost()) && (null!=other.getHost())
			        && StringX.equalsIgnoreCase(this.getHost().toString(), other.getHost().toString());
		}
		return result;
	}

	public URI getHost()
	{
		return this.host;
	}

	// Methods
	public static Collection<AthenaServer> getServers()
	{
		return AthenaServer.servers;
	}

	public boolean isOnline(ClientConfiguration config)
	{
		String baseServerUrl = String.format("https://%s", this.getHost());
		SoteriumResponse response = null;
		try{
			response = SoteriumResponse.get(config, baseServerUrl, this.getRelativePath());
			this.available = ((null!=response) && response.getData().hasKey("ping"));
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}

		return this.available;
	}

	public String getRelativePath()
	{
		return this.relativePath;
	}

	public JsonData toJson()
	{
		JsonData result = JsonData.createObject();
		result.put("title", this.getTitle());
		result.put("online", this.isAvailable());
		return result;
	}

	public String getTitle()
	{
		return this.title;
	}

	public boolean isAvailable()
	{
		return this.available;
	}

	public void setAvailable(boolean available)
	{
		this.available=available;
	}

	// Getters and setters
	public boolean isValid()
	{
		return !(StringX.isBlank(this.getTitle()) && (null!=this.getHost()) && StringX.isBlank(this.getRelativePath()));
	}
}
