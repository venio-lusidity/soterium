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


import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;

import java.io.Closeable;
import java.io.IOException;

public interface IStoreConfiguration extends Closeable
{
	/**
	 * Open module.
	 */
	void open()
		throws ApplicationException, IOException;

	boolean transform(JsonData item)
		throws Exception;

	/**
	 * Should the configuration data be updated.
	 *
	 * @return true or false.
	 */
	boolean hasFileBeenUpdated();

	/**
	 * If the file has been updated and a span of time has occurred that is greater than equal to the interval,
	 * reload the configuration data.
	 */
	void checkAndReload();

// Getters and setters
	/**
	 * Is this module open?
	 *
	 * @return true if module is open.
	 */
	boolean isOpened();

	/**
	 * The configuration data.
	 *
	 * @return
	 */
	JsonData getConfigData();

	/**
	 * The interval to check for update configuration data.
	 *
	 * @return An interval in milliseconds.
	 */
	long getInterval();

	/**
	 * Is this module enabled? Only enabled modules are loaded.
	 *
	 * @return true if module is enabled.
	 */
	boolean isEnabled();
}
