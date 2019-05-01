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
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.console.Console;
import com.lusidity.console.commands.BaseCommand;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.framework.json.JsonData;
import com.lusidity.json.JsonDataToExcel;
import com.lusidity.services.helper.QueryHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;

@SuppressWarnings("UnusedDeclaration")
public
class QueryCommand
	extends BaseCommand
{
// -------------------------- OTHER METHODS --------------------------

    public static final String QUERY_OPTION = "query";
	public static final String STORE_OPTION = "store";
	public static final String PARTITION_OPTION = "partition";
	public static final String START_OPTION = "start";
	public static final String LIMIT_OPTION = "limit";
	public static final String EXTENSION_TYPE_OPTION = "ext";

	public QueryCommand(){
		super();
	}

	public enum FileTypes{
		json,
		xls
	}

	@SuppressWarnings("UnqualifiedStaticUsage")
	@Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
        try
        {
	        JsonData query = new JsonData(commandLine.getOptionValue(QueryCommand.QUERY_OPTION));
	        if(!query.isValid()){
		        this.showHelp(console, commandLine);
		        Environment.getInstance().getReportHandler().fine("The query is not in the expected format.");
	        }
	        else
	        {
		        String store = commandLine.getOptionValue(QueryCommand.STORE_OPTION);
		        String partitionType = commandLine.getOptionValue(QueryCommand.PARTITION_OPTION);
		        int start = 0;
		        if(commandLine.hasOption(START_OPTION))
		        {
			        start = Integer.parseInt(commandLine.getOptionValue(QueryCommand.START_OPTION));
		        }
		        int limit = 0;
		        if(commandLine.hasOption(LIMIT_OPTION))
		        {
			        limit = Integer.parseInt(commandLine.getOptionValue(QueryCommand.LIMIT_OPTION));
		        }
		        QueryCommand.FileTypes fileType = Enum.valueOf(QueryCommand.FileTypes.class, commandLine.getOptionValue(QueryCommand.EXTENSION_TYPE_OPTION));


		        JsonData data = JsonData.createObject();
		        data.put("domain", store);
		        data.put("type", partitionType);
		        data.put("native", query);

		        QueryHelper queryHelper = new QueryHelper(new SystemCredentials(), ScopedConfiguration.getInstance().isEnabled(), data, start, limit);
		        JsonData results = queryHelper.execute(null);

		        File file = null;

		        if (null!=results)
		        {

			        int hits = results.getInteger("hits");
			        String stats = String.format("next: %d, limit: %d, hits: %d, took: %s, max score: %d",
			                                     results.getInteger("next"),
			                                     results.getInteger("limit"),
			                                     hits,
			                                     results.getString("took"),
			                                     results.getInteger("maxScore"));
			        Environment.getInstance().getReportHandler().say(stats);

			        if (hits>0)
			        {
				        switch (fileType)
				        {
					        case json:
						        file = results.save(Environment.getInstance().getConfig().getTempDir(), false);
						        break;
					        case xls:
						        JsonData items = results.getFromPath("results");

						        if (null!=items)
						        {
							        Class<? extends DataVertex> cls = BaseDomain.getDomainType(store);
							        JsonDataToExcel jsonDataToExcel = new JsonDataToExcel(items, cls.getSimpleName());
							        boolean processed = false;
							        if(null!=cls){
								        File sf = Environment.getSchemaFor(cls);
								        if(sf.exists()){
									        JsonData schema = new JsonData(sf);
									        if(schema.isJSONObject())
									        {
										        FileInfo fileInfo = jsonDataToExcel.saveAsExcel(Environment.getInstance().getConfig().getTempDir(), schema);
										        file = fileInfo.getFile();
										        processed = true;
									        }
								        }
							        }

							        if(!processed)
							        {
								        FileInfo fileInfo = jsonDataToExcel.saveAsExcel(Environment.getInstance().getConfig().getTempDir());
								        file = fileInfo.getFile();
							        }
						        }
						        break;
				        }
			        }
			        else
			        {
				        Environment.getInstance().getReportHandler().say("No results found.");
			        }
		        }
		        else
		        {
			        Environment.getInstance().getReportHandler().say("No results found.");
		        }

		        if (null!=file)
		        {
			        Environment.getInstance().getReportHandler().say("The files was saved at %s", file.getAbsolutePath());
		        }
		        else
		        {
			        Environment.getInstance().getReportHandler().say("The file was not saved.");
		        }
	        }
        }
        catch (Exception ex){
	        this.showHelp(console, commandLine);
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
		return "query";
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

        Option queryOption = new Option(QueryCommand.QUERY_OPTION, true, "An ElasticSearch query, must be wrapped in single quotes.");
	    queryOption.setType(String.class);
	    queryOption.setRequired(true);
	    queryOption.setValueSeparator('\'');
        options.addOption(queryOption);

	    Option storeOption = new Option(QueryCommand.STORE_OPTION, true, "A class path to the type of object returned.");
	    storeOption.setType(String.class);
	    storeOption.setRequired(true);
	    options.addOption(storeOption);

	    Option partOption = new Option(QueryCommand.PARTITION_OPTION, true, "A class path to the partition type that will be searched using the query.");
	    partOption.setType(String.class);
	    partOption.setRequired(true);
	    options.addOption(partOption);

	    Option startOption = new Option(QueryCommand.START_OPTION, true, "The paging start point.");
	    startOption.setType(Integer.class);
	    startOption.setRequired(false);
	    options.addOption(startOption);

	    Option limitOption = new Option(QueryCommand.LIMIT_OPTION, true, "The maximum number of objects returned.");
	    limitOption.setType(Integer.class);
	    limitOption.setRequired(false);
	    options.addOption(limitOption);

	    Option extOption = new Option(QueryCommand.EXTENSION_TYPE_OPTION, true, "The WriteConsoleJob file type.");
	    extOption.setType(QueryCommand.FileTypes.class);
	    extOption.setRequired(true);
	    options.addOption(extOption);

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
