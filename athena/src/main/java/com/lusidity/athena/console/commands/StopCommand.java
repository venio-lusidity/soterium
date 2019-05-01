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

package com.lusidity.athena.console.commands;


import com.lusidity.console.Console;
import com.lusidity.console.commands.BaseCommand;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.json.JsonData;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@SuppressWarnings("UnusedDeclaration")
public
class StopCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

	// Constructors
	public StopCommand(){
		super();
	}

	// Overrides
	@Override
	public void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		console.say("Stopping.");
		console.stopListening();
		console.close();
	}

	@Override
	public JsonData execute(JsonData params, BasePrincipal principal)
	{
		return null;
	}

	@Override
	public
	String getDescription()
	{
		return "Stop listening.";
	}

	/**
	 * Get name for this command.
	 *
	 * @return Command name.
	 */
	@Override
	public
	String getName()
	{
		return "stop";
	}

	/**
	 * Get command line options for this command.
	 *
	 * @return Apache Commons CLI Options.
	 */
	@Override
	public
	Options getOptions()
	{
		return null;
	}

    @Override
    public boolean isWebEnabled() {
        return false;
    }

}
