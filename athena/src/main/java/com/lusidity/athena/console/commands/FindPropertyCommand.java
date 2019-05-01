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
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;

/**
 * findProperty -property "/common/topic/image,/type/object/name" -limit 5
 */
@SuppressWarnings("UnusedDeclaration")
public
class FindPropertyCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------
    private static final String PROPERTY_OPTION = "property";
    private static final String LIMIT_OPTION = "limit";

	public FindPropertyCommand(){
		super();
	}

	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
	    String property = commandLine.getOptionValue(FindPropertyCommand.PROPERTY_OPTION);
        String[] properties = StringX.split(property, ",");

        int limit = 5;
        if(commandLine.hasOption(FindPropertyCommand.LIMIT_OPTION)){
            limit = Integer.parseInt(commandLine.getOptionValue(FindPropertyCommand.LIMIT_OPTION));
        }

        Collection<DataVertex> vertices = Environment.getInstance().getDataStore().getQueries().hasProperty(limit, DataVertex.class, properties);

        System.out.println("\n\n");
        if(vertices.isEmpty()){
            System.out.println(String.format("No results for: %s", property));
        }
        else {
            JsonData results = JsonData.createArray();
            for(DataVertex vertex: vertices){
                JsonData item = vertex.toJson(false, "en");
                boolean valid = true;
	            if(null!=properties)
	            {
		            for (String prop : properties)
		            {
			            valid = item.has(prop);
			            if (!valid)
			            {
				            break;
			            }
		            }
	            }
                if(valid) {
                    results.put(item);
                }
            }
            System.out.println(results.toString(5));
        }
        System.out.println("\n\n");
    }

	@Override
	public
	String getDescription()
	{
		return "Find nodes in the data store with the specified property.";
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
		return "findProperty";
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

        Option property = new Option(FindPropertyCommand.PROPERTY_OPTION, true, "The name of the property to find.");
        property.setRequired(true);
        property.setType(String.class);
        options.addOption(property);

        Option limit = new Option(FindPropertyCommand.LIMIT_OPTION, true, "Max records to find.");
        limit.setRequired(false);
        limit.setType(Integer.class);
        options.addOption(limit);

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
