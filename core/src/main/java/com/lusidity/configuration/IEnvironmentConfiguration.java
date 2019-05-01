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

import com.lusidity.framework.json.JsonData;
import org.restlet.data.Method;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;

public interface IEnvironmentConfiguration
{

	/**
	 * Enumerated type for server roles.
	 */
	enum ServerRoles
	{
		cache,
		data,
		index,
		web,
		report
	}

	Object getProperty(String key);

	boolean isLoggable(Method method);

	void initializePrincipals();

	<T extends IEnvironmentConfiguration> T as(Class<? extends IEnvironmentConfiguration> cls);

	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	boolean clearStores();

	String getSetting(String key);

	void setSetting(String key, Object value);

	void removeSetting(String key);

	<T> T open(IEnvironmentConfiguration.ServerRoles role, Object... params);

	int maxIteratorSize();

	String getSecurityClassification();

	File getLicenseFile();

	File getPrivateKeyPath();

	boolean isInitializeDomains();

	default boolean isInitializeWebServer()
	{
		return true;
	}

	boolean isObfuscated();

	int getAcsCacheExpireInDays();

	Integer getKeyCode();

	// Getters and setters
	String getExceptionReportPath();

	// Overrides
	String getDocsPath();

	boolean isLogEnabled();

	boolean isTesting();

	String getApplicationName();

	int getUserDaysInactive();

	int getUserIdentityDaysInactive();

	int getUserDaysLeft();

	int getUserLogInterval();

	Level getEmailLogLevel();

	String getServerName();

	String getBaseServerUrl();

	String getBlobBaseUrl();

	Long getOfflineWaitInterval();

	Integer getOfflineRetries();

	String getReferer();

	boolean isCacheMemoryManagerEnabled();

	boolean isWarmUpEnabled();

	JsonData getDefaultGroups();

	boolean isXmlDtdDisabled();

	JsonData getPermissions();

	JsonData getGroups();

	JsonData getRules();

	boolean isOutputToConsole();

	boolean isDebug();

	boolean isDevOnly();

	boolean isSandbox();

	boolean isServerMode();

	boolean isBatchMode();

	@SuppressWarnings("BooleanParameter")
	void setBatchMode(boolean batchMode);

	boolean isInitialize();

	@SuppressWarnings("BooleanParameter")
	void setInitialize(boolean initialize);

	default void setInitializeWebServer(boolean initialize)
	{
		// do nothing
	}

	boolean isTimeLogged();

	Level getLogLevel();

	@SuppressWarnings("unused")
	Collection<ServerEntry> getServerEntries();

	String getResourcePath();

	Integer getMaxThreads();

	Integer getReportMaxThreads();

	boolean isWorkerDebugMode();

	File getTempDir();

	File getNotesDir();
}
