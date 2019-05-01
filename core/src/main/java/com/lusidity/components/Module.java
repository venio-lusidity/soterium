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

package com.lusidity.components;

import com.lusidity.framework.exceptions.ApplicationException;

/**
 * Interface for any module in the Skynet component model.
 */
public interface Module
{
	/**
	 * Open module.
	 *
	 * @param params
	 */
	void open(Object... params)
		throws ApplicationException;

	/**
	 * Close module.
	 */
	void close();

// Getters and setters
	/**
	 * Is this module open?
	 *
	 * @return true if module is open.
	 */
	boolean isOpened();

	/**
	 * Is this module enabled? Only enabled modules are loaded.
	 *
	 * @return true if module is enabled.
	 */
	boolean isEnabled();
}