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

package com.lusidity.reports;

import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import com.lusidity.json.JsonDataToExcel;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

public class FileReport implements IAuditReport
{
	private final String className;
// Constructors
	public FileReport(String className)
	{
		super();
		this.className = className;
	}

// Overrides
	@Override
	public JsonData report(Object... args)
	{
		return null;
	}

	@Override
	public boolean canReportOn(Class<? extends DataVertex> cls)
	{
		return ClassX.isKindOf(cls, DataVertex.class);
	}

// Methods
	public static void dumpFiles(String key)
	{
		Collection<FileInfo> fileInfos=VertexFactory.getInstance().startsWith(FileInfo.class, "title", String.format("%s_", key), 0, 100);
		for (FileInfo fileInfo : fileInfos)
		{
			try
			{
				fileInfo.delete();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
	}

	@SuppressWarnings({
		"unused",
		"OverlyComplexMethod",
		"OverlyLongMethod"
		,
		"LoopStatementThatDoesntLoop"
	})
	public FileInfo exportToExcel(DataVertex vertex, Class<? extends DataVertex> cls, Collection<DataVertex> vertices, String key)
	{
		FileInfo result=null;
		try
		{
			ApolloVertex av = (ApolloVertex)vertex;
			String title = StringX.replace(av.fetchTitle().getValue(), " ", "_" );
			String vertexId = av.fetchId().getValue();
			Collection<FileInfo> fileInfos=VertexFactory.getInstance().
				startsWith(FileInfo.class, "title", String.format("%s-%s_%s_", title , vertexId, key), 0, 10);
			for (FileInfo fileInfo : fileInfos)
			{
				result=fileInfo;
				break;
			}
			if (null==result)
			{
				Constructor constructor = cls.getConstructor();
				DataVertex dataVertex =(DataVertex) constructor.newInstance();

				JsonData schema=dataVertex.getExcelSchema(0).getSchema();
				JsonData items=JsonData.createArray();

				//noinspection ConstantConditions
				if (null!=vertices)
				{
					int on=0;
					for(DataVertex child: vertices)
					{
						JsonData item=child.toJson(false);
						items.put(item);
						on++;
					}

					result=FileReport.output(items, schema, vertexId, title, key, 0, on, this.className);
				}
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning(ex);
		}
		return result;
	}

	private static FileInfo output(JsonData items, JsonData schema, String vertexId, String title, String key, int from, int to, String className)
		throws IOException, ApplicationException
	{
		FileInfo result=null;
		String path=String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files");
		path=StringX.replace(path, "//", "/");
		File file=new File(path);
		String prefix=String.format("%s-%s_%s_%d_%d", title, vertexId, key, from, to);

		JsonDataToExcel jsonDataToExcel=new JsonDataToExcel(items, className);
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

	public boolean isLinked(Person person, DataVertex vertex, String key)
	{
		FileInfo actual=null;
		ApolloVertex av = (ApolloVertex)vertex;
		String title = StringX.replace(av.fetchTitle().getValue(), " ", "_" );
		String vertexId = av.fetchId().getValue();
		Collection<FileInfo> fileInfos=VertexFactory.getInstance().startsWith(FileInfo.class, "title", String.format("%s-%s_%s_", title, vertexId, key), 0, 10);
		Collection<FileInfo> delete=new ArrayList<>();
		for (FileInfo fileInfo : fileInfos)
		{
			if ((null!=fileInfo.getFile()) && !fileInfo.exists())
			{
				delete.add(fileInfo);
			}
			else
			{
				actual=fileInfo;
				break;
			}
		}
		if(null != actual){
			//compare to see if day is before today, not 24 hours ago
			DateTime created = actual.fetchCreatedWhen().getValue();
			int day =created.getDayOfYear();
			DateTime now = DateTime.now();
			if(day < now.getDayOfYear()){
				delete.add(actual);
				actual = null;
			}
		}

		try
		{
			for (FileInfo fileInfo : delete)
			{
				fileInfo.delete();
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning(ex);
		}
		return ((null!=person) && (null!=actual)) && person.getBlobFiles().contains(actual);
	}
}
