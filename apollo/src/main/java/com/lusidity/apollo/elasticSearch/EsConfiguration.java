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

package com.lusidity.apollo.elasticSearch;

import com.lusidity.Environment;
import com.lusidity.data.interfaces.BaseServerConfiguration;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.restlet.data.Protocol;

import java.io.File;


@SuppressWarnings({
	"RedundantFieldInitialization",
	"unused"
})
public class EsConfiguration extends BaseServerConfiguration
{
	private static EsConfiguration INSTANCE=null;
	private Boolean immediatelyAvailable=null;
	private Boolean clientEnabled=null;
	private Boolean transactionCheckEnabled=null;

	// Constructors
	public EsConfiguration()
	{
		super();
		EsConfiguration.INSTANCE=this;
	}

	public EsConfiguration(JsonData jsonData)
	{
		super(jsonData);
		EsConfiguration.INSTANCE=this;
	}

	// Overrides
	@Override
	public void close()
	{

	}

	@Override
	public boolean transform(JsonData item)
	{
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return this.isOpened();
	}

	public boolean clientEnabled()
	{
		if (null==this.clientEnabled)
		{
			this.clientEnabled=this.getConfigData().getBoolean("clientEnabled");
		}
		return this.clientEnabled;
	}

	public int getThreadPoolSize(int defaultSize)
	{
		int result=this.getConfigData().getInteger(defaultSize, "threadPoolSize");
		if (result<=0)
		{
			result=defaultSize;
		}
		if (result<=0)
		{
			result=10;
		}
		return result;
	}

	public String getCommand(String command)
	{
		return this.getConfigData().getString("search_guard", "commands", command);
	}

	public boolean initializeSSl()
	{
		return this.getConfigData().getBoolean(false, "initializeSSl");
	}

	// Getters and setters
	protected static EsConfiguration getInstance()
	{
		return EsConfiguration.INSTANCE;
	}

	@SuppressWarnings("UnusedDeclaration")
	public String getBackendName()
	{
		return "elastic search";
	}

	public JsonData getClientHosts()
	{
		return this.getConfigData().getFromPath("clientHosts");
	}

	public int getClientPort()
	{
		return this.getConfigData().getIpPort(9300, "clientPort");
	}

	public String getClusterName()
	{
		return this.getConfigData().getString("clusterName");
	}

	public String getHttpHost()
	{
		return this.getConfigData().getString("httpHost");
	}

	public String getProtocolString()
	{
		return this.getConfigData().getString("protocol");
	}

	public int getHttpPort()
	{
		return this.getConfigData().getIpPort(9200, "httpPort");
	}

	public String getLocalHostAddress()
	{
		return this.getConfigData().getString("localHostAddress");
	}

	public Integer getThreadsProcessed()
	{
		return this.getConfigData().getInteger(20, "threadsProcessed");
	}

	public String getClientPortRange()
	{
		String result=this.getConfigData().getString("clientPortRange");
		if (StringX.isBlank(result))
		{
			result="9300-9400";
		}
		return result;
	}

	public File getLogConfigDir()
	{
		return BaseServerConfiguration.getFile(this.getConfigData().getString("log_config_dir"));
	}

	public File getLogConfFile()
	{
		return BaseServerConfiguration.getFile(this.getConfigData().getString("log_config_file"));
	}

	public File getLogDir()
	{
		return BaseServerConfiguration.getFile(this.getConfigData().getString("log_dir"));
	}

	public boolean isImmediatelyAvailable()
	{
		if (null==this.immediatelyAvailable)
		{
			this.immediatelyAvailable=this.getConfigData().getBoolean("immediatelyAvailable");
		}
		return this.immediatelyAvailable;
	}

	public boolean isDeadLockEnabled()
	{
		return this.getConfigData().getBoolean("dead_lock_enabled");
	}

	public boolean isHttps()
	{
		return (null!=Environment.getInstance().getKeyStoreManagerApollo()) && (null!=this.getProtocol()) && this.getProtocol().equals(Protocol.HTTPS);
	}

	public String getVersion()
	{
		return this.getData().getString("version");
	}

	public boolean isTransactionCheckEnabled()
	{
		if (null==this.transactionCheckEnabled)
		{
			this.transactionCheckEnabled=this.getConfigData().getBoolean("transactionCheckEnabled");
			if (null==this.transactionCheckEnabled)
			{
				this.transactionCheckEnabled=false;
			}
		}
		return this.transactionCheckEnabled;
	}
}
