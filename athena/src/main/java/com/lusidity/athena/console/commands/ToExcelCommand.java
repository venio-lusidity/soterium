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
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.office.CSVUtils;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.json.JsonDataToExcel;
import com.lusidity.office.ExcelSchema;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

@SuppressWarnings("UnusedDeclaration")
public
class ToExcelCommand
	extends BaseCommand
{

	// Fields
	public static final String STORE_OPTION="store";
	public static final String PARTITION_OPTION="partition";
	public static final String START_OPTION="start";
	public static final String BATCH_OPTION="batch";
	public static final String SORT_OPTION="sort";
	private static final String MAX_OPTION="max";

// -------------------------- OTHER METHODS --------------------------

	// Constructors
	public ToExcelCommand()
	{
		super();
	}

	// Overrides
	@Override
	public void execute(Console console, CommandLine commandLine)
		throws Exception
	{
		boolean process=true;
		String storeStr=commandLine.getOptionValue(QueryCommand.STORE_OPTION);

		if (StringX.isBlank(storeStr))
		{
			Environment.getInstance().getReportHandler().severe("The store is required");
			process=false;
		}

		String partitionStr=storeStr;
		if (commandLine.hasOption(ToExcelCommand.PARTITION_OPTION))
		{
			partitionStr=commandLine.getOptionValue(QueryCommand.PARTITION_OPTION);
		}

		@SuppressWarnings("unchecked")
		Class<? extends DataVertex> store=(Class<? extends DataVertex>) Class.forName(storeStr);
		@SuppressWarnings("unchecked")
		Class<? extends DataVertex> partition=(Class<? extends DataVertex>) Class.forName(partitionStr);

		if ((null==store) || (null==partition))
		{
			Environment.getInstance().getReportHandler().severe("Unknown store or partition.");
			process=false;
		}

		if (process)
		{

			int max=0;
			if (commandLine.hasOption(ToExcelCommand.MAX_OPTION))
			{
				int value=Integer.parseInt(commandLine.getOptionValue(ToExcelCommand.MAX_OPTION));
				max=(value>0) ? value : max;
			}

			int batch=10000;
			if (commandLine.hasOption(ToExcelCommand.BATCH_OPTION))
			{
				int value=Integer.parseInt(commandLine.getOptionValue(ToExcelCommand.BATCH_OPTION));
				batch=(value>0) ? value : batch;
			}

			int start=0;
			if (commandLine.hasOption(ToExcelCommand.START_OPTION))
			{
				int value=Integer.parseInt(commandLine.getOptionValue(ToExcelCommand.START_OPTION));
				start=(value>0) ? value : start;
			}

			String sort="title";
			if (commandLine.hasOption(ToExcelCommand.SORT_OPTION))
			{
				sort=commandLine.getOptionValue(ToExcelCommand.SORT_OPTION);
				if (StringX.isBlank(sort))
				{
					sort="title";
				}
			}
			ToExcelCommand.handle(store, partition, null, start, batch, max, sort);
		}
	}


	@SuppressWarnings("OverlyLongMethod")
	static
	FileInfo handle(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, String fileName, int start, int batch, int max, String sort)
	{
		FileInfo result = null;
		try
		{
			Stopwatch stopwatch=new Stopwatch();
			stopwatch.start();

			int limit=10;
			int on = 1;
			int last = 1;
			BaseQueryBuilder queryBuilder=
				Environment.getInstance().getIndexStore().getQueryBuilder(store, partition, start, limit);
			queryBuilder.sort(StringX.isBlank(sort) ? "createdWhen": sort, BaseQueryBuilder.Sort.asc);
			queryBuilder.setIncludeDuplicates(true);
			queryBuilder.matchAll();
			queryBuilder.setApi(BaseQueryBuilder.API._search);

			String path=String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files");
			path=StringX.replace(path, "//", "/");
			DateTime now = DateTime.now();
			File file=new File(path, String.format("%s_%d_%d_%d_%d_%d_%d.csv", ClassHelper.getIndexKey(store), now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour(), now.getSecondOfMinute()));
			result = new FileInfo(file);
			QueryResults queryResults = queryBuilder.execute();
			Environment.getInstance().getReportHandler().say("Getting records for store: %s partition: %s.", queryResults.getHits(), store.getSimpleName(), partition.getSimpleName());

			LinkedHashMap<String, Object> schema = null;
			CSVUtils csvUtils = new CSVUtils();
			boolean stop = false;
			do{
				String sc = Environment.getInstance().getSecurityClassification();
				for(IQueryResult queryResult: queryResults){
					DataVertex vertex = queryResult.getVertex();
					if(null==schema){
						ExcelSchema excelSchema = vertex.getExcelSchema(0);
						schema = ((null==excelSchema) ? null : excelSchema.getCSVSchema());
					}
					JsonData row=vertex.toJson(false);
					if(!row.isEmpty()){
						csvUtils.append(schema, row, file, StringX.isBlank(sc) ? "" : sc, StringX.isBlank(sc) ? "" : String.format("This document in its entirety is %s and should be treated accordingly.", sc));
					}
					on++;
				}
				/*if(!StringX.isBlank(sc))
				{
					csvUtils.append(file, sc);
				}*/
				if(!stop)
				{
					queryBuilder.setStart(queryResults.getNext());
					queryResults=queryBuilder.execute();
				}
			}
			while(!stop && !queryResults.isEmpty());

			stopwatch.stop();
			Environment.getInstance().getReportHandler().say("FileInfo creation took %s for store: %s partition: %s.", stopwatch.elapsed().toString(), store.getSimpleName(), partition.getSimpleName());
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return result;
	}

	@Override
	public JsonData execute(JsonData params, BasePrincipal principal)
	{
		JsonData result=JsonData.createObject();

		try
		{
			//noinspection unchecked
			Class<? extends DataVertex> store=(Class<? extends DataVertex>) params.getClassFromName(QueryCommand.STORE_OPTION);

			String fileName=params.getString("fileName");
			if (!StringX.isBlank(fileName))
			{
				fileName=StringX.replace(fileName, " ", "_").toLowerCase();
			}

			Class<? extends DataVertex> partition=store;
			if (params.hasKey(ToExcelCommand.PARTITION_OPTION))
			{
				//noinspection unchecked
				partition=(Class<? extends DataVertex>) params.getClassFromName(QueryCommand.PARTITION_OPTION);
			}

			boolean process=true;

			if ((null==store) || (null==partition))
			{
				Environment.getInstance().getReportHandler().severe("Unknown store or partition.");
				process=false;
			}

			if (process)
			{
				int max=0;
				if (params.hasKey(ToExcelCommand.MAX_OPTION))
				{
					Integer value=params.getInteger(ToExcelCommand.MAX_OPTION);
					max=((null!=value) && (value>0)) ? value : max;
				}

				int batch=10000;
				if (params.hasKey(ToExcelCommand.BATCH_OPTION))
				{
					Integer value=params.getInteger(ToExcelCommand.BATCH_OPTION);
					batch=((null!=value) && (value>0)) ? value : batch;
				}

				int start=0;
				if (params.hasKey(ToExcelCommand.START_OPTION))
				{
					Integer value=params.getInteger(ToExcelCommand.START_OPTION);
					start=((null!=value) && (value>0)) ? value : start;
				}

				String sort="title";
				if (params.hasKey(ToExcelCommand.SORT_OPTION))
				{
					sort=params.getString(ToExcelCommand.SORT_OPTION);
					if (StringX.isBlank(sort))
					{
						sort="title";
					}
				}
				FileInfo fileInfo=ToExcelCommand.handle(store, partition, fileName, start, batch, max, sort);
				if (fileInfo.getFile().exists())
				{
					String url=fileInfo.getWebUrl("files");
					result.put("download", url);
					result.put("success", fileInfo.getFile().exists());
					if ((null!=principal) && (ClassX.isKindOf(principal, Person.class)))
					{
						((Person) principal).getBlobFiles().add(fileInfo);
					}
				}
			}
			else
			{
				result.put("error", "Invalid class names.");
			}
		}
		catch (Exception ignored)
		{
			result.put("error", "Something went wrong.");
		}
		return result;
	}

	@Override
	public
	String getDescription()
	{
		return "Stop listening.";
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
		return "toExcel";
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

		Option max=new Option(ToExcelCommand.MAX_OPTION, true, "The max number of Assets to get, 0 or null means all.");
		max.setRequired(false);
		max.setType(Integer.class);
		options.addOption(max);

		Option storeOption=new Option(ToExcelCommand.STORE_OPTION, true, "A class path to the type of object returned.");
		storeOption.setType(String.class);
		storeOption.setRequired(true);
		options.addOption(storeOption);

		Option partOption=new Option(ToExcelCommand.PARTITION_OPTION, true,
			"A class path to the partition type that will be searched using the query, if option is not used will default to the store option value."
		);
		partOption.setType(String.class);
		partOption.setRequired(false);
		options.addOption(partOption);

		Option startOption=new Option(ToExcelCommand.START_OPTION, true, "The paging start point. Default starts at 0.");
		startOption.setType(Integer.class);
		startOption.setRequired(false);
		options.addOption(startOption);

		Option limitOption=new Option(ToExcelCommand.BATCH_OPTION, true, "The maximum number of objects returned. Default 10000.");
		limitOption.setType(Integer.class);
		limitOption.setRequired(false);
		options.addOption(limitOption);

		Option sort=new Option(ToExcelCommand.SORT_OPTION, true, "The field to sort on, default title.");
		sort.setType(String.class);
		sort.setRequired(false);
		options.addOption(sort);

		return options;
	}

	@Override
	public
	boolean isWebEnabled()
	{
		return true;
	}

	private static FileInfo output(JsonData items, JsonData schema, Class<? extends DataVertex> store, String fileName, Class<? extends DataVertex> partition, int from, int to)
		throws IOException, ApplicationException
	{
		FileInfo result=null;
		String path=String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files");
		path=StringX.replace(path, "//", "/");
		File file=new File(path);
		String prefix=String.format("%s_%s_%d_%d", store.getSimpleName().toLowerCase(), partition.getSimpleName().toLowerCase(), from, to);
		if (!StringX.isBlank(fileName))
		{
			prefix=String.format("%s_%d_%d", fileName.toLowerCase(), from, to);
		}

		JsonDataToExcel jsonDataToExcel=new JsonDataToExcel(items, store.getSimpleName());
		if (null==schema)
		{
			result=jsonDataToExcel.saveAsExcel(file, prefix);
		}
		else
		{
			result=jsonDataToExcel.saveAsExcel(file, prefix, schema);
		}

		return result;
	}
}
