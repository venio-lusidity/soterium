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

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementAttributes;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.system.primitives.Text;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.helper.PropertyHelper;

import java.lang.reflect.Field;
import java.util.Collection;

public class JsonTransformer
{

	// Methods
	public static JsonData extract(DataVertex dataVertex, boolean storing, String... languages)
	{
		Stopwatch sw=new Stopwatch();
		sw.elapsed();

		JsonData result=JsonData.createObject();

		Collection<Field> fields=PropertyHelper.getAllFields(dataVertex.getClass());
		for (Field field : fields)
		{
			if (PropertyHelper.isUsable(field))
			{
				try
				{
					field.setAccessible(true);
					String key=field.getName();
					Object value=field.get(dataVertex);

					if (!ClassX.isKindOf(field.getType(), ElementAttributes.class) || ClassX.isKindOf(field.getType(), KeyDataCollection.class))
					{
						if (ClassX.isKindOf(field.getType(), KeyDataCollection.class))
						{
							KeyDataCollection keyDataCollection =(KeyDataCollection) field.get(dataVertex);
							value = dataVertex.getVertexData().getFromPath(keyDataCollection.getKeyData().getKeyName());
							key = keyDataCollection.getKeyData().getKeyName();
						}
						if (!StringX.isBlank(key) && (null!=value))
						{
							result.put(key, value);
						}
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex.toString());
				}
			}
		}
		sw.stop();
		Environment.getInstance().getReportHandler().timed("JsonTransformer.extract took %s", sw.elapsed());

		return result;
	}

	@SuppressWarnings("unused")
	public static JsonData extractAttributes(DataVertex dataVertex)
	{
		JsonData jsonData=JsonData.createObject();

		Stopwatch sw=new Stopwatch();
		sw.start();

		Collection<Field> fields=PropertyHelper.getAllFields(dataVertex.getClass());
		for (Field field : fields)
		{
			if (PropertyHelper.isUsable(field))
			{
				try
				{
					Class<?> fieldType=field.getType();
					boolean build=!ClassX.isKindOf(fieldType, ElementEdges.class);

					if (build)
					{
						field.setAccessible(true);
						String key=field.getName();

						Object value=field.get(dataVertex);
						if (!StringX.isBlank(key) && (null!=value))
						{
							if (ClassX.isKindOf(fieldType, Text.class))
							{
								//For some reason if you don't do this the JsonData toString fails.
								value=value.toString();
							}
							jsonData.put(key, value);
						}
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex.toString());
				}
			}
		}

		sw.stop();
		Environment.getInstance().getReportHandler().timed("JsonTransformer.extractAttributes took %s", sw.elapsed());

		return jsonData;
	}

	public static void transform(JsonData jsonData, DataVertex dataVertex)
	{
		Stopwatch sw=new Stopwatch();
		sw.start();

		Collection<String> keys=jsonData.keys();
		Collection<Field> fields=PropertyHelper.getAllFields(dataVertex.getClass());

		for (String key : keys)
		{
			try
			{
				Field field=JsonTransformer.getField(dataVertex.getClass(), key, fields);

				if ((null!=field) && !ClassX.isKindOf(field.getType(), ElementAttributes.class))
				{
					Class<?> expectedType=field.getType();
					Object o=jsonData.getObjectFromPath(key);
					if (((null!=o) && field.getType().equals(expectedType)))
					{
						Object value=Common.getTypeFor(o, expectedType);
						field.setAccessible(true);
						field.set(dataVertex, value);
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}

		sw.stop();
		Environment.getInstance().getReportHandler().timed("JsonTransformer.step3 took %s", sw.elapsed());
	}

	private static Field getField(Class<? extends DataVertex> cls, String key, Collection<Field> fields)
	{
		Field result=null;

		for (Field field : fields)
		{
			String fieldName=field.getName();

			boolean match=fieldName.equals(key);
			if (!match)
			{
				AtSchemaProperty schemaProperty=field.getAnnotation(AtSchemaProperty.class);
				if (null!=schemaProperty)
				{
					String propertyKey=ClassHelper.getPropertyKey(schemaProperty.expectedType(), fieldName);
					match=StringX.equals(key, propertyKey);
				}
			}

			if (match)
			{
				result=field;
				break;
			}
		}

		return ((null!=result) && PropertyHelper.isUsable(result)) ? result : null;
	}

}
