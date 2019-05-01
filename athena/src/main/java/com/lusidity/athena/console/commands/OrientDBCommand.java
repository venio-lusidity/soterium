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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@SuppressWarnings("UnusedDeclaration")
public
class OrientDBCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    private static String SET_OPTION = "set";
    private static String ALTER_OPTION = "alter";
    private static String GET_OPTION = "get";
    private static String ALL_OPTION = "all";

	public OrientDBCommand(){
		super();
	}

	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
        try {
	        /*
            if (Environment.getInstance().getDataStore() instanceof ODBDataStore) {
                ODBDataStore odbDataStore = (ODBDataStore) Environment.getInstance().getDataStore();
                if(commandLine.hasOption(OrientDBCommand.ALL_OPTION)){
                    JsonData items = odbDataStore.getConfig();
                    if(!items.isEmpty()){
                        console.say(items.toString(5));
                    }
                }
                else if(commandLine.hasOption(OrientDBCommand.GET_OPTION)){
                    String key = commandLine.getOptionValue(OrientDBCommand.GET_OPTION);
                    JsonData item = odbDataStore.getConfig(key);
                    if(null!=item){
                        console.say(item.toString(5));
                    }
                }
                else if (commandLine.hasOption(OrientDBCommand.SET_OPTION)) {
                    String value = commandLine.getOptionValue(OrientDBCommand.SET_OPTION);
                    String parts[] = StringX.split(value, "::");
                    if (parts.length == 2) {
                        odbDataStore.setConfig(parts[0], parts[1]);
                    } else {
                        System.out.println("The config option value is not in the proper format, expected key::value.");
                    }
                } else if (commandLine.hasOption(OrientDBCommand.ALTER_OPTION)) {
                    String cmd = commandLine.getOptionValue(OrientDBCommand.ALTER_OPTION);
                    if (StringX.startsWithIgnoreCase(cmd, "alter")) {
                        odbDataStore.alter(cmd);
                    } else {
                        System.out.println("The command is not valid.");
                    }
                } else {
                    System.out.println("Use -set or -alter.");
                }

            } else {
                System.out.println("The data store is not an OrientDB data store.");
            }
            */
        }
        catch (Exception ex){
            System.out.println("\n");
            ex.printStackTrace();
            System.out.println("\n");
        }
    }

	@Override
	public
	String getDescription()
	{
		return "Set or report global configurations and alter statements.";
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
		return "odb";
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

        options.addOption(OrientDBCommand.SET_OPTION, true, "Sets a configuration value within the data store. (format key::value)");
        options.addOption(OrientDBCommand.ALTER_OPTION, true, "Alter the data store.");
        options.addOption(OrientDBCommand.ALL_OPTION, false, "Reports all configuration settings.");
        options.addOption(OrientDBCommand.GET_OPTION, true, "Report configuration setting by key.");

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
