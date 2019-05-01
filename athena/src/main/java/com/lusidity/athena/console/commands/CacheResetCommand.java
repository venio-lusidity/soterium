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

@SuppressWarnings("UnusedDeclaration")
public
class CacheResetCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

	public CacheResetCommand(){
		super();
	}

	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
        Environment.getInstance().getCache().resetCache();
        System.out.println("The datastore cache statuses have been reset.");
	}

    @Override
	public
	String getDescription()
	{
		return "Reset cache statuses.";
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
		return "cacheReset";
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
        return true;
    }

    @Override
    public JsonData execute(JsonData params, BasePrincipal principal) {
        Environment.getInstance().getCache().resetCache();
        JsonData result = JsonData.createObject();
        result.put("success", true);
        return result;
    }

}
