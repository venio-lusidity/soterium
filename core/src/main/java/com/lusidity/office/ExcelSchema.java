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

package com.lusidity.office;

import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.office.CsvColumn;
import com.lusidity.framework.text.StringX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class ExcelSchema
{
	private final JsonData schema;
	private int actualSize=0;

	// Constructors
	public ExcelSchema(JsonData schema)
	{
		super();
		this.schema=schema;
	}

	public ExcelSchema(JsonData schema, int actualSize)
	{
		super();
		this.schema=schema;
		this.actualSize=actualSize;
	}

	private void add(LinkedHashMap<String, Object> results, JsonData working, String parentKey)
	{
		if((null!=working) && (!working.isEmpty()) && working.isJSONObject())
		{
			List<CsvColumn> columns = new ArrayList<>();

			List<String> keys=new ArrayList<>(working.keys());
			int on = keys.size();
			for (String key : keys)
			{
				JsonData item=working.getFromPath(key);
				Integer idx = item.getInteger("idx");
				idx = (null==idx)? on : idx;
				CsvColumn column = new CsvColumn(key, idx, item);
				columns.add(column);
				on++;
			}

			columns.sort(new Comparator<CsvColumn>()
			{
				// Overrides
				@Override
				public int compare(CsvColumn o1, CsvColumn o2)
				{
					return o1.getIdx().compareTo(o2.getIdx());
				}
			});


			for (CsvColumn column: columns)
			{
				String key = column.getKey();
				JsonData item = column.getItem();
				if (StringX.startsWith(key, "/"))
				{
					JsonData children=working.getFromPath(key, "schema");
					this.add(results, children, key);
				}
				else
				{
					String finalKey=StringX.isBlank(parentKey) ? key : String.format("%s::%s", parentKey, key);
					String title = item.getString("s_label");
					if(StringX.isBlank(title)){
						title = finalKey;
					}
					results.put(finalKey, title);
				}
			}
		}
	}

	// Getters and setters
	public JsonData getSchema()
	{
		return this.schema;
	}

	public int getSize()
	{
		//noinspection NestedConditionalExpression
		return (this.actualSize>0) ? this.actualSize : (((null!=this.schema) && (this.schema.isValid())) ? this.schema.length() : 0);
	}

	public int getActualSize()
	{
		return this.actualSize;
	}

	public void setActualSize(int actualSize)
	{
		this.actualSize=actualSize;
	}

	public LinkedHashMap<String, Object> getCSVSchema()
	{
		LinkedHashMap<String, Object> results=new LinkedHashMap<>();
		this.add(results, this.schema, null);
		results.put("/system/primitives/uri_value/identifiers::value", "identifiers");
		return results;
	}
}
