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

package com.lusidity.data.handler;

import com.lusidity.Environment;
import com.lusidity.data.field.IKeyDataHandler;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataTransformed;
import com.lusidity.framework.text.StringX;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

public class KeyDataHandlerStringToClass implements IKeyDataHandler
{
	// Constructors
	public KeyDataHandlerStringToClass(){
		super();
	}

	// Overrides
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod"
		,
		"OverlyNestedMethod"
	})
	@Override
	public KeyDataTransformed handleSetterBefore(Object newValue, KeyData keyData)
	{
		Object result = null;
		if(newValue instanceof String){
			Collection<Class> results=new ArrayList<>();
			if(StringX.contains(newValue.toString(), ",")){
				String[] parts=StringX.split(newValue.toString(), ",");
				if (null!=parts)
				{
					for (String part : parts)
					{
						try
						{
							Class cls = Class.forName(part.trim());
							if(null!=cls){
								results.add(cls);
							}
						}
						catch (Exception ex)
						{
							Environment.getInstance().getReportHandler().severe(ex);
						}
					}
				}
			}
			else{
				try
				{
					Class cls = Class.forName(newValue.toString().trim());
					if(null!=cls){
						results.add(cls);
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
			result = results;
		}
		else if((null!=newValue) && newValue.getClass().isArray()){
			Collection<Class> results = new ArrayList<>();
			int length = Array.getLength(newValue);
			for (int i = 0; i < length; i ++)
			{
				Object obj=Array.get(newValue, i);
				try
				{
					Class cls;
					if (obj instanceof String)
					{
						cls=Class.forName(obj.toString().trim());
					}
					else
					{
						cls =(Class) obj;
					}
					if (null!=cls)
					{
						results.add(cls);
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
			result = results;
		}
		else{
			result = newValue;
		}
		return new KeyDataTransformed(result, true);
	}

	@Override
	public void handleSetterAfter(Object value, KeyData keyData)
	{
	}

	@Override
	public KeyDataTransformed handleGetterAfter(Object value, KeyData keyData)
	{
		return new KeyDataTransformed(value, true);
	}

	@Override
	public KeyDataTransformed getDefaultValue(Object value, KeyData keyData)
	{
		return new KeyDataTransformed(value, true);
	}
}
