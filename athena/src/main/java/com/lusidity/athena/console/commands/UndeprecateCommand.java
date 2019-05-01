/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
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
class UndeprecateCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

	// Fields
	public static final String ID_OPTION = "id";
    public static final String VERBOSE_OPTION = "verbose";

	// Constructors
	public UndeprecateCommand(){
		super();
	}

	// Overrides
	@SuppressWarnings({
		"SingleStatementInBlock",
		"ThrowCaughtLocally"
	})
	@Override
	public void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		try {
			if (!commandLine.hasOption(UndeprecateCommand.ID_OPTION)) {
				throw new ApplicationException("Option -id is missing.");
			}

			String id = commandLine.getOptionValue(UndeprecateCommand.ID_OPTION);
			if (StringX.isBlank(id)) {
				throw new ApplicationException("The value of -id cannot be null.");
			}

			boolean saved = false;
			int total = 0;

			DataVertex vertex =VertexFactory.getInstance().get(id);
			if((null!=vertex) && vertex.fetchDeprecated().getValue()){
				vertex.fetchDeprecated().setValue(false);
				saved = vertex.save();
			}

			System.out.println("\n\n");
			if(!saved){
				System.out.println(String.format("Failed to recover vertex or vertices: %s", id));
			}
			else {
				System.out.println(String.format("Recovered vertex or vertices, %s.", id));
			}
			System.out.println("\n\n");

		}
		catch (Exception ex){
			//noinspection CallToPrintStackTrace
			ex.printStackTrace();
		}

		System.out.println("\n\n");
	}

	@Override
	public JsonData execute(JsonData params, BasePrincipal principal)
	{
		return null;
	}

    @Override
	public
	String getDescription()
	{
		return "Recover a deprecated vertex";
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
		return "deprecation";
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

        Option phraseOption = new Option(UndeprecateCommand.ID_OPTION, true, "The id of the entity.");
        phraseOption.setType(String.class);
        phraseOption.setRequired(true);
        options.addOption(phraseOption);

        return options;
    }

    @Override
    public boolean isWebEnabled() {
        return false;
    }

	private void process(Class<? extends DataVertex> cls, String id)
	{
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

}
