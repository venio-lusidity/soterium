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

import com.lusidity.framework.annotations.AtCell;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public
abstract class DocumentItem
{
	private String[] values;

	// Constructors
	public DocumentItem(String[] values)
	{
		super();
		this.values = values;
		this.load();
	}

	public void load(){
		Collection<Field> fields = new ArrayList<>();
		for (Class<?> c = this.getClass(); c != null; c = c.getSuperclass())
		{
			Field[] values = c.getDeclaredFields();
			Collections.addAll(fields, values);
		}
		if(this.values.length > 0){
			for (Field field : fields)
			{
				try
				{
					field.setAccessible(true);
					AtCell atCell=field.getAnnotation(AtCell.class);
					if (null!=atCell)
					{
						int idx = atCell.index();
						if(this.values.length >idx)
						{
							Object value=this.values[idx];
							//empty values are created by CSVReader as empty strings - " "
							if (null!=value)
							{
								if(!value.toString().isEmpty()){
									if(StringX.endsWithIgnoreCase(field.getType().getName(), "org.joda.time.DateTime")) {
										//Common.getTypeFor throws exception for DateTime in this format: Jul 22, 2016 18:53:09 EDT
										value = this.getJodaDateTime(value.toString());
									} else{
										value=Common.getTypeFor(value, field.getType());
									}
									if((null!=value))
									{
										field.set(this, value);
									}
								}

							}
						}
					}
				}
				catch (Exception ex)
				{
					ReportHandler.getInstance().severe(ex);
				}
			}
		}

	}

	private
	DateTime getJodaDateTime(String value) {
		DateTime result = null;
		String pattern = "MMM dd, yyyy HH:mm:ss z";
		try
		{
			Date date = new SimpleDateFormat(pattern, Locale.ENGLISH).parse(value);
			result = new DateTime(date.getTime());
		}
		catch (ParseException e)
		{
			ReportHandler.getInstance().severe(e);
		}
		return result;
	}

	public abstract Object clean(Object value);
}
