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

package com.lusidity.console;

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.json.JsonData;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface Command
{
	/**
	 * Execute this command.
	 *
	 * @param console     Console.
	 * @param commandLine Command line.
	 * @throws Exception
	 */
	void execute(Console console, CommandLine commandLine)
		throws Exception;

	/**
	 * If this is web enabled use the JsonData object for the params.
	 * The key is the option the value is the value for that option.
	 *
	 * @param params    A key value pair as arguments.
	 * @param principal
	 * @return a JsonData object for the response.
	 */
	JsonData execute(JsonData params, BasePrincipal principal);

	void showHelp(Console console, CommandLine commandLine);

// Getters and setters
	/**
	 * Get a brief summary/description of this command.
	 *
	 * @return Summary/description.
	 */
	String getDescription();

	/**
	 * Get name of this command.
	 *
	 * @return Command name.
	 */
	String getName();

	/**
	 * Get command line options for this command.
	 *
	 * @return Apache Commons CLI Options.
	 */
	Options getOptions();

	/**
	 * Is this command accessible from the web?
	 *
	 * @return true or false.
	 */
	boolean isWebEnabled();
}
