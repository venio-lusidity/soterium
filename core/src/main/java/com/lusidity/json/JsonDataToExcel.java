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

package com.lusidity.json;

import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.office.ExcelX;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class JsonDataToExcel
{
	private final JsonData data;
	private final String className;

// Constructors
	public JsonDataToExcel(JsonData data, String className)
	{
		super();
		this.data=data;
		this.className = className;
	}

	/**
	 * Create an Excel file given an array of objects, currently only works with flat objects.
	 *
	 * @param outputFolder The folder to write the file to.
	 * @return An Excel file or null.
	 */
	public FileInfo saveAsExcel(File outputFolder)
		throws ApplicationException, IOException
	{
		return this.saveAsExcel(String.valueOf(DateTime.now().getMillis()), outputFolder);
	}

	/**
	 * Create an Excel file given an array of objects, currently only works with flat objects.
	 *
	 * @param fileName     The file name with no extension.
	 * @param outputFolder The folder to write the file to.
	 * @return An Excel file or null.
	 */
	public FileInfo saveAsExcel(String fileName, File outputFolder)
		throws ApplicationException, IOException
	{
		FileInfo result;
		if (this.data.isJSONArray())
		{
			Collection<String> keys=new ArrayList<>();
			for (Object o : this.data)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=new JsonData(o);
					CollectionX.addAllIfUnique(keys, item.keys());
				}
			}

			JsonData schema=JsonData.createObject();
			for (String key : keys)
			{
				String v=StringX.getMainTitle(key);
				v=StringX.startsWith(v, "/") ? StringX.getLast(v, "/") : v;
				v=StringX.insertSpaceAtCapitol(v);
				v=StringX.toTitle(v);
				schema.put(key, JsonData.createObject().put("s_label", v));
			}

			result=this.saveAsExcel(fileName, outputFolder, schema);
		}
		else
		{
			throw new ApplicationException("The JsonData object must be an array of Json objects.");
		}
		return result;
	}

	/**
	 * Create an Excel file given an array of objects, currently only works with flat objects.
	 *
	 * @param fileName     The file name with no extension.
	 * @param outputFolder The folder to write the file to.
	 * @param schema       A JsonData object that defines the properties and labels to use.
	 * @return An Excel file or null.
	 */
	public FileInfo saveAsExcel(String fileName, File outputFolder, JsonData schema)
		throws ApplicationException, IOException
	{
		ExcelX excelX = new ExcelX();
		return excelX.fromJsonData(fileName, outputFolder, schema, this.data, this.className, true);
	}

	/**
	 * Create an Excel file given an array of objects, currently only works with flat objects.
	 *
	 * @param outputFolder The folder to write the file to.
	 * @param prefix       Prefix for timestamp.
	 * @return An Excel file or null.
	 */
	public FileInfo saveAsExcel(File outputFolder, String prefix)
		throws ApplicationException, IOException
	{
		return this.saveAsExcel(String.format("%s_%s", prefix, String.valueOf(DateTime.now().getMillis())), outputFolder);
	}

	/**
	 * Create an Excel file given an array of objects, currently only works with flat objects.
	 *
	 * @param outputFolder The folder to write the file to.
	 * @param prefix       Prefix for timestamp.
	 * @return An Excel file or null.
	 */
	public FileInfo saveAsExcel(File outputFolder, String prefix, JsonData schema)
		throws ApplicationException, IOException
	{
		return this.saveAsExcel(String.format("%s_%s", prefix, String.valueOf(DateTime.now().getMillis())), outputFolder, schema);
	}

	/**
	 * Create an Excel file given an array of objects, currently only works with flat objects.
	 *
	 * @param file The folder or file to to.
	 * @param schema       A JsonData object that defines the properties and labels to use.
	 * @return An Excel file or null.
	 */
	public FileInfo saveAsExcel(File file, JsonData schema)
		throws ApplicationException, IOException
	{
		FileInfo result;
		if((StringX.endsWithIgnoreCase(file.getName(), "xlsx") || StringX.endsWithIgnoreCase(file.getName(), "csv"))){
			result = this.saveAsExcel(file.getName(), file.getParentFile(), schema);
		}
		else{
			result = this.saveAsExcel(String.valueOf(DateTime.now().getMillis()), file, schema);
		}
		return result;
	}

}
