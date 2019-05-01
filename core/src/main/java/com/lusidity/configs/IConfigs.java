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

package com.lusidity.configs;

public interface IConfigs
{
	/**
	 * Used to load in the values for the configuration.
	 */
	void open();

	/**
	 * Was the configuration file changed/updated.
	 * @return true or false
	 */
	boolean isUpdated();

	/**
	 * The name of the configuration.
	 * Recommend using a value within the configuration file.
	 * @return The name of the configuration.
	 */
	String name();
	/**
	 * What is this configuration used for.
	 * Recommend using a value within the configuration file.
	 * @return Describes what this configuration is used for.
	 */
	String description();

	/**
	 * Is the configuration opened.
	 * @return true or false
	 */
	boolean isOpen();
}
