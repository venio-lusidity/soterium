
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

package com.lusidity.configuration;

import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;

import java.io.File;

@SuppressWarnings({
	"UnusedDeclaration",
	"ClassWithTooManyFields"
})
public class SplunkLoggingConfiguration extends BaseSoteriumConfiguration
{
	private static SplunkLoggingConfiguration instance=null;

	// Constructors
	public SplunkLoggingConfiguration(File file)
	{
		super(file);
		this.load();
		SplunkLoggingConfiguration.instance=this;
	}
	public static SplunkLoggingConfiguration getInstance(){
		return SplunkLoggingConfiguration.instance;
	}

	// Methods
// Getters and setters
	public String getUsername()
	{
		return this.getData().getString("username", "value");
	}

	public int getBatchSize()
	{
		return this.getData().getInteger("batch_size","value");
	}

	public int getInterval()
	{
		return this.getData().getInteger("interval","value"	);
	}

	public String getToken()
	{
		return this.getData().getString("token", "value");
	}

	public String getPostEndpoint()
	{
		return this.getData().getString("post_endpoint", "value");
	}

	public String getHost()
	{
		return this.getData().getString("host", "value");
	}

	public Boolean getDelete()
	{
		return this.getData().getBoolean("delete_es", "value");
	}

	public int getPort()
	{
		return this.getData().getInteger("port", "value");
	}

	public HttpClientX.Protocol getProtocol()
	{
		String value = this.getData().getString("protocol", "value");
		return StringX.equalsIgnoreCase(value, "http")? HttpClientX.Protocol.HTTP : HttpClientX.Protocol.HTTPS;
	}

	public String getOwner()
	{
		return this.getData().getString("owner", "value");
	}

	public String getSourcetype()
	{
		return this.getData().getString("sourcetype", "value");
	}

	public DateTime getLastPush(){
		return this.getData().getDateTime("last_push");
	}

	public String getIndex()
	{
		return this.getData().getString("index", "value");
	}

	public void setLastPush(DateTime pushed){
		this.getData().update("last_push", pushed);
		this.getData().save(this.getFile(), false);
	}
	public Boolean getDebug()
	{
		return this.getData().getBoolean("debug", "value");
	}
}
