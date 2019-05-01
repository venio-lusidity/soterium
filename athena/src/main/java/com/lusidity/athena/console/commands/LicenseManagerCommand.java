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
import com.lusidity.framework.text.StringX;
import com.lusidity.license.LicenseManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;

import java.io.File;

@SuppressWarnings("UnusedDeclaration")
public
class LicenseManagerCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    private static final String PUB_PATH = "pub";
    private static final String PRVT_PATH = "prvt";
	private static final String DESTINATION = "dest";
	private static final String FILENAME= "fn";
	private static final String DAYS= "days";
	private static final String ACCESS_LEVEL = "al";
	private static final String KEY_VALUE_PAIRS =  "kvp";

	public LicenseManagerCommand(){
		super();
	}

    @Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		File pub = new File(commandLine.getOptionValue(LicenseManagerCommand.PUB_PATH));
		File prvt = new File(commandLine.getOptionValue(LicenseManagerCommand.PRVT_PATH));
		File dest = new File(commandLine.getOptionValue(LicenseManagerCommand.DESTINATION));
		String filename = commandLine.getOptionValue(LicenseManagerCommand.FILENAME);
		Integer days = Integer.parseInt(commandLine.getOptionValue(LicenseManagerCommand.DAYS));
		LicenseManager.AccessLevel al = LicenseManager.AccessLevel.valueOf(commandLine.getOptionValue(LicenseManagerCommand.ACCESS_LEVEL));

		DateTime expiresOn = DateTime.now().plusDays(days);

		Object[] kvp = null;
		String val = commandLine.getOptionValue(LicenseManagerCommand.KEY_VALUE_PAIRS);
		if(!StringX.isBlank(val)){
			kvp = StringX.split(val, ",");
		}

		LicenseManager manager = new LicenseManager(pub, prvt);
		File result = manager.create(dest, filename, expiresOn, al, kvp);
		Environment.getInstance().getReportHandler().say("License created at %s", result.getAbsolutePath());
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
		return "licman";
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

        Option pub = new Option(LicenseManagerCommand.PUB_PATH, true, "Public RAS DER file path.");
        pub.setRequired(true);
        pub.setType(String.class);
        options.addOption(pub);

		Option prvt = new Option(LicenseManagerCommand.PRVT_PATH, true, "Private RAS DER file path.");
		prvt.setRequired(true);
		prvt.setType(String.class);
		options.addOption(prvt);

		Option dest = new Option(LicenseManagerCommand.DESTINATION, true, "Output directory of all files.");
		dest.setRequired(true);
		dest.setType(String.class);
		options.addOption(dest);

		Option fn = new Option(LicenseManagerCommand.FILENAME, true, "The license filename not including extension.");
		fn.setRequired(true);
		fn.setType(String.class);
		options.addOption(fn);

		Option day = new Option(LicenseManagerCommand.DAYS, true, "How many days is it good from today.");
		day.setRequired(true);
		day.setType(Integer.class);
		options.addOption(day);

		String values = StringX.enumToString(LicenseManager.AccessLevel.class);

		Option al = new Option(LicenseManagerCommand.ACCESS_LEVEL, true, String.format("Expected value: %s", values));
		al.setRequired(true);
		al.setType(String.class);
		options.addOption(al);

		Option kvp = new Option(LicenseManagerCommand.KEY_VALUE_PAIRS, true, "Comma delimited list of key value pairs as additions property values for the license file.  Do not use a space after the delimiter.");
		kvp.setRequired(false);
		kvp.setType(String.class);
		options.addOption(kvp);

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
