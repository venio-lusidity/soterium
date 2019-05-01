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
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.Stopwatch;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MergeCommand extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

	// Fields
	public static final String FROM_OPTION = "from";
    public static final String TO_OPTION = "to";

	// Constructors
	public MergeCommand(){
		super();
	}

	// Overrides
	@Override
	public void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		System.out.println(String.format("\n\nMerge started at %s.", stopwatch.getStartedWhen().toString("dd-MMM-yyyy HH:mm:ss")));

		String fromStr=commandLine.getOptionValue(MergeCommand.FROM_OPTION);
		String toStr=commandLine.getOptionValue(MergeCommand.TO_OPTION);

		DataVertex fVertex = VertexFactory.getInstance().get(fromStr);
		DataVertex tVertex = VertexFactory.getInstance().get(toStr);

		fVertex.mergeTo(tVertex, true);

		stopwatch.stop();
		System.out.println(String.format("Merge took %s", stopwatch.elapsed().toString()));
	}

	@Override
	public JsonData execute(JsonData params, BasePrincipal principal)
	{
		JsonData result=JsonData.createObject();
		if (params.hasKey(MergeCommand.FROM_OPTION) && params.hasKey(MergeCommand.TO_OPTION))
		{
			String fromStr=params.getString(MergeCommand.FROM_OPTION);
			String toStr=params.getString(MergeCommand.TO_OPTION);

			Stopwatch stopwatch=new Stopwatch();
			stopwatch.start();
			result.put("started", stopwatch.getStartedWhen());

			try
			{
				DataVertex fVertex=VertexFactory.getInstance().get(fromStr);
				DataVertex tVertex=VertexFactory.getInstance().get(toStr);

				fVertex.mergeTo(tVertex, true);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
				result.put("error", "Failed to merge the objects.");
			}

			stopwatch.stop();
			result.put("stopped", stopwatch.getStoppedWhen());
			result.put("took", stopwatch.elapsed().toString());
		}
		else
		{
			result.put("error", "Parameters missing, expects");
		}
		return result;
	}

    @Override
    public
    String getDescription()
    {
        return "Merge two vertices.";
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
        return "merge";
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

        Option from=new Option(MergeCommand.FROM_OPTION, true, "");
        from.setType(String.class);
        from.setRequired(true);
        options.addOption(from);

        Option to=new Option(MergeCommand.TO_OPTION, true, "");
        to.setType(String.class);
        to.setRequired(true);
        options.addOption(to);

        return options;
    }

    @Override
    public boolean isWebEnabled() {
        return true;
    }
}
