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

package com.lusidity.cache;

import com.lusidity.data.DataVertex;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.server.IServer;

import java.net.URI;
import java.util.Map;

public interface ICache extends IServer
{
	void put(DataVertex entry)
		throws ApplicationException;

	/**
	 * Check to see if the configuration file has changed and if so reload it.
	 */
	void checkAndLoad();

	<T extends DataVertex> T get(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType,
	                             String key)
		throws ApplicationException;

	<T extends DataVertex> T get(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType,
	                             URI key)
		throws ApplicationException;

	void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key,
	         String entry)
		throws ApplicationException;

	void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key,
	         DataVertex entry)
		throws ApplicationException;

	void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException;

	void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException;

	void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key)
		throws ApplicationException;

	JsonData toJson();

	void resetCacheAttempts();

	void resetCache();

	void reload();

	/**
	 * Delete all cached objects and reset the cache.
	 */
	boolean flushAll();

// Getters and setters
Map<Class<? extends DataVertex>, Long> getCacheableObjects();

	long getTotal();

	boolean isDisabled();

	void setDisabled(boolean disabled);

	JsonData getStats();
}
