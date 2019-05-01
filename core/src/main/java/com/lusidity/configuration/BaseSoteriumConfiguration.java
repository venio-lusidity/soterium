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

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.BaseServerConfiguration;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.ContactDetailHelper;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.regex.RegExHelper;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import com.lusidity.security.IdentityHelper;
import com.lusidity.server.IServer;
import org.json.JSONObject;
import org.restlet.data.Method;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;

public abstract class BaseSoteriumConfiguration implements IEnvironmentConfiguration
{
	private static boolean initialized = false;

	private static final Integer DEFAULT_OFF_RETRIES=120;
	private static final Long DEFAULT_OFF_WAIT_INTERVAL=500L;
	protected JsonData data=null;
	private final Collection<ServerEntry> serverEntries=new ArrayList<>();
	private Long lastChecked=0L;
	private File file=null;
	private JsonData permissions=null;
	private JsonData settings=null;
	private JsonData groups=null;
	private JsonData rules=null;
	private JsonData users=null;
	private JsonData defaultGroups=null;
	private boolean clearStores=false;
	private boolean serverMode=false;
	private Long offlineWaitInterval=null;
	private Integer offlineRetries=null;

	// Constructors
	public BaseSoteriumConfiguration(File file)
	{
		super();
		this.file=file;
	}

