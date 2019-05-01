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
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.net.URI;

@SuppressWarnings("UnusedDeclaration")
public
class GetCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    public static final String ID_OPTION = "id";
    public static final String VERBOSE_OPTION = "verbose";

	public GetCommand(){
		super();
	}

	@SuppressWarnings({
		"CallToPrintStackTrace",
		"ThrowCaughtLocally"
	})
	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{

        try {
            if (!commandLine.hasOption(GetCommand.ID_OPTION)) {
                throw new ApplicationException("Option -id is missing.");
            }

            String id = commandLine.getOptionValue(GetCommand.ID_OPTION);
            if (StringX.isBlank(id)) {
                throw new ApplicationException("The value of -id cannot be null.");
            }

            DataVertex vertex;

            if(StringX.startsWith(id, "/vertices/") || StringX.startsWith(id, "#")){
                vertex = VertexFactory.getInstance().get(DataVertex.class, id);
            }
            else{
                vertex = VertexFactory.getInstance().get(DataVertex.class, URI.create(id));
            }

            System.out.println("\n\n");
            if(null==vertex){
                System.out.println(String.format("Could not find entity: %s", id));
            }
            else {
                JsonData results = vertex.toJson(false);
                System.out.println(results.toString(5));
            }
            System.out.println("\n\n");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        System.out.println("\n\n");
	}

	@Override
	public
	String getDescription()
	{
		return "Search for a term in Apollo..";
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
		return "get";
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
        Options options=new Options();

        Option phraseOption = new Option(GetCommand.ID_OPTION, true, "The id of the entity.");
        phraseOption.setType(String.class);
        phraseOption.setRequired(true);
        options.addOption(phraseOption);

        options.addOption(GetCommand.VERBOSE_OPTION, false, "Output all related facts in JSON format, requires -verbose.");

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
