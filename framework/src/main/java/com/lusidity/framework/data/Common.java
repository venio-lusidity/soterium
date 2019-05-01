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

package com.lusidity.framework.data;

import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class Common {
    /**
     * Direction of link.
     */
    public static enum Direction
    {
        IN,
        OUT,
        BOTH
    }

	// Methods
	@SuppressWarnings("IfStatementWithTooManyBranches")
	public static Object getTypeFor(Object o, Class expectedType)
	{
		Object result=o;

		try
		{
			if(null!=result)
			{
				if (expectedType.equals(InetAddress.class))
				{
					result=InetAddress.getByName(String.valueOf(o));
				}
				if (expectedType.equals(Boolean.class) || expectedType.equals(boolean.class))
				{
					result=Boolean.parseBoolean(o.toString());
				}
				else if (expectedType.equals(DateTime.class))
				{
					if (o instanceof Long)
					{
						result=new DateTime(o);
					}
					else if (o instanceof String)
					{
						result=DateTime.parse(o.toString());
					}
				}
				else if (expectedType.equals(Double.class) || expectedType.equals(double.class))
				{
					result=(StringX.isBlank(o.toString())) ? null : Double.parseDouble(o.toString());
				}
				else if (expectedType.equals(Enum.class) || ClassX.isKindOf(expectedType, Enum.class))
				{
					result=Enum.valueOf(expectedType, o.toString());
				}
				else if (expectedType.equals(Float.class) || expectedType.equals(float.class))
				{
					result=Float.parseFloat(o.toString());
				}
				else if (expectedType.equals(Integer.class) || expectedType.equals(int.class))
				{
					try
					{
						String num=o.toString();
						if (num.contains("."))
						{
							num=num.substring(0, num.indexOf('.'));
						}

						result=Integer.parseInt(num);
					}
					catch (Exception ex)
					{
						ReportHandler.getInstance().severe(ex);
					}
				}
				else if (expectedType.equals(Long.class) || expectedType.equals(long.class))
				{
					result=Long.parseLong(o.toString());
				}
				else if (expectedType.equals(URI.class))
				{
					result=URI.create(o.toString());
				}
				else if (expectedType.equals(String.class))
				{
					result=o.toString();
				}
				else if (expectedType.equals((Class.class)))
				{
					result=ClassX.getClassFromNameSafely(o.toString());
					if (null==result)
					{
						result=o;
					}
				}
				else if ((o instanceof JsonData) && (!Objects.equals(expectedType, JsonData.class)))
				{
					result=o.toString();
				}
				else if ((o instanceof String) && (Objects.equals(expectedType, JsonData.class)))
				{
					result=new JsonData(o.toString());
				}
				else if ((o instanceof JsonData) && (Objects.equals(expectedType, JsonData.class)))
				{
					result=o;
				}
				else if ((o instanceof JSONArray) && (Objects.equals(expectedType, JsonData.class)))
				{
					result=new JsonData(o);
				}
				else if ((o instanceof JSONObject) && (Objects.equals(expectedType, JsonData.class)))
				{
					result=new JsonData(o);
				}
				else if ((o instanceof UUID))
				{
					result=UUID.fromString(result.toString());
				}
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning("Could not cast the object to type %s", expectedType.getName());
		}

		return result;
	}

	public static Object getQueryValueFor(Object value, String quoteUsedIfString)
	{
		Object result=null;

		if(null!=value)
		{
			Class expectedType = value.getClass();
			if (expectedType.equals(String.class) || expectedType.equals((Class.class))
			    || expectedType.equals(DateTime.class) || expectedType.equals(URI.class)
			    || expectedType.equals(Enum.class) || ClassX.isKindOf(expectedType, Enum.class)
			    || expectedType.equals(InetAddress.class) || expectedType.equals(UUID.class))
			{
				result=String.format("%s%s%s", quoteUsedIfString, value.toString(), quoteUsedIfString);
			}
			else{
				result = value;
			}
		}

		return result;
	}
}
