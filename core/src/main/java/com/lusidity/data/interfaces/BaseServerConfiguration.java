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

package com.lusidity.data.interfaces;

import com.lusidity.Environment;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.restlet.data.Protocol;

import java.io.File;
import java.io.IOException;

public abstract class BaseServerConfiguration implements IStoreConfiguration
{

	private static final long DEFAULT_INTERVAL=60000L;
	private JsonData data=null;
	private JsonData configData=null;
	private Boolean disabled=false;
	private boolean opened=false;
	private String configFilePath=null;
	private Long lastModified=null;
	private DateTime lastChecked=DateTime.now();
	private Long interval=null;

	// Constructors
	public BaseServerConfiguration(JsonData jsonData)
	{
		super();
		this.data=jsonData;
	}

	public BaseServerConfiguration()
	{
		super();
	}

	// Getters and setters
	public boolean isBatchMode()
	{
		return Environment.getInstance().isBatchMode();
	}

	public String getConfigFilePath()
	{
		return this.configFilePath;
	}

	@SuppressWarnings("unused")
	public Long getLastModified()
	{
		return this.lastModified;
	}

	@Override
	public void open()
		throws ApplicationException, IOException
	{
		if (null!=this.data)
		{
			if (this.data.has("disabled", "value"))
			{
				this.disabled=this.data.getBoolean("disabled", "value");
			}
			else
			{
				this.disabled=this.data.getBoolean("disabled");
			}

			if (this.data.has("configFilePath", "value"))
			{
				this.configFilePath=this.data.getString("configFilePath", "value");
			}
			else
			{
				this.configFilePath=this.data.getString("configFilePath");
			}
			if (!StringX.isBlank(this.configFilePath))
			{
				File file=BaseServerConfiguration.getFile(this.configFilePath);
				if (file.exists())
				{
					this.lastChecked=new DateTime(file.lastModified());
					this.configData=new JsonData(file);
					if(this.configData.isValid())
					{
						Environment.getInstance().getReportHandler().fine("Configuration loaded for %s at %s.", this.getClass().getSimpleName(), file.getAbsolutePath());
					}
					else{
						throw new ApplicationException("The configuration file is corrupted, %s.", (null!=file) ? file.getAbsolutePath() : "unknown file");
					}
				}
				else
				{
					Environment.getInstance().getReportHandler().severe("The configuration file is missing, %s.", (null!=file) ? file.getAbsolutePath() : "unknown file");
					Environment.getInstance().close();
				}
			}
			else
			{
				Environment.getInstance().getReportHandler().severe("The configuration file is missing");
				Environment.getInstance().close();
			}

			if (null==this.configData)
			{
				this.configData=this.data;
			}

			if((null==this.configData) || !this.configData.isValid()){
				Environment.getInstance().getReportHandler().severe("The configuration file is corrupted");
				Environment.getInstance().close();
			}

			if (!this.disabled)
			{
				if (this.data.isJSONArray())
				{
					for (Object o : this.data)
					{
						if (o instanceof JSONObject)
						{
							try
							{
								JsonData item=new JsonData(o);
								this.transform(item);
							}
							catch (Exception ex)
							{
								Environment.getInstance().getReportHandler().severe(ex);
							}

						}
					}
				}
				else
				{
					try
					{
						this.transform(this.data);
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().severe(ex);
					}
				}
				this.setOpened(true);
			}
			Environment.getInstance().registerCloseable(this);

			this.opened=true;
			Environment.getInstance().getReportHandler().info("The configuration for %s has been loaded.", this.getClass().getName());
		}
	}

	public void setLastModified(Long lastModified)
	{
		this.lastModified=lastModified;
	}

	public Protocol getProtocol()
	{
		Protocol result=null;
		if (this.configData.has("protocol", "value"))
		{
			result=this.configData.getProtocol("protocol", "value");
		}
		else
		{
			result=this.configData.getProtocol("protocol");
		}
		return result;
	}

	public static File getFile(String path)
	{
		File result;
		if ((null!=Environment.getInstance()) && StringX.startsWithIgnoreCase(path, "/resource", "resource"))
		{
			result=new File(Environment.getInstance().getConfig().getResourcePath(), StringX.stripStart(path, "/", "resource"));
		}
		else if ((null!=Environment.getInstance()) && StringX.startsWithIgnoreCase(path, "/config"))
		{
			result=new File(Environment.getInstance().getConfig().getResourcePath(), StringX.stripStart(path, "/"));
		}
		else if ((null!=Environment.getInstance()) && StringX.startsWithIgnoreCase(path, "/"))
		{
			result=new File(path);
		}
		else
		{
			result=new File(Environment.getInstance().getConfig().getResourcePath(), path);
		}
		if (!result.exists())
		{
			File test=new File("../", path);
			if (test.exists())
			{
				result=test;
			}
		}
		if (!result.exists() && StringX.contains(result.getAbsolutePath(), "/test/resource"))
		{
			String p=StringX.replace(result.getAbsolutePath(), "/test/resource", "/resource");
			result=new File(p);
		}

		return result;
	}

	public String getHost()
	{
		String result=null;
		if (this.configData.has("protocol", "value"))
		{
			result=this.configData.getString("host", "value");
		}
		else
		{
			result=this.configData.getString("host");
		}
		return result;
	}

	public JsonData getData()
	{
		this.checkAndReload();
		return this.data;
	}

	@Override
	public boolean isOpened()
	{
		return !this.disabled && this.opened;
	}

	@Override
	public JsonData getConfigData()
	{
		this.checkAndReload();
		return this.configData;
	}

	@Override
	public boolean hasFileBeenUpdated()
	{
		boolean result=(null==this.lastModified);
		File file=new File(this.configFilePath);
		if (result)
		{
			result=false;
			this.lastModified=file.lastModified();
		}
		else if (file.exists())
		{
			long n=file.lastModified();
			result=n>this.lastModified;
			if (result)
			{
				this.lastModified=n;
			}
		}
		return result;
	}

	@Override
	public synchronized void checkAndReload()
	{
		try
		{
			DateTime dt=DateTime.now();
			Long dif=dt.getMillis()-this.lastChecked.getMillis();
			if (dif>=this.getInterval())
			{
				if (this.hasFileBeenUpdated())
				{
					this.reload();
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	public void reload()
	{
		try
		{
			this.opened=false;
			this.configFilePath=null;
			this.lastModified=null;
			this.lastChecked=DateTime.now();
			this.interval=null;
			this.open();
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	@Override
	public long getInterval()
	{
		if (null==this.interval)
		{
			if (this.configData.has("interval", "value"))
			{
				this.interval=this.configData.getLong("interval", "value");
			}
			else
			{
				this.interval=this.configData.getLong("interval");
			}
		}
		if ((null==this.interval) || (this.interval<-0))
		{
			this.interval=BaseServerConfiguration.DEFAULT_INTERVAL;
		}
		return this.interval;
	}

	protected void setOpened(boolean opened)
	{
		this.opened=opened;
	}
}
