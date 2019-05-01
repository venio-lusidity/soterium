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

@SuppressWarnings("UnusedDeclaration")
public
class DeleteCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    public static final String ID_OPTION = "id";
    public static final String VERBOSE_OPTION = "verbose";

	public DeleteCommand(){
		super();
	}

	@SuppressWarnings({
		"SingleStatementInBlock",
		"ThrowCaughtLocally"
	})
	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
        try {
            if (!commandLine.hasOption(DeleteCommand.ID_OPTION)) {
                throw new ApplicationException("Option -id is missing.");
            }

            String id = commandLine.getOptionValue(DeleteCommand.ID_OPTION);
            if (StringX.isBlank(id)) {
                throw new ApplicationException("The value of -id cannot be null.");
            }

            boolean deleted = false;
            int total = 0;

	        DataVertex vertex =VertexFactory.getInstance().get(id);
	        if(null!=vertex){
		        deleted = vertex.delete();
	        }

            System.out.println("\n\n");
            if(!deleted){
                System.out.println(String.format("Failed to delete vertex or vertices: %s", id));
            }
            else {
                System.out.println(String.format("Deleted vertex or vertices, %s.", id));
            }
            System.out.println("\n\n");

        }
        catch (Exception ex){
	        //noinspection CallToPrintStackTrace
	        ex.printStackTrace();
        }

        System.out.println("\n\n");
	}

    private void process(Class<? extends DataVertex> cls, String id){
        System.out.println("\nImporting...\n");
	    // TODO: create a class that starts a thread that monitors the progress.
        /*
        ConsoleProgressThreadBase progress = null;
        try {
            DropClassHandler dropClassHandler = new DropClassHandler(cls, 1, 0);

            progress = new ConsoleProgressBarThread(System.out, dropClassHandler);
            progress.setShowCounter(true);
            progress.start();

            dropClassHandler.process();

            progress.waitToStop();
            System.out.println(String.format("\n\n The %s command completed.", this.getName()));
        } catch (Exception e) {
            if (null != progress) {
                progress.waitToStop();
            }
            Environment.getInstance().getReportHandler().severe(e);
        }
        */
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
		return "delete";
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

        Option phraseOption = new Option(DeleteCommand.ID_OPTION, true, "The id of the entity.");
        phraseOption.setType(String.class);
        phraseOption.setRequired(true);
        options.addOption(phraseOption);

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