	protected void load()
	{
		try
		{
			ReportHandler.getInstance().debug("Checking to see if file exists.");
			if ((null!=this.file) && this.file.exists())
			{
				ReportHandler.getInstance().info("Loading the file as a JsonData object. %s.", this.getClass().getName());
				this.data=new JsonData(this.file);
				ReportHandler.getInstance().debug("Get last date JSON file was modified.");
				this.lastChecked=this.file.lastModified();
				ReportHandler.getInstance().debug("Validating the JsonData loaded from the JSON file.");
				if (this.data.isValid())
				{
					this.transform();
				}
				else{
					ReportHandler.getInstance().warning("The config file is not properly fomatted for %s.", this.getClass().getName());
				}
			}
			else
			{
				ReportHandler.getInstance().debug("FileInfo does not exist.");
				ReportHandler.getInstance().warning("The config file does not exist for %s.", this.getClass().getName());
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().debug("About to throw an error.");
			ReportHandler.getInstance().severe("Failed to load %s. %s", this.getClass().getName(), ex.getMessage());
		}
	}

	protected void transform()
	{
		if(!BaseSoteriumConfiguration.initialized)
		{
			ReportHandler.getInstance().debug("Transforming static data.");
			ReportHandler.getInstance().debug("Loading settings");
			this.settings=this.data.getFromPath("settings", "value");
			ReportHandler.getInstance().debug("Loading permissions");
			this.permissions=this.data.getFromPath("permissions", "value");
			ReportHandler.getInstance().debug("Loading groups");
			this.groups=this.data.getFromPath("groups", "value");
			ReportHandler.getInstance().debug("Loading rules");
			this.rules=this.data.getFromPath("rules", "value");
			ReportHandler.getInstance().debug("Loading users");
			this.users=this.data.getFromPath("users", "value");
			ReportHandler.getInstance().debug("Loading defaultGroups");
			this.defaultGroups=this.data.getFromPath("defaultGroups", "value");
			if ((null==this.defaultGroups) || !this.defaultGroups.isJSONArray())
			{
				this.defaultGroups=JsonData.createArray();
			}
			BaseSoteriumConfiguration.initialized = true;
		}

		ReportHandler.getInstance().debug("Loading servers");
		JsonData stores=this.data.getFromPath("servers", "value");
		if ((null!=stores) && stores.isJSONArray())
		{
			for (Object o : stores)
			{
				if (o instanceof JSONObject)
				{
					JsonData store=new JsonData(o);
					ReportHandler.getInstance().debug("Loading configCls");
					Class configCls=store.getClassFromName("configCls");
					ReportHandler.getInstance().debug("Loading storeCls");
					Class storeCls=store.getClassFromName("storeCls");
					if (ClassX.isKindOf(configCls, BaseServerConfiguration.class) && ClassX.isKindOf(storeCls, IServer.class))
					{
						ReportHandler.getInstance().debug("Creating new server entry.");
						@SuppressWarnings("unchecked")
						ServerEntry serverEntry=new ServerEntry(storeCls, configCls, store);
						this.serverEntries.add(serverEntry);
					}
				}
			}
		}
	}

	protected BaseSoteriumConfiguration()
	{
	}

	// Overrides
	@Override
	public String getExceptionReportPath()
	{
		return this.getData().getString("exceptionReportPath", "value");
	}

	// Overrides
	@Override
	public String getDocsPath()
	{
		return this.getData().getString("docsPath", "value");
	}

	@Override
	public boolean isLogEnabled()
	{
		return this.getData().getBoolean("log_enabled", "value");
	}

	@Override
	public boolean isTesting()
	{
		return this.getData().getBoolean("testing_enabled", "value");
	}

	@Override
	public String getApplicationName()
	{
		return this.getData().getString("applicationName", "value");
	}

	@Override
	public int getUserDaysInactive()
	{
		Integer result=this.getData().getInteger("userDaysInactive", "value");
		return (null==result) ? Identity.DEFAULT_USER_LOG_INTERVAL : result;
	}

	@Override
	public int getUserIdentityDaysInactive()
	{
		Integer result=this.getData().getInteger("userIdentityDaysInactive", "value");
		return (null==result) ? Identity.DEFAULT_USER_LOG_INTERVAL : result;
	}

	@Override
	public int getUserDaysLeft()
	{
		Integer result=this.getData().getInteger("userDaysLeft", "value");
		return (null==result) ? Identity.DEFAULT_USER_LOG_INTERVAL : result;
	}

	@Override
	public int getUserLogInterval()
	{
		Integer result=this.getData().getInteger("userLogInterval", "value");
		return (null==result) ? Identity.DEFAULT_USER_LOG_INTERVAL : result;
	}

	@Override
	public Level getEmailLogLevel()
	{
		Level result=null;
		if (this.getData().hasKey("emailLogLevel"))
		{
			try
			{
				result=Level.parse(this.getData().getString("emailLogLevel", "value").toUpperCase());
			}
			catch (Exception ignored){}
		}
		return result;
	}

	@Override
	public String getServerName()
	{
		return this.getData().getString("serverName", "value");
	}

	@Override
	public String getBaseServerUrl()
	{
		return this.getData().getString("serverBaseUrl", "value");
	}
	@Override
	public String getBlobBaseUrl(){
		return this.getData().getString("blobBaseUrl", "value");
	}

	@Override
	public Object getProperty(String key)
	{
		Object result=this.getData().getObjectFromPath(key);
		if (result instanceof JsonData)
		{
			JsonData item=(JsonData) result;
			if (item.hasKey("value"))
			{
				result=item.getObjectFromPath("value");
			}
		}
		return result;
	}

	@Override
	public boolean isLoggable(Method method)
	{
		return false;
	}

	@Override
	public Long getOfflineWaitInterval()
	{
		if (null==this.offlineWaitInterval)
		{
			this.offlineWaitInterval=this.getData().getLong("offlineWaitInterval", "value");
			if (null==this.offlineWaitInterval)
			{
				this.offlineWaitInterval=BaseSoteriumConfiguration.DEFAULT_OFF_WAIT_INTERVAL;
			}
		}
		return this.offlineWaitInterval;
	}

	@Override
	public Integer getOfflineRetries()
	{
		if (null==this.offlineRetries)
		{
			this.offlineRetries=this.getData().getInteger("offlineWaitInterval", "value");
			if (null==this.offlineRetries)
			{
				this.offlineRetries=BaseSoteriumConfiguration.DEFAULT_OFF_RETRIES;
			}
		}
		return this.offlineRetries;
	}

	@Override
	public String getReferer()
	{
		return this.getData().getString("referer", "value");
	}

	@Override
	public boolean isCacheMemoryManagerEnabled()
	{
		return this.getData().getBoolean("cacheMemoryManagerEnabled", "value");
	}

	@Override
	public boolean isWarmUpEnabled()
	{
		return this.getData().getBoolean("warmUpEnabled", "value");
	}

	@Override
	public JsonData getDefaultGroups()
	{
		return this.defaultGroups;
	}

	@Override
	public boolean isXmlDtdDisabled()
	{
		return false;
	}

	@Override
	public void initializePrincipals()
	{
		if ((null!=this.users) && this.users.isJSONArray())
		{
			for (Object o : this.users)
			{
				if (o instanceof JSONObject)
				{
					JsonData data=new JsonData(o);
					String id=data.getString("identifier");
					if (!StringX.isBlank(id) && !StringX.equalsIgnoreCase(id, "example"))
					{
						try
						{
							this.makePrincipal(data);
						}
						catch (Exception ex)
						{
							ReportHandler.getInstance().warning(ex);
						}
					}
				}
			}
		}
		this.users=null;
	}

	private void makePrincipal(JsonData data)
		throws ApplicationException
	{
		Person person=IdentityHelper.getOrCreate(data, Identity.LoginType.pki);
		if ((null!=person))
		{
			JsonData categories=data.getFromPath("categories");

			if ((null!=categories) && categories.isJSONArray())
			{
				for (Object o : categories)
				{
					if (o instanceof JSONObject)
					{
						JsonData item=new JsonData(o);
						BaseContactDetail.CategoryTypes category=item.getEnum(BaseContactDetail.CategoryTypes.class, "category"
						);
						String value=item.getString("value");
						String ext=item.getString("ext");
						if ((null!=category) && !StringX.isBlank(value))
						{
							BaseContactDetail detail=ContactDetailHelper.make(category, value, ext);
							if (null!=detail)
							{
								boolean exists=false;
								for (BaseContactDetail bcd : person.getContactDetails())
								{
									if (detail.equals(bcd))
									{
										exists=true;
										break;
									}
								}
								if (!exists)
								{
									person.getContactDetails().add(detail);
								}
							}
						}
					}
				}
			}

			JsonData acl=data.getFromPath("acl");
			if (null!=acl)
			{
				JsonData groups=acl.getFromPath("groups");
				JsonData permissions=acl.getFromPath("permissions");

				if ((null!=groups) && groups.isJSONArray())
				{
					for (Object obj : groups)
					{
						if (obj instanceof Double)
						{
							Double itemId=(Double) obj;
							Group r=Group.Queries.getGroup(itemId);
							if ((null!=r) && !Group.isInGroup(person, r))
							{
								r.getPrincipals().add(person);
							}
						}
					}
				}
			}

			for (Identity identity : person.getIdentities())
			{
				if (!identity.hasStatus(Identity.Status.approved) && !identity.fetchDeprecated().getValue())
				{
					try
					{
						identity.fetchStatus().setValue(Identity.Status.approved);
						identity.save();
					}
					catch (Exception ex){
						Environment.getInstance().getReportHandler().severe(ex);
					}
				}
			}
		}
	}

	@Override
	public JsonData getPermissions()
	{
		return this.permissions;
	}

	@Override
	public JsonData getGroups()
	{
		return this.groups;
	}

	@Override
	public JsonData getRules()
	{
		return this.rules;
	}

	@Override
	public <T extends IEnvironmentConfiguration> T as(Class<? extends IEnvironmentConfiguration> cls)
	{
		T result=null;
		try
		{
			Constructor c=cls.getConstructor(File.class);
			//noinspection unchecked
			result=(T) c.newInstance(this.file);
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
		}
		return result;
	}

	@Override
	public boolean isOutputToConsole()
	{
		return this.getData().getBoolean("outputToConsole", "value");
	}

	/**
	 * Using this in production can cause performance issues as it calls the file system.
	 * @return true or false
	 */
	@Override
	public boolean isDebug()
	{
		return this.getData().getBoolean("debugMode", "value");
	}

	@Override
	public boolean isSandbox()
	{
		return this.getData().getBoolean("sandboxMode", "value");
	}

	@Override
	public boolean isServerMode()
	{
		return this.serverMode;
	}

	@Override
	public boolean isDevOnly()
	{
		return this.getData().getBoolean("devOnly", "value");
	}

	public String getKeyStoreConfigPath()
	{
		return this.getData().getString("keyStoreConfigPath", "value");
	}

	public String getKeyStoreConfigPathApollo()
	{
		return this.getData().getString("keyStoreConfigPathApollo", "value");
	}

	public void setServerMode(boolean enabled)
	{
		this.serverMode=enabled;
	}

	@Override
	public boolean isBatchMode()
	{
		return this.getData().getBoolean("batchMode", "value");
	}

	@Override
	public void setBatchMode(boolean batchMode)
	{
		this.setKeyValue("batchMode", batchMode);
	}

	protected void setKeyValue(String key, boolean value)
	{
		JsonData data=this.getData().getFromPath("value", key);
		if(null==data){
			data = JsonData.createObject();
			this.getData().put(key, data);
		}
		data.remove("value");
		data.put("value", value);
	}

	@Override
	public String getSetting(String key)
	{
		return ((null!=this.settings) && this.settings.isJSONObject()) ? this.settings.getString(key) : null;
	}

	@Override
	public void setSetting(String key, Object value)
	{
		if (null==this.settings)
		{
			this.settings=JsonData.createObject();
		}
		this.settings.remove(key);
		this.settings.put(key, value);
	}

	@Override
	public void removeSetting(String key)
	{
		this.settings.remove(key);
	}

	@Override
	public File getTempDir()
	{
		return this.getData().getFile("tempDir", "value");
	}

	@Override
	public File getNotesDir()
	{
		return this.getData().getFile("notesDir", "value");
	}

	@Override
	public <T> T open(IEnvironmentConfiguration.ServerRoles role, Object... params)
	{
		T result=null;
		for (ServerEntry serverEntry : this.serverEntries)
		{
			if (serverEntry.isInRole(role) && !serverEntry.isDisabled())
			{
				try
				{
					Environment.getInstance().getReportHandler().info(serverEntry.toString());
					Constructor configConstructor=serverEntry.getConfiguration().getConstructor(JsonData.class);

					BaseServerConfiguration config=(BaseServerConfiguration) configConstructor.newInstance(serverEntry.getConfigData());
					config.open();

					Constructor storeConstructor=serverEntry.getStore().getConstructor();
					IServer iServer=(IServer) storeConstructor.newInstance();
					ReportHandler.getInstance().info("Starting %s", iServer.getClass().getName());
					iServer.start(config, params);
					if (iServer.isOpened())
					{
						//noinspection unchecked
						result=(T) iServer;
						ReportHandler.getInstance().info("The role, %s, has been started for %s.", role, serverEntry.getStore().getName());
					}
					else{
						Environment.getInstance().getReportHandler().severe("%s is not opened", iServer.getClass().getSimpleName());
						Environment.getInstance().close();
					}
				}
				catch (Exception e)
				{
					ReportHandler.getInstance().severe(e);
				}
				break;
			}
		}
		return result;
	}

	@Override
	public int maxIteratorSize()
	{
		return this.getData().getInteger(Environment.DEFAULT_MAX_ITERATOR_SIZE, "maxIteratorSize", "value");
	}

	@Override
	public String getSecurityClassification()
	{
		return this.getData().getString("securityClassification", "value");
	}

	@Override
	public File getLicenseFile()
	{
		return this.getData().getFile("license", "licenseFile", "value");
	}

	@Override
	public File getPrivateKeyPath()
	{
		return this.getData().getFile("license", "privateKeyPath", "value");
	}

	@Override
	public boolean isInitializeDomains()
	{
		return this.getData().getBoolean(true, "initializeDomains", "value");
	}

	@Override
	public boolean isInitializeWebServer(){
		return this.getData().getBoolean(true, "initializeDomains", "value");
	}

	@Override
	public boolean isObfuscated()
	{
		return false;
	}

	@Override
	public int getAcsCacheExpireInDays()
	{
		return this.getData().getInteger(1, "acsCacheExpireInDays", "value");
	}

	@Override
	public Integer getKeyCode()
	{
		return this.getData().getInteger("keyCode", "value");
	}

	@Override
	public boolean isInitialize()
	{
		return this.getData().getBoolean(true, "initialize", "value");
	}

	@Override
	public void setInitialize(boolean enabled)
	{
		this.setKeyValue("initialize", enabled);
	}

	@Override
	public void setInitializeWebServer(boolean enabled)
	{
		this.setKeyValue("initializeWebServer", enabled);
	}

	@Override
	public boolean isTimeLogged()
	{
		return this.getData().getBoolean("logTime", "value");
	}

	@Override
	public boolean clearStores()
	{
		return this.clearStores;
	}

	@Override
	public Level getLogLevel()
	{
		return Level.parse(this.getData().getString("logLevel", "value"));
	}

	@Override
	public Collection<ServerEntry> getServerEntries()
	{
		return this.serverEntries;
	}

	@Override
	public String getResourcePath()
	{
		File result=new File(this.getData().getString("resourcePath", "value"));
		return ((null!=result) && result.exists()) ? result.getAbsolutePath() : null;
	}

	@Override
	public Integer getMaxThreads()
	{
		return this.getData().getInteger(1, 0, "maxThreads", "value");
	}

	@Override
	public Integer getReportMaxThreads()
	{
		return this.getData().getInteger(1, 0, "reportMaxThreads", "value");
	}

	@Override
	public boolean isWorkerDebugMode()
	{
		return this.getData().getBoolean("workerDebugMode", "value");
	}

	protected JsonData getData()
	{
		this.checkAndReload();
		return this.data;
	}

	/**
	 * Check the last modified date and if the file has been updated reload.
	 * This can be a performance issue in some cases be careful allowing this check.
	 */
	public void checkAndReload()
	{
		try
		{
			if(null!=this.file)
			{
				Long dif=this.file.lastModified();
				if (!Objects.equals(dif, this.lastChecked))
				{
					this.lastChecked=dif;
					this.load();
				}
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
		}
	}

	// Getters and setters
	public Collection<Class<? extends DataVertex>> getCacheableObjects()
	{
		Collection<Class<? extends DataVertex>> results=new ArrayList<>();
		if (this.getData().hasKey("cacheableObjects"))
		{
			JsonData items=this.getData().getFromPath("cacheableObjects");
			if ((null!=items) && items.isJSONArray())
			{
				for (Object o : items)
				{
					if (o instanceof JSONObject)
					{
						JsonData item=new JsonData(o);
						Class value=item.getClassFromName("value");
						if (ClassX.isKindOf(value, DataVertex.class))
						{
							//noinspection unchecked
							results.add(value);
						}
					}
				}
			}
		}
		return results;
	}

	public Collection<CommandItem> getCommands()
	{
		Collection<CommandItem> results=new ArrayList<>();
		JsonData items=this.getData().getFromPath("commands", "value");
		if ((null!=items) && items.isJSONArray())
		{
			for (Object o : items)
			{
				if (o instanceof String)
				{
					String value=(String) o;
					if (!StringX.isBlank(value))
					{
						String[] parts=RegExHelper.WHITE_SPACE.split(value);
						if ((null!=parts) && (parts.length>0))
						{
							results.add(new CommandItem(parts));
						}
					}
				}
			}
		}
		return results;
	}
	public File getFile()
	{
		return this.file;
	}

	public void setClearStores(boolean clearStores)
	{
		this.clearStores=clearStores;
	}
}