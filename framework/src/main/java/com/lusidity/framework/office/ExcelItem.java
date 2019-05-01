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
import org.apache.poi.hssf.usermodel.HSSFRow;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public
class ExcelItem
{
	private int cols = 0;
	private int rowIndex = -1;
	private HSSFRow row = null;
	private int rows = 0;

	// Constructors
	public ExcelItem(int rowIndex, HSSFRow row, int rows, int cols){
		super();
		this.rowIndex = rowIndex;
		this.row = row;
		this.rows = rows;
		this.cols = cols;
		this.load();
	}

	public void load(){
		Collection<Field> fields = new ArrayList<>();
		for (Class<?> c = this.getClass(); c != null; c = c.getSuperclass())
		{
			Field[] values = c.getDeclaredFields();
			Collections.addAll(fields, values);
		}

		for(Field field: fields){
			try
			{
				field.setAccessible(true);
				AtCell atCell=field.getAnnotation(AtCell.class);
				if (null!=atCell)
				{
					Object value=this.row.getCell(atCell.index());
					if (null!=value)
					{
						value=Common.getTypeFor(value, field.getType());
						field.set(this, value);
					}
				}
			}
			catch (Exception ex){
				ReportHandler.getInstance().severe(ex);
			}
		}
	}

	// Getters and setters
	public int getCols()
	{
		return this.cols;
	}

	public
	int getRowIndex()
	{
		return this.rowIndex;
	}

	public
	HSSFRow getRow()
	{
		return this.row;
	}

	public
	int getRows()
	{
		return this.rows;
	}
}
