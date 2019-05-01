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

package com.lusidity.data;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.jobs.BaseJob;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ClassHelper
{
	// Methods
	public static <T extends DataVertex> T as(DataVertex vertex, Class<T> cls)
	{
		T result=null;
		if (!ClassX.isKindOf(vertex.getClass(), cls))
		{
			try
			{
				Constructor constructor=cls.getConstructor(JsonData.class, Object.class);
				//noinspection unchecked
				result=(T) constructor.newInstance(vertex.getVertexData(), vertex.getIndexId());
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		else
		{
			//noinspection unchecked
			result=(T) vertex;
		}

		return result;
	}

	public static String getPropertyKey(Class<? extends DataVertex> cls, Field field)
	{
		return ClassHelper.getPropertyKey(cls, field.getName());
	}

	public static String getPropertyKey(Class<? extends DataVertex> cls, String fieldName)
	{
		String result=ClassHelper.getClassKey(cls);
		return String.format("%s/%s", result, fieldName);
	}

	public static String getClassKey(Class<? extends DataVertex> cls)
	{
		String clsName=cls.getName();
		clsName=StringX.stripStart(clsName, "com.lusidity");
		clsName=StringX.stripStart(clsName, "domains");
		String result=null;
		StringBuffer sb=new StringBuffer();
		String[] parts=StringX.split(clsName, '.');
		if (null!=parts)
		{
			for (String part : parts)
			{
				if (!StringX.isBlank(part))
				{
					if (sb.length()>0)
					{
						sb.append("/");
					}
					sb.append(StringX.insertStringAtCapitol(part, "_").toLowerCase());
				}
			}
			result=sb.toString();
			if (!StringX.startsWith(result, "/"))
			{
				result=String.format("/%s", result);
			}
		}
		return result;
	}

	public static AtSchemaClass getSchema(Class<? extends DataVertex> cls)
	{
		return cls.getAnnotation(AtSchemaClass.class);
	}

	public static AtSchemaProperty getPropertySchema(Class<? extends DataVertex> cls, String property)
	{
		AtSchemaProperty result=null;
		String fieldName=StringX.getLast(property, "/");
		Field field=PropertyHelper.getField(cls, fieldName);
		if (null!=field)
		{
			result=field.getAnnotation(AtSchemaProperty.class);
		}
		return result;
	}

	/**
	 * Transform the String into a vertex.
	 *
	 * @param cls  The type of vertex being transformed to.
	 * @param data The data to step3 from.
	 * @return A vertex transformed from cache or null.
	 */
	public static <T extends DataVertex> T fromCacheableRepresentation(Class<? extends DataVertex> cls, String data)
	{
		T result=null;
		JsonData item=new JsonData(data);
		if (item.isJSONObject())
		{
			result=ClassHelper.fromCacheableRepresentation(cls, item);
		}
		return result;
	}

	/**
	 * Transform the JsonData into a vertex.
	 *
	 * @param cls  The type of vertex being transformed to.
	 * @param data The data to step3 from.
	 * @return A vertex transformed from cache or null.
	 */
	public static <T extends DataVertex> T fromCacheableRepresentation(Class<? extends DataVertex> cls, JsonData data)
	{
		T result=null;
		try
		{
			IDataStore dataStore=Environment.getInstance().getDataStore();
			result=ClassHelper.as(data, cls);

			if (Environment.getInstance().isOpened())
			{
				result.transformCachedEdges(data);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	public static <T extends DataVertex> T as(JsonData item, Class<? extends DataVertex> cls)
	{
		T result=null;
		if ((null!=item) && item.isJSONObject())
		{
			try
			{
				Class<? extends DataVertex> finalClass=ClassHelper.getFinalClass(item, cls);
				if (null!=finalClass)
				{
					Constructor constructor=finalClass.getConstructor(JsonData.class, Object.class);
					//noinspection unchecked
					result=(T) constructor.newInstance(item, null);
				}
				else
				{
					//noinspection ThrowCaughtLocally
					throw new ApplicationException("Could not determine the class.");
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	public static Class<? extends DataVertex> getFinalClass(JsonData item, Class<? extends DataVertex> cls)
	{
		Class<? extends DataVertex> result=cls;
		String vertexType=item.getString("vertexType");
		if (!StringX.isBlank(vertexType))
		{
			result=Environment.getInstance().getApolloVertexType(vertexType);
		}
		return result;
	}

	public static <T extends ApolloVertex> Collection<T> sortCreatedWhen(Collection<? extends ApolloVertex> collection, BaseQueryBuilder.Sort sort)
	{
		Collection<T> results=new ArrayList<>();
		if ((null!=collection) && (null!=sort))
		{
			List<ApolloVertex> list=new ArrayList<>(collection);
			Collections.sort(list, new Comparator<ApolloVertex>()
			{
// Overrides
				@Override
				public int compare(ApolloVertex o1, ApolloVertex o2)
				{
					@SuppressWarnings("OverlyStrongTypeCast")
					int result=o1.fetchCreatedWhen().getValue().compareTo(o2.fetchCreatedWhen().getValue());
					if (sort==BaseQueryBuilder.Sort.desc)
					{
						result*=-1;
					}
					return result;
				}
			});
			for (ApolloVertex vertex : list)
			{
				//noinspection unchecked
				results.add((T) vertex);
			}
		}
		return results;
	}

	/**
	 * Evaluate the properties of the two vertices for changes.  Will not check complex properties.
	 *
	 * @param before The vertex value before the change.
	 * @param after  The vertex value after the change.
	 * @return A JsonData object the lists the deltas.  {"the property changed": {"before": "the value before", "after": "the value after"}}
	 */
	public static JsonData getDeltas(DataVertex before, DataVertex after)
	{
		JsonData result=JsonData.createObject();
		if(null!=before)
		{
			Map<String, Method> methods=PropertyHelper.getAllMethods(before.getClass());
			Collection<String> processed=new ArrayList<>();
			for (Map.Entry<String, Method> entry : methods.entrySet())
			{
				Method method=entry.getValue();
				if (ClassX.isKindOf(method.getReturnType(), KeyData.class))
				{
					try
					{
						String name=method.getName();
						if ((StringX.startsWith(name, "fetch") && !processed.contains(name)))
						{
							processed.add(name);
							method.setAccessible(true);
							Object b = method.invoke(before);
							Object a = (null!=after) ? method.invoke(after) : null;
							//noinspection ChainOfInstanceofChecks
							if(b instanceof KeyData){
								KeyData bK = (KeyData)b;
								KeyData aK = (null!=a) ? (KeyData)a : null;
								Object valueBefore = bK.getValue();
								Object valueAfter = (null!=aK) ? aK.getValue():null;
								if (!Objects.equals(valueBefore, valueAfter))
								{
									JsonData item=JsonData.createObject();
									item.put("before", (null==valueBefore) ? "null" : valueBefore);
									item.put("after", (null==valueAfter) ? "null" : valueAfter);
									result.put(bK.getKeyName(), item);
								}
							}
							else if(b instanceof KeyDataCollection){
								// internal collections are not handled here yet.
							}
						}
					}
					catch (Exception ex){}
				}
			}
		}
		return result;
	}

	public static String getTitle(Class<? extends BaseJob> cls)
	{
		String result=cls.getSimpleName();
		return StringX.insertSpaceAtCapitol(result);
	}

	public static String getDomainKey(Class<? extends DataVertex> cls)
	{
		return (null==cls) ? null : String.format("/domains/%s", ClassHelper.getIndexKey(cls));
	}

	public static String getIndexKey(Class<? extends DataVertex> cls)
	{
		String result=null;
		if (null!=cls)
		{
			String type=ClassHelper.getClassKey(cls);
			result=ClassHelper.getIndexKey(type);
		}
		return result;
	}

	public static String getIndexKey(String vertexType)
	{
		return ClassHelper.getIndexKey(vertexType, "_");
	}

	private static String getIndexKey(String vertexType, String delimiter)
	{
		String result=vertexType;
		if (!StringX.isBlank(result))
		{
			result=StringX.stripStart(result, "/");
			result=StringX.stripEnd(result, "/");
			result=StringX.replace(result, "/", delimiter);
			result=result.toLowerCase();
		}
		return result;
	}
}
