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
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.security.ObfuscateX;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@SuppressWarnings("UnusedDeclaration")
public
class ObfuscateCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    private static final String SEED_OPTION = "seed";
    private static final String VALUE_OPTION = "value";
	private static final String BASE_OPTION = "base";

	//obfuscate -seed 69ceaet7-efbf-4d0d-8t8b-4ba58t05bb24 -base null -value t7r9B0ux

	public ObfuscateCommand(){
		super();
	}

    @Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		String result = ObfuscateX.obfuscate(commandLine.getOptionValue(ObfuscateCommand.VALUE_OPTION), commandLine.getOptionValue(ObfuscateCommand.SEED_OPTION), commandLine.getOptionValue(ObfuscateCommand.BASE_OPTION));
		System.out.println(result);
    }

	@Override
	public
	String getDescription()
	{
		return "Obfuscate something.";
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
		return "obfuscate";
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
        Options options = new Options();

        Option seed = new Option(ObfuscateCommand.SEED_OPTION, true, "Up to 32 characters as a key to the encryption algorithm.");
        seed.setRequired(true);
        seed.setType(String.class);
        options.addOption(seed);

		Option base = new Option(ObfuscateCommand.BASE_OPTION, true, "A 36 character key.");
		base.setRequired(false);
		base.setType(String.class);
		options.addOption(base);

        Option value = new Option(ObfuscateCommand.VALUE_OPTION, true, "The value being obfuscated.");
        value.setRequired(true);
        value.setType(String.class);
        options.addOption(value);

        return options;
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
