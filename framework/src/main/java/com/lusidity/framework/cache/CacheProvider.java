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

package com.lusidity.framework.cache;


import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;

public
interface CacheProvider
{
	/**
	 * Get an object with the specified key.
	 * @param key Key.
	 * @param loader Cache loader; or null to behave like a non-loading cache.
	 * @return Cached object, or null if an object of the specified type is not cached for the specified key.
	 */
	Object get(String key, CacheLoader loader) throws ApplicationException;

	/**
	 * Put a value into the cache.
	 * @param key Key.
	 * @param value Value.
	 * @param <T> Type.
	 */
	<T> void put(String key, T value) throws ApplicationException;

	/**
	 * Remove the specified key from the cache.
	 * @param key Key.
	 */
	void invalidate(String key);

	/**
	 * Open connection to cache provider.
	 */
	void open();

	/**
	 * Close connection to cache provider.
	 */
	void close();

	/**
	 * Get cache statistics.
	 * @return Cache statistics.
	 */
    JsonData getStats();

	/**
	 * Get a lock object for synchronization.
	 * @return Lock object.
	 */
	Object getLock();

    void cleanUp();
}
