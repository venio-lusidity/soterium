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

import com.lusidity.console.Console;
import com.lusidity.console.commands.BaseCommand;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.exceptions.UserException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/** User: jjszucs Date: 6/22/12 Time: 12:35 */
@SuppressWarnings("UnusedDeclaration")
public
class ExecCommand
	extends BaseCommand
{
	private static final String COMMENT_PREFIX = "#";

	public ExecCommand(){
		super();
	}

	/**
	 * Execute this command.
	 *
	 * @param console
	 * 	Console.
	 * @param commandLine
	 * 	Command line.
	 *
	 * @throws UserException
	 */
	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws UserException, IOException
	{
		String[] args = commandLine.getArgs();
		int nArgs = args.length;
		if (nArgs != 1)
		{
			throw new UserException("Script must be specified.");
		}
		String scriptPath = args[0];
		BufferedReader scriptReader = new BufferedReader(new FileReader(scriptPath));
		try
		{
			String line;
			//noinspection NestedAssignment,MethodCallInLoopCondition
			while ((line = scriptReader.readLine()) != null)
			{
				line = line.trim();
				if (!StringX.isBlank(line) && !line.startsWith(ExecCommand.COMMENT_PREFIX))
				{
					console.say(line);
					console.interpret(line, false /* isInteractive */);
				}
			}
		}
		finally
		{
			scriptReader.close();
		}
	}

	@Override
	public
	String getDescription()
	{
		return "Execute a script.";
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
		return "exec";
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
		//  FileInfo path of script is passed as an anonymous option
		return new Options();
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
