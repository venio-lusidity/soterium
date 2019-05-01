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

package com.lusidity.helper;

import com.lusidity.Environment;
import com.lusidity.annotations.AtIndexedField;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementAttributes;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.office.ExcelSchema;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public class PropertyHelper
{

// Methods
	/**
	 * Get all Property annotations of a class including super classes.
	 *
	 * @param cls The class to find Property annotations in.
	 * @return All Property annotations of a class including super classes.
	 */
	public static Collection<AtIndexedField> getClassesAnnotatedWithProperty(Class cls)
	{
		Collection<AtIndexedField> results=new ArrayList<>();
		AtIndexedField[] properties=(AtIndexedField[]) cls.getAnnotationsByType(AtIndexedField.class);
		if (null!=properties)
		{
			for (AtIndexedField indexedField : properties)
			{
				results.add(indexedField);
			}
		}
		Class parent=cls.getSuperclass();
		if (ClassX.isKindOf(parent, ApolloVertex.class))
		{
			Collection<AtIndexedField> superProperties=PropertyHelper.getClassesAnnotatedWithProperty(parent);
			for (AtIndexedField superIndexedField : superProperties)
			{
				boolean add=true;
				for (AtIndexedField indexedField : results)
				{
					if (Objects.equals(indexedField.key(), superIndexedField.key()))
					{
						add=false;
						break;
					}
				}
				if (add)
				{
					results.add(superIndexedField);
				}
			}
		}
		return results;
	}

	/**
	 * Get a filed by name.
	 *
	 * @param cls       The class the field is part of.
	 * @param fieldName The name of the filed to find.
	 * @return A field.
	 */
	public static Field getField(Class<?> cls, String fieldName)
	{
		return PropertyHelper.getField(PropertyHelper.getAllFields(cls), fieldName);
	}

	/**
	 * Get a filed by name.
	 *
	 * @param fields    A collection of fields.
	 * @param fieldName The name of the filed to find.
	 * @return A field.
	 */
	public static Field getField(Collection<Field> fields, String fieldName)
	{
		Field result=null;
		for (Field field : fields)
		{
			if (field.getName().equals(fieldName))
			{
				result=field;
				break;
			}
		}
		return result;
	}

	/**
	 * Get all Fields of a class including super classes.
	 *
	 * @param cls A class.
	 * @return All Fields of a class including super classes.
	 */
	public static Collection<Field> getAllFields(Class<?> cls)
	{
		Collection<Field> results=new ArrayList<>();
		for (Class<?> c=cls; c!=null; c=c.getSuperclass())
		{
			Field[] fields=c.getDeclaredFields();
			Collections.addAll(results, fields);
		}

		return results;
	}

	/**
	 * Get all Fields of a class including super classes.
	 *
	 * @param cls A class.
	 * @return All Methods of a class including super classes.
	 */
	public static Map<String, Method> getAllMethods(Class<?> cls)
	{
		Map<String, Method> results = new HashMap<>();
		for (Class<?> c=cls; c!=null; c=c.getSuperclass())
		{
			Method[] methods=c.getDeclaredMethods();
			for(Method method: methods){
				if(StringX.startsWith(method.getName(), "fetch") &&  !results.containsKey(method.getName())){
					results.put(method.getName(), method);
				}
			}
		}
		return results;
	}

	public static Class getAnnotatedPropertyBaseType(Class cls, AtIndexedField indexedField)
	{
		Collection<Field> fields=PropertyHelper.getAllFields(cls);
		Field field=PropertyHelper.getField(fields, indexedField.key());
		Class result=field.getType();
		if (ClassX.isKindOf(result, ElementAttributes.class))
		{
			AtSchemaProperty schemaProperty=field.getAnnotation(AtSchemaProperty.class);
			if (null!=schemaProperty)
			{
				result=schemaProperty.expectedType();
			}
		}
		return result;
	}

	/*
	 * Invoke the getter method of the field specified returning the value.
	 */
	public static Object getPropertyValue(DataVertex vertex, Field field, Object... args)
	{
		return PropertyHelper.getPropertyValue(vertex, field.getName(), args);
	}

	/*
	* Invoke the getter method of the field specified returning the value.
	*/
	private static Object getPropertyValue(DataVertex vertex, String fieldName, Object... args)
	{
		Object result=null;
		try
		{
			if (args.length==0)
			{
				result=new PropertyDescriptor(fieldName, vertex.getClass()).getReadMethod().invoke(vertex);
			}
			else
			{
				result=new PropertyDescriptor(fieldName, vertex.getClass()).getReadMethod().invoke(vertex, args);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	public static HashMap<Field, Annotation> getAllFieldsAnnotatedWith(Class<? extends DataVertex> cls, Class<? extends Annotation> annotationType)
	{
		HashMap<Field, Annotation> results=new HashMap<>();
		try
		{
			Collection<Field> fields=PropertyHelper.getAllFields(cls);
			for (Field field : fields)
			{
				Annotation annotation=field.getAnnotation(annotationType);
				if (null!=annotation)
				{
					results.put(field, annotation);
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return results;
	}

	public static String getFromUrl(URL url)
	{
		StringBuilder result=new StringBuilder();
		String u=StringX.getFirst(url.toString(), "?");
		String[] parts=StringX.split(u, "/");
		if (null!=parts)
		{
			boolean found=false;
			for (String part : parts)
			{
				if (found)
				{
					result.append(String.format("/%s", part));
				}
				else
				{
					found=StringX.equalsIgnoreCase(part, "properties");
				}
			}
		}
		return result.toString();
	}

	public static boolean isUsable(Field field)
	{
		return !Modifier.isStatic(field.getModifiers())
		       && !Modifier.isTransient(field.getModifiers());
	}

	public static String getSubPropertyName(String propertyName, Class subType, String subPropertyName)
	{
		String result = null;
		if(ClassX.isKindOf(subType, DataVertex.class)){
			result = String.format("%s/%s.%s", ClassHelper.getClassKey((Class<? extends DataVertex>)subType), propertyName, subPropertyName);
		}
		else{
			result = String.format("%s.%s", propertyName, subPropertyName);
		}
		return result;
	}

	public static ExcelSchema getExcelSchema(DataVertex vertex)
	{
		JsonData result = PropertyHelper.getExcelSchema(vertex, vertex.getClass(), 0);
		return new ExcelSchema(result);
	}

	private static JsonData getExcelSchema(DataVertex vertex, Class domain, int on)
	{
		JsonData result = JsonData.createObject();
		Collection<Field> fields = PropertyHelper.getAllFields(domain);
		for (Field field : fields) {
			if (PropertyHelper.isUsable(field)) {
				try {
					Class cls = field.getType();
					if (ClassX.isKindOf(cls, KeyData.class)){
						String title = StringX.insertStringAtCapitol(field.getName(), " ");
						title = title.toLowerCase();
						if(StringX.equalsIgnoreCase(field.getName(), "id")){
							result.put("lid", JsonData.createObject().put("s_label", "lid").put("idx", on));
						}
						else
						{
							result.put(field.getName(), JsonData.createObject().put("s_label", title).put("idx", on));
						}
						on++;
					}
					else if((null!=vertex) && ClassX.isKindOf(cls, KeyDataCollection.class)){
						try
						{
							field.setAccessible(true);
							@SuppressWarnings({
								"unchecked",
								"ZeroLengthArrayAllocation"
							})
							Method m=domain.getDeclaredMethod(String.format("fetch%s", StringX.toFirstUpper(field.getName())), new Class[]{});
							Object o = m.invoke(vertex);
							KeyDataCollection kdc=(KeyDataCollection) o;
							KeyData kd=kdc.getKeyData();
							String propertyKey=kd.getKeyName();

							JsonData schema=PropertyHelper.getExcelSchema(null, kd.getFieldType(), on);
							result.put(propertyKey, JsonData.createObject().put("s_label", propertyKey).put("schema", schema));
							on+=schema.length();
						}
						catch (Exception ignored){}
					}
				} catch (Exception ignored) {
				}
			}
		}
		return result;
	}
}
