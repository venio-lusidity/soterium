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


import com.lusidity.Environment;
import com.lusidity.console.Console;
import com.lusidity.console.commands.BaseCommand;
import com.lusidity.data.DeduplicateData;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.interfaces.IReIndexer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class DeduplicateCommand extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

	// Fields
	public static final String STORE_OPTION="store";

	// Constructors
	public DeduplicateCommand(){
		super();
	}

	// Overrides
	@Override
	public void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		String storeStr=commandLine.getOptionValue(QueryCommand.STORE_OPTION);

		if (StringX.isBlank(storeStr))
		{
			Environment.getInstance().getReportHandler().severe("The store is required");
		}

		DeduplicateData deduplicateData = new DeduplicateData(1, 0, true);
// TODO: create a class that starts a thread that monitors the progress.
        /*
        ConsoleProgressThreadBase progress = null;
        try {
            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();

            System.out.println(String.format("%n%nDeduplicating started at %s.", stopwatch.getStartedWhen().toString("dd-MMM-yyyy HH:mm:ss")));

            progress = new ConsoleProgressBarThread(System.out, deduplicateData);
            progress.setShowCounter(true);
            progress.start();

            if(StringX.endsWithAnyIgnoreCase(storeStr, "all")){
                deduplicateData.process();
            }
            else
            {
                @SuppressWarnings("unchecked")
                Class<? extends DataVertex> store=(Class<? extends DataVertex>) Class.forName(storeStr);

                if ((null!=store))
                {
                    deduplicateData.process(store);
                }
            }

            progress.waitToStop();
            stopwatch.stop();
            System.out.println(String.format("%s%sDeduplicated %d vertices in %s%s%s",
                System.lineSeparator(), System.lineSeparator(),deduplicateData.getDeleted(),
                stopwatch.toString(), System.lineSeparator(),System.lineSeparator()));
        } catch (Exception e) {
            if (null != progress) {
                progress.waitToStop();
            }
            Environment.getInstance().getReportHandler().severe(e);
        }
        */
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
        return "Repair or update and existing index.";
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
        return "deduplicate";
    }

    /**
     * Get command line options for this command.
     *
     * @return Apache Commons CLI Options.
     */
    @Override
    public Options getOptions()
    {
        Options options=new Options();
        Option storeOption=new Option(DeduplicateCommand.STORE_OPTION, true, "A class path to the type of object." +
                                                                         "  If you want to deduplicate all, use the word \"all\"");
        storeOption.setType(String.class);
        storeOption.setRequired(true);
        options.addOption(storeOption);
        return options;
    }

    @Override
    public boolean isWebEnabled() {
        return false;
    }

	private void process(IReIndexer indexer)
	{
		System.out.println("\nImporting...\n");
		// TODO: create a class that starts a thread that monitors the progress.
	    /*
        ConsoleProgressThreadBase progress = null;
        try {
            Environment.getInstance().registerCloseable(indexer);

            progress = new ConsoleProgressBarThread(System.out, indexer);
            progress.setShowCounter(true);
            progress.start();

            indexer.process();

            progress.waitToStop();
            System.out.println("\n\n Import completed.");

            Environment.getInstance().closeAndRemove(indexer);
        } catch (Exception e) {
            if (null != progress) {
                progress.waitToStop();
            }
            Environment.getInstance().getReportHandler().severe(e);
        }
        */
    }

}
