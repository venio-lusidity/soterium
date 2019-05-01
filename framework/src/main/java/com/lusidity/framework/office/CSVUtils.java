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

package com.lusidity.framework.office;

import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.util.*;

@SuppressWarnings("CollectionDeclaredAsConcreteClass")
public class CSVUtils {

	private static final char DEFAULT_SEPARATOR = ',';
	private static final char DEFAULT_QUOTE = '"';
	private int errors=0;

	// Constructors
	public CSVUtils()
	{

	}

	// Methods
	public static List<String> parseLine(String cvsLine) {
		return CSVUtils.parseLine(cvsLine, CSVUtils.DEFAULT_SEPARATOR, CSVUtils.DEFAULT_QUOTE);
	}

	public static List<String> parseLine(String cvsLine, char separators, char customQuote) {
		List<String> result = new ArrayList<>();
		char quote = customQuote;
		//if empty, return!
		if (StringX.isBlank(cvsLine)) {
			return result;
		}

		if (quote == ' ') {
			quote =CSVUtils.DEFAULT_QUOTE;
		}

		if (separators == ' ') {
			separators =CSVUtils.DEFAULT_SEPARATOR;
		}

		StringBuffer curVal = new StringBuffer();
		boolean inQuotes = false;
		boolean startCollectChar = false;
		boolean doubleQuotesInColumn = false;

		char[] chars = cvsLine.toCharArray();

		for (char ch : chars) {
			if (inQuotes) {
				startCollectChar = true;
				if (ch == quote) {
					inQuotes = false;
					doubleQuotesInColumn = false;
				} else {

					//Fixed : allow "" in custom quote enclosed
					if (ch == '\"') {
						if (!doubleQuotesInColumn) {
							curVal.append(ch);
							doubleQuotesInColumn = true;
						}
					} else {
						curVal.append(ch);
					}

				}
			} else {
				if (ch == quote) {

					inQuotes = true;

					//Fixed : allow "" in empty quote enclosed
					if ((chars[0]!='"') && (quote=='\"')) {
						curVal.append('"');
					}

					//double quotes in column will hit this!
					if (startCollectChar) {
						curVal.append('"');
					}

				} else if (ch == separators) {

					result.add(curVal.toString());

					curVal = new StringBuffer();
					startCollectChar = false;

				} else if (ch == '\r') {
					//ignore LF characters
					continue;
				} else if (ch == '\n') {
					//the end, break!
					break;
				} else {
					curVal.append(ch);
				}
			}

		}

		result.add(curVal.toString());

		return result;
	}

	public static List<String> parseLine(String cvsLine, char separators)
	{
		return CSVUtils.parseLine(cvsLine, separators, CSVUtils.DEFAULT_QUOTE);
	}

	public static List<String> parseLine(Reader r) throws Exception {
		int ch = r.read();
		while (ch == 'r') {
			ch = r.read();
		}
		if (ch<0) {
			return null;
		}
		List<String> store = new ArrayList<>();
		StringBuffer curVal = new StringBuffer();
		boolean inquotes = false;
		boolean started = false;
		while (ch>=0) {
			if (inquotes) {
				started=true;
				if (ch == '"') {
					inquotes = false;
				}
				else {
					curVal.append((char)ch);
				}
			}
			else {
				if (ch == '"') {
					inquotes = true;
					if (started) {
						// if this is the second quote in a value, add a quote
						// this is for the double quote in the middle of a value
						curVal.append('"');
					}
				}
				else if (ch == ',') {
					store.add(curVal.toString());
					curVal = new StringBuffer();
					started = false;
				}
				else if (ch == 'r') {
					//ignore LF characters
				}
				else if (ch == 'n') {
					//end of a line, break out
					break;
				}
				else {
					curVal.append((char)ch);
				}
			}
			ch = r.read();
		}
		store.add(curVal.toString());
		return store;
	}

	private static Collection<String> makeKeys(LinkedHashMap<String, Object> schema)
	{
		Collection<String> results=new ArrayList<>();
		for (Map.Entry<String, Object> entry : schema.entrySet())
		{
			String key=entry.getKey();
			if (entry.getValue() instanceof LinkedHashMap)
			{
				@SuppressWarnings("unchecked")
				Collection<String> children=CSVUtils.makeKeys((LinkedHashMap<String, Object>) entry.getValue());
				for (String child : children)
				{
					results.add(String.format("%s::%s", key, child));
				}
			}
			else
			{
				results.add(key);
			}
		}

		return results;
	}

