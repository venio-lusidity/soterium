/*
 * Copyright (c) 2008-2013, Venio, Inc.
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
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.server.WebServerConfig;
import com.lusidity.services.server.WebServices;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
public
class ServerCommand
	extends BaseCommand
{
	private static final String CONFIG_OPTION = "config";
	private static final String ACTION_OPTION = "action";
	private static final String ACTION_STOP = "stop";
	private static final String ACTION_START = "start";
	private static final String DEBUG_OPTION = "debug";
    private static final String LOGGING_OPTION = "logging";

	public ServerCommand(){
		super();
	}

    @SuppressWarnings("FeatureEnvy")
	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws ApplicationException
	{
		String action = commandLine.getOptionValue(ServerCommand.ACTION_OPTION);

		if (StringX.isBlank(action))
		{
			action = ServerCommand.ACTION_START;
		}

		if (action.equals(ServerCommand.ACTION_START))
		{
			if (StringX.isBlank(commandLine.getOptionValue(ServerCommand.CONFIG_OPTION)))
			{
                console.say("The configuration data is missing.");
			}
			else
			{
				try
				{
                    String config = commandLine.getOptionValue(ServerCommand.CONFIG_OPTION);
                    JsonData jsonData = new JsonData(config);
                    WebServerConfig webServerConfig = new WebServerConfig(jsonData);

                    if(webServerConfig.isValid()){
                        console.say(
                                String.format(
                                        "\n\nStarting web services: protocol=%s, port=%d ...", webServerConfig.getProtocol().getSchemeName(), webServerConfig.getPort()
                                )
                        );
                        WebServices webServices = new WebServices();
                        webServices.start(webServerConfig);

                    }
                    else{
                        console.say("The configuration data is not valid.");
                    }

				}
				catch (Exception e)
				{
					throw new ApplicationException(e);
				}
			}
		}
		else
		{
			ServerCommand.stop(console);
		}
	}

	@Override
	public
	String getDescription()
	{
		return "Start web services.";
	}

	private static
	void stop(Console console)
	{
		if(null!=Environment.getInstance().getWebServer()){
            try {
                Environment.getInstance().getWebServer().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
		return "server";
	}

	/**
	 * Get command line options for this command.
	 *
	 * @return Apache Commons CLI Options.
	 */
	@SuppressWarnings("AccessStaticViaInstance")
	@Override
	public
	Options getOptions()
	{
		Options options = new Options();

		options.addOption(
			OptionBuilder.
				hasArg(true).
				isRequired(false).
				withDescription("Configuration").
				create(ServerCommand.CONFIG_OPTION)
		);

		options.addOption(
			OptionBuilder.
				hasArg(true).
				isRequired(false).
				withDescription("Available actions are stop or start.").
				create(ServerCommand.ACTION_OPTION)
		);

		options.addOption(
			OptionBuilder.
				hasArg(false).
				isRequired(false).
				withDescription("Debugging mode.").
				create(ServerCommand.DEBUG_OPTION)
		);

        options.addOption(
                OptionBuilder.
                        hasArg(true).
                        isRequired(false).
                        withDescription("Enable logging? true or false, default true.").
                        create(ServerCommand.LOGGING_OPTION)
        );

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
