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
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.json.JsonData;
import com.lusidity.index.interfaces.IReIndexer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class ReIndexCommand extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    private static final String OPTION_INDEX_STORE = "store";
    private static final String OPTION_INDEX_PARTITION = "partition";
    private static final String OPTIONS_MAPPING = "step1";

    public ReIndexCommand(){
        super();
    }

    @Override
    public
    void execute(Console console, CommandLine commandLine)
            throws Exception
    {
        String s = commandLine.getOptionValue(ReIndexCommand.OPTION_INDEX_STORE);
        String p = commandLine.getOptionValue(ReIndexCommand.OPTION_INDEX_PARTITION);
        JsonData map = new JsonData(commandLine.getOptionValue(ReIndexCommand.OPTIONS_MAPPING));
        Class<? extends DataVertex> store = BaseDomain.getDomainType(s);
        Class<? extends DataVertex> partition = BaseDomain.getDomainType(p);

        IReIndexer indexRepair = Environment.getInstance().getIndexStore().getReIndexer(store, partition, map);
        this.process(indexRepair);
    }

    private void process(IReIndexer indexer){
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
        return "reIndex";
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
        options.addOption(ReIndexCommand.OPTION_INDEX_PARTITION, true, "The index partition name.");
        options.addOption(ReIndexCommand.OPTION_INDEX_STORE, true, "The index name");
        options.addOption(ReIndexCommand.OPTIONS_MAPPING, true, "The index mapping in JSON.");
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