	private static String makeHeader(LinkedHashMap<String, Object> schema)
	{
		StringBuffer sb=new StringBuffer();
		if (null!=schema)
		{
			int on=0;
			for (Map.Entry<String, Object> entry : schema.entrySet())
			{
				if (on>0)
				{
					sb.append(",");
				}

				if (entry.getValue() instanceof LinkedHashMap)
				{
					//noinspection unchecked
					sb.append(CSVUtils.makeHeader((LinkedHashMap<String, Object>) entry.getValue()));
				}
				else
				{
					sb.append(entry.getValue());
				}
				on++;
			}
		}
		return sb.toString();
	}

	/**
	 * Append a row of data to a file, Not thread safe.
	 * @param schema The column names and keys.
	 * @param row The row of data.
	 * @param file The file to append to.
	 * @param headers Each string is a new line at the top of the document.
	 */
	public synchronized void append(LinkedHashMap<String, Object> schema, JsonData row, File file, String... headers) throws Exception
	{
		if(!file.exists()){
			if(null!=headers){
				for(String head: headers){
					if(!StringX.isBlank(head))
					{
						CSVUtils.write(file, head);
					}
				}
			}
			CSVUtils.write(file, String.format("Created: %s", DateTime.now().toString("dd-MMM-yyyy HH:mm:ss")));
			CSVUtils.write(file, CSVUtils.makeHeader(schema));
		}
		if(null!=row)
		{
			String content=this.makeRow(schema, row);
			if (!StringX.isBlank(content))
			{
				CSVUtils.write(file, content);
			}
		}
	}

	public void append(File file, String... rows) throws Exception
	{
		if(null!=rows){
			for(String row: rows){
				if(!StringX.isBlank(row))
				{
					CSVUtils.write(file, row);
				}
			}
		};
	}

	private static void write(File file, String content)
	{
		try (BufferedWriter writer=new BufferedWriter(new FileWriter(file, true)))
		{
			writer.write(content);
			writer.newLine();
		}
		catch (Exception ignored){}
	}

	private String makeRow(LinkedHashMap<String, Object> schema, JsonData row)
	{
		this.decodeRow(row);
		StringBuffer sb = new StringBuffer();
		Collection<String> keys = CSVUtils.makeKeys(schema);
		int on = 0;
		for(String key: keys){
			if(on>0){
				sb.append(",");
			}
			if(StringX.contains(key, "::")){
				try
				{
					StringBuilder value=new StringBuilder();
					// can only go two deep.
					String[] parts=StringX.split(key, "::");
					if ((null!=parts) && (parts.length>2))
					{
						value.append(row.getString(key));
					}
					else if(null!=parts)
					{
						String k=(parts[StringX.equals(parts[0], "/") ? 1 : 0]);
						JsonData items=row.getFromPath(k);

						if((null!=items) && items.isJSONArray())
						{
							for (Object o : items)
							{
								try
								{
									if (o instanceof JSONObject)
									{
										JsonData item=JsonData.create(o);
										String v=item.getString(parts[1]);
										if (!StringX.isBlank(v))
										{
											if (value.length()>0)
											{
												value.append("\r\n");
											}
											value.append(v);
										}
									}
									else if (null!=o)
									{
										String v=o.toString();
										if (value.length()>0)
										{
											value.append("\r\n");
										}
										value.append(v);
									}
								}
								catch (Exception ignored){
									this.errors++;
								}
							}
						}
					}

					String v=StringX.replace(value.toString(), "\"", "\"\"");
					sb.append(String.format("\"%s\"", (null==v) ? "" : v));
				}
				catch (Exception ignored){
					this.errors++;
				}
			}
			else
			{
				try
				{
					String value=row.getString(key);
					if (null!=value)
					{
						value=StringX.replace(value, "\"", "\"\"");
						sb.append(String.format("\"%s\"", value));
					}
				}catch (Exception ignored){
					this.errors++;
				}
			}
			on++;
		}
		String result = sb.toString().trim();
		if(result.length()<=schema.size()){
			result = null;
			this.errors++;
		}

		return result;
	}

	private void decodeRow(JsonData row) {
		try{
			for(String key: row.keys()){
				JsonData data = row.getFromPath(key);
				if((null!=data) && data.isJSONArray() && StringX.startsWithIgnoreCase(key,"/system/primitives/raw_string"))
				{
					for (Object o : data)
					{
						if (o instanceof JSONObject)
						{
							JsonData item=JsonData.create(o);
							this.decode(item);
						}
					}
				}
			}
		}
		catch (Exception ignored){
			this.errors++;
		}
	}
	private void decode(JsonData data){
		for(String key: data.keys()) {
			if(StringX.equalsIgnoreCase(key, "value")){
				String result = null;
				String val = data.getString(key);
				if ((val instanceof String) && StringX.isBase64Encoded(val))
				{
					result=StringX.decode((String)val);
				}
				if(null != result){
					data.update(key, result);
				}
			}
		}
	}
}
