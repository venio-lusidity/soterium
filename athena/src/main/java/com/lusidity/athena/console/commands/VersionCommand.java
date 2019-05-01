/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.athena.console.commands;

import com.lusidity.Environment;
import com.lusidity.console.Console;
import com.lusidity.console.commands.BaseCommand;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.json.JsonData;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public
class VersionCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

	public VersionCommand(){
		super();
	}

	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		Environment environment= Environment.getInstance();
		Map<String, String> properties = environment.getAppProperties();
		if (!properties.isEmpty())
		{
			for (Map.Entry<String, String> property : properties.entrySet())
			{
				//noinspection UseOfSystemOutOrSystemErr
				console.say(String.format("%s: %s", property.getKey(), property.getValue()));
			}
		}
	}

	@Override
	public
	String getDescription()
	{
		return "version of the current build";
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
		return "version";
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

    @Override
    public JsonData execute(JsonData params, BasePrincipal principal) {
        return null;
    }

}
