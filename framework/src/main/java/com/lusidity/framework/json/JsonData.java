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

package com.lusidity.framework.json;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.internet.http.IPNetX;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.java.ComparatorX;
import com.lusidity.framework.java.ObjectX;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.xml.LazyXmlNode;
import com.lusidity.framework.xml.XmlX;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Protocol;
import org.restlet.ext.json.JsonRepresentation;
import org.w3c.dom.Document;

import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLXML;
import java.sql.Time;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings({
	"ChainOfInstanceofChecks",
	"UnusedDeclaration",
	"ConditionalExpressionWithNegatedCondition"
	,
	"NestedConditionalExpression"
	,
	"OverlyComplexBooleanExpression"
	,
	"OverlyComplexClass"
	,
	"OverlyComplexMethod"
	,
	"OverlyLongMethod"
	,
	"OverlyNestedMethod"
})
public class JsonData implements Iterable<Object>, Serializable, Cloneable
{
// ------------------------------ FIELDS ------------------------------

	class StringComparator implements Comparator<String>
	{
		@Override
		public int compare(String a, String b)
		{
			int result;
			if (StringX.equals(a, b))
			{
				result=0;
			}
			else if (StringX.isBlank(a))
			{
				result=-1;
			}
			else if (StringX.isBlank(b))
			{
				result=1;
			}
			else
			{
				result=a.compareTo(b);
			}
			return result;
		}
	}
	private static final String KEY_ADDED = "JsonData_AlreadyAdded";
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private Object json = null;

// --------------------------- CONSTRUCTORS ---------------------------
private String error="";

	// Constructors
	public JsonData(Object json)
	{
		super();
		this.load(json);
	}

	/**
	 * This constructor makes an empty JSONObject.
	 * If you want an array use JsonData.createArray();
	 */
	public JsonData()
	{
		this.load(new JSONObject());
	}

	public JsonData(File file)
	{
		super();
		Object o=JsonEngine.getFromFile(file);
		this.load(o);
	}

	public JsonData(LazyXmlNode node)
	{
		super();
		Object o=JsonEngine.fromXml(node);
		this.load(o);
		this.fixKeys();
	}

	public JsonData(Document document)
	{
		super();
		Object o=JsonEngine.fromXml(document);
		this.load(o);
	}

	// Overrides
	@Override
	public Iterator<Object> iterator()
	{
		return new JsonIterator(this.isJSONArray() ? this.toJSONArray() : null);
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public JsonData clone()
		throws CloneNotSupportedException
	{
		return new JsonData(this.toString());
	}

	@Override
	public String toString()
	{
		String result=null;
		Object obj=null;
		if (this.isJSONObject())
		{
			obj=this.toJSONObject();
		}
		else if (this.isJSONArray())
		{
			obj=this.toJSONArray();
		}
		if (null!=obj)
		{
			result=obj.toString();
		}
		return result;
	}

	// Methods
	public static JsonData makeLabel(String label, Object value)
	{
		JsonData result=JsonData.createObject();
		result.put("value", value);
		result.put("label", label);
		return result;
	}

	public static JsonData makeLabel(String label, Object value, String description)
	{
		JsonData result=JsonData.createObject();
		result.put("value", value);
		result.put("label", label);
		result.put("desc", description);
		return result;
	}

	public static JsonData split(String str, String delimiter)
	{
		throw new NotImplementedException("This should treat a string as a delimited key value pair.");
	}

	public static void readLines(File file, JsonDataLineHandler jsonDataLineHandler)
		throws ApplicationException
	{
		FileX.readLines(file, jsonDataLineHandler);
	}

	public static JsonData createObject()
	{
		JSONObject jsonObject = new JSONObject();
		return new JsonData(jsonObject);
	}

	public static JsonData createArray()
	{
		JSONArray jsonArray = new JSONArray();
		return new JsonData(jsonArray);
	}

	public static JsonData getFromUri(URI uri, String... headers)
	{
		String str = HttpClientX.getString(uri, headers);
		JsonData result = new JsonData("");
		if (JsonEngine.isValid(str))
		{
			try
			{
				result = new JsonData(str);
			} catch (Exception ignored)
			{
			}
		}

		return result;
	}

	public static String getSafeKey(String key)
	{
		@SuppressWarnings("NonConstantStringShouldBeStringBuffer") String result = key;
		if (!StringX.isBlank(result))
		{
			try
			{
				if (StringX.startsWith(key, "ns") && StringX.contains(key, ":"))
				{
					String[] parts=StringX.split(key, ":");
					if ((null!=parts) && (parts.length>1))
					{
						result="";
						for (int i=1; i<parts.length; i++)
						{
							result=String.format("%s%s", result, parts[i]);
						}
					}
				}
				result=result.trim();
				result=Pattern.compile("[^0-9a-zA-Z\\./]+").matcher(result).replaceAll("_");
				result=StringX.stripStart(result, "_");
				result=StringX.stripEnd(result, "_");
			}
			catch (Exception ignored){}
		}
		return result;
	}

	@SuppressWarnings("UseOfObsoleteDateTimeApi")
	public static String getGetter(Class javaType)
	{
		String result = null;
		try
		{
			JsonData item = new JsonData();
			if (Objects.equals(javaType, Enum.class))
			{
				result = "getEnum";
			}
			else if (Objects.equals(javaType, URI.class))
			{
				result = "getUri";
			}
			else if (Objects.equals(javaType, URL.class))
			{
				result = "getUrl";
			}
			else if ((Objects.equals(javaType, long.class)) || (Objects.equals(javaType, Long.class)))
			{
				result = "getLong";
			}
			else if ((Objects.equals(javaType, byte[].class)) || (Objects.equals(javaType, Byte[].class)))
			{
				result = "getByteArray";
			}
			else if (Objects.equals(javaType, boolean.class))
			{
				result = "getByte";
			}
			else if (Objects.equals(javaType, String.class))
			{
				result = "getString";
			}
			else if ((Objects.equals(javaType, DateTime.class)) || (Objects.equals(javaType, Date.class)))
			{
				result = "getDateTime";
			}
			else if (Objects.equals(javaType, BigDecimal.class))
			{
				result = "getBigDecimal";
			}
			else if ((Objects.equals(javaType, double.class)) || (Objects.equals(javaType, Double.class)))
			{
				result = "getDouble";
			}
			else if ((Objects.equals(javaType, int.class)) || (Objects.equals(javaType, Integer.class)))
			{
				result = "getInteger";
			}
			else if ((Objects.equals(javaType, float.class)) || (Objects.equals(javaType, Float.class)))
			{
				result = "getFloat";
			}
			else if ((Objects.equals(javaType, short.class)) || (Objects.equals(javaType, Short.class)))
			{
				result = "getShort";
			}
			else if (Objects.equals(javaType, Time.class))
			{
				result = "getDateTime";
			}
			else if (Objects.equals(javaType, SQLXML.class))
			{
				result = "getXML";
			}
		} catch (Exception ignored)
		{
		}
		return result;
	}

	public static JsonData create(Object o)
	{
		return new JsonData(o);
	}

	private static JsonData
	fixAllKeys(JsonData jsonData)
	{
		JsonData result=jsonData.isJSONArray() ? JsonData.createArray() : JsonData.createObject();

		if (jsonData.isJSONArray())
		{
			for (Object o : jsonData)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=JsonData.fixAllKeys(new JsonData(o));
					result.put(item);
				}
				else
				{
					result.put(o);
				}
			}
		}
		else if (jsonData.isJSONObject())
		{
			Collection<String> keys=jsonData.keys();
			for (String key : keys)
			{
				String property=JsonData.getSafeKey(key);
				Object value=jsonData.getObjectFromPath(key);
				if (value instanceof JsonData)
				{
					result.put(property, JsonData.fixAllKeys((JsonData) value));
				}
				else
				{
					result.put(property, value);
				}
			}
		}

		return result;
	}

	/**
	 * Only works with Arrays.
	 *
	 * @param obj The object to insert into the beginning of the array.
	 */
	public void prepend(Object obj)
	{
		if (this.isJSONArray())
		{
			JsonData temp=JsonData.createArray();
			temp.put(obj);
			for (Object o : this)
			{
				temp.put(o);
			}
			this.json=temp.json;
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void append(File file, int total)
		throws Exception
	{
		if (!file.getParentFile().exists())
		{
			file.getParentFile().mkdirs();
		}
		if (!file.exists())
		{
			this.put("totalItems", total);
		}
		try (BufferedWriter writer=new BufferedWriter(new FileWriter(file, true)))
		{
			writer.write(this.toString());
			writer.newLine();
		}
		catch (Exception ignored)
		{
		}
	}

	public boolean getBoolean(boolean defaultValue, String... keys)
	{
		boolean result=defaultValue;
		String v=this.getString(keys);
		if (StringX.equalsAnyIgnoreCase(v, "true", "false"))
		{
			result=this.getBoolean(keys);
		}
		return result;
	}

	/**
	 * Get the string of the last property in the path. If the property is not a string it will return null.
	 *
	 * @param keys The path to the value prop1:prop2:prop_value.
	 * @return If the property is not a string it will return null. If any of the properties are an array the first
	 * value of
	 * that array
	 * will be used to continue to search for the expected value.
	 */
	public String getString(String... keys)
	{
		String result=null;
		if (null!=keys)
		{
			String path=this.getPath(keys);
			Object o=JsonEngine.getFromPath(this.json, path);
			if ((null!=o) && !(o instanceof JsonData) && !(o instanceof JSONObject) && !(o instanceof JSONArray))
			{
				result=o.toString().trim();
				if (XmlX.isNullValue(result))
				{
					result=null;
				}
				else if (StringX.equalsIgnoreCase(result, "null"))
				{
					result=null;
				}
			}
		}
		return result;
	}

	/**
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return true or false, default false.
	 */

	public Boolean getBoolean(String... keys)
	{
		Boolean result=false;
		String path=this.getPath(keys);
		Object o=JsonEngine.getFromPath(this.json, path);
		if (o instanceof String)
		{
			try
			{
				result=Boolean.parseBoolean(o.toString().trim());
			}
			catch (Exception ignored)
			{
				result=null;
			}
		}
		else if (o instanceof Boolean)
		{
			result=(Boolean) o;
		}
		return result;
	}

	private String getPath(String... keys)
	{
		StringBuilder result=new StringBuilder();
		if (null!=keys)
		{
			for (String key : keys)
			{
				if (result.length()>0)
				{
					result.append("::");
				}
				result.append(key);
			}
		}
		return result.toString();
	}

	/**
	 * Merge an array of objects with this array.
	 *
	 * @param array A JsonData Array.
	 */
	public void merge(JsonData array)
	{
		if ((null!=array) && array.isJSONArray())
		{
			for (Object o : array)
			{
				this.put(o);
			}
		}
	}

	/**
	 * Works with a JSONObject or JSONArray.
	 * @param key The key to check.
	 * @param expected The value to find.
	 * @return true if found.
	 */
	public
	boolean contains(String key, Object expected)
	{
		boolean result = false;
		if(this.isJSONArray()){
			for(Object actual: this){
				if(actual instanceof JSONObject){
					JsonData item = new JsonData(actual);
					result = item.contains(key, expected);
				}
				else{
					result = Objects.equals(actual, expected);
				}
				if(result){
					break;
				}
			}
		}
		else{
			Object actual = this.getObjectFromPath(key);
			result = Objects.equals(actual, expected);
		}
		return result;
	}

	/**
	 * Works only with a JSONArray.
	 * @param expected The value to find.
	 * @return true if found.
	 */
	public
	boolean contains(Object expected)
	{
		boolean result = false;
		if(this.isJSONArray()){
			result = this.contains(null, expected);
		}
		return result;
	}

	public
	void update(String key, Object o)
	{
		this.remove(key);
		this.put(key, o);
	}

	public Iterator<String> iteratorKeys()
	{
		return new JsonKeysIterator(this.isJSONObject() ? this : null);
	}

	public void fixKeys()
	{
		JsonData result;
		if (this.isJSONObject())
		{
			result = JsonData.fixAllKeys(this);
			this.load(result.toJSONObject());
		}
		else
		{
			result = JsonData.createArray();
			for (Object o : this)
			{
				if (o instanceof JSONObject)
				{
					JsonData item = JsonData.fixAllKeys(new JsonData(o));
					result.put(item);
				}
			}

			this.load(result.toJSONArray());
		}
	}

	/**
	 * Get the DateTime of the last property in the path. If the property is not an DateTime it will return null.
	 *
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an DateTime it will return null. If any of the properties are an array the first
	 * value of
	 * that
	 * array will be used to continue to search for the expected value.
	 */
	public DateTime getDateTime(String... keys)
	{
		DateTime result = null;
		String path = this.getPath(keys);
		Object o = JsonEngine.getFromPath(this.json, path);
		if (o instanceof String)
		{
			try
			{
				result = JsonEngine.parseDateTime(o.toString().trim());
			} catch (Exception ignored)
			{
				try
				{
					result = DateTime.parse(o.toString().trim());
				} catch (Exception ignore)
				{
					result = null;
				}
			}
		}
		else if (o instanceof DateTime)
		{
			result = (DateTime) o;
		}
		return result;
	}

	/**
	 * Get the Double of the last property in the path. If the property is not an Double it will return null.
	 *
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an Double it will return null. If any of the properties are an array the first
	 * value of that
	 * array will be used to continue to search for the expected value.
	 */
	public Double getDouble(String... keys)
	{
		Double result = null;
		Object o = this.getString(keys);
		if (null!=o)
		{
			try
			{
				result = Double.parseDouble(o.toString());
			} catch (Exception ignored)
			{
				result = null;
			}
		}
		return result;
	}

	/**
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return A float or null.
	 */
	public Float getFloat(String... keys)
	{
		Float result = null;
		String path = this.getPath(keys);
		Object o = this.getString(keys);
		if (null!=o)
		{
			try
			{
				result = Float.parseFloat(o.toString());
			} catch (Exception ignored)
			{
				result = null;
			}
		}
		return result;
	}

	/**
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return A long or null.
	 */
	public Long getLong(String... keys)
	{
		Long result = null;
		Object o = this.getString(keys);
		if (null!=o)
		{
			try
			{
				result = Long.parseLong(o.toString());
			}
			catch (Exception ignored)
			{
			}
		}
		return result;
	}

	/**
	 * Get the Integer of the last property in the path. If the property is not an Integer it will return null.
	 *
	 * @param defaultValue The value returned if the value is null or the value is not in range.
	 * @param min          minimum value of the result returned.
	 * @param max          maximum value of the result returned.
	 * @param keys         The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an Integer it will return null. If any of the properties are an array the first
	 * value of
	 * that
	 * array will be used to continue to search for the expected value.
	 */
	public Integer getInteger(int defaultValue, int min, int max, String... keys)
	{
		Integer result=this.getInteger(keys);
		if ((null==result) || ((result<min) || (result>max))){
			result = defaultValue;
		}
		return result;
	}

	/**
	 * Get the Integer of the last property in the path. If the property is not an Integer it will return null.
	 *
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an Integer it will return null. If any of the properties are an array the first
	 * value of
	 * that
	 * array will be used to continue to search for the expected value.
	 */
	public Integer getInteger(String... keys)
	{
		Integer result = null;
		Object o = this.getString(keys);
		if (null!=o)
		{
			try
			{
				result = Integer.parseInt(o.toString());
			} catch (Exception ignored)
			{
				result = null;
			}
		}
		return result;
	}

	/**
	 * Get the Integer of the last property in the path. If the property is not an Integer it will return null.
	 *
	 * @param defaultValue The value returned if the value is null.
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an Integer it will return null. If any of the properties are an array the first
	 * value of
	 * that
	 * array will be used to continue to search for the expected value.
	 */
	public Integer getInteger(int defaultValue, String... keys)
	{
		Integer result=this.getInteger(keys);
		if (null==result)
		{
			result=defaultValue;
		}
		return result;
	}

	/**
	 * Get the Port. If port is out of range it will return the default port and if default port is out of range it will return null.
	 *
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an Integer it will return null. If any of the properties are an array the first
	 * value of
	 * that
	 * array will be used to continue to search for the expected value.
	 */
	public Integer getIpPort(int defaultPort, String... keys)
	{
		Integer result = this.getIpPort(keys);
		if((null==result) && IPNetX.isValidPort(defaultPort)){
			result = defaultPort;
		}
		return result;
	}

	/**
	 * Get the Port. If port is out of range it will return null.
	 *
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an Integer it will return null. If any of the properties are an array the first
	 * value of
	 * that
	 * array will be used to continue to search for the expected value.
	 */
	public Integer getIpPort(String... keys)
	{
		Integer result = null;
		Object o = this.getString(keys);
		if (null!=o)
		{
			try
			{
				result = Integer.parseInt(o.toString());
			} catch (Exception ignored)
			{
				result = null;
			}
		}

		if(!IPNetX.isValidPort(result)){
			result = null;
		}
		return result;
	}

	public Integer getInteger(int min, int max, String... keys)
	{
		Integer result = this.getInteger(keys);
		if((null==result) || (result<min)){
			result = min;
		}
		if((max>0) && (result>max)){
			result = max;
		}
		return result;
	}

	/**
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return A URL or null;
	 */
	public URL getUrl(String... keys)
	{
		URL result = null;
		URI uri = this.getUri(keys);
		if (null!=uri)
		{
			try
			{
				result = uri.toURL();
			} catch (MalformedURLException ignored)
			{
			}
		}
		return result;
	}

	/**
	 * Get the URI of the last property in the path. If the property is not an URI it will return null.
	 *
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return If the property is not an URI it will return null. If any of the properties are an array the first
	 * value of that array will be used to continue to search for the expected value.
	 */
	public URI getUri(String... keys)
	{
		URI result = null;
		String o = this.getString(keys);
		if (!StringX.isBlank(o))
		{
			try
			{
				result = URI.create(o.trim());
			} catch (Exception ignored)
			{
				result = null;
			}
		}
		return result;
	}

	public Short getShort(String... keys)
	{
		Short result = null;
		try
		{
			String path = this.getPath(keys);
			String value = this.getString(path);
			if (!StringX.isBlank(value))
			{
				result = Short.parseShort(value);
			}
		} catch (Exception ignored)
		{
		}
		return result;
	}

	public String getString(Enum path)
	{
		return this.getString(path.toString());
	}

	public Collection<String> getStringCollection(String... keys)
	{
		Collection<String> result = new ArrayList<>();
		JsonData items = this.getFromPath(keys);
		if (items.isJSONArray())
		{
			for (Object o : items)
			{
				if (o instanceof String)
				{
					result.add(o.toString());
				}
			}
		}
		return result;
	}

	public JsonData getFromPath(String... keys)
	{
		JsonData result = null;
		if (null!=keys)
		{
			String path = this.getPath(keys);
			if (!StringX.isBlank(path))
			{
				Object o = JsonEngine.getFromPath(this.json, path);
				if (null!=o)
				{
					try
					{
						result = new JsonData(o);
					} catch (Exception ignored)
					{
					}
				}
			}
		}
		return result;
	}

	public LazyXmlNode getXML(String... keys)
	{
		LazyXmlNode result = null;
		try
		{
			String value = this.getString(keys);
			if (!StringX.isBlank(value))
			{
				result = LazyXmlNode.load(value, false);
			}
		} catch (Exception ignore)
		{
		}
		return result;
	}

	public JsonData getFromPath(Enum key)
	{
		return this.getFromPath(key.toString());
	}

	/**
	 * @param index The index of the object to get.
	 * @param keys  The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 *              This should be a path to an array within this object.
	 * @return Found object or null.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	public Object getFromPath(int index, String... keys)
	{
		Object result = null;
		JsonData jsonData = this.getFromPath(keys);
		if (jsonData.isJSONArray() && (index>-1))
		{
			result = jsonData.at(index);
		}
		return result;
	}

	public Object at(int index)
	{
		Object result = null;
		if (this.isJSONArray())
		{
			try
			{
				result = this.toJSONArray().get(index);
			} catch (Exception ignored)
			{
			}
		}
		return result;
	}

	/**
	 * Does the specified key have a blank or null value or not exist?
	 *
	 * @param key Property key to check.
	 * @return true if the specified property is (effectively) blank.
	 */
	public boolean isBlank(String key)
	{
		return JsonEngine.isBlank(this.toJSONObject(), key);
	}

	public Object toObject()
	{
		return !this.isValid() ? null : (this.isJSONArray() ? this.toJSONArray() : this.toJSONObject());
	}

	public JsonData put(Enum key, Object value)
	{
		return this.put(key.toString(), value);
	}

	/**
	 * Put a value into the underlying JSON object.
	 * Null values are not excluded.
	 *
	 * @param key   Key to put.
	 * @param value Value to put.
	 */
	@SuppressWarnings("MethodWithMultipleReturnPoints")
	public JsonData put(String key, Object value)
	{
		return this.put(key, value, false);
	}

	public synchronized JsonData put(String key, Object value, boolean preserveKey)
	{
		// normally we do not allow null values to be set in the json
		// however some data stores do not allow the removing of a property so you have to allow properties to be set as null.
		String fKey = key;
		if (!preserveKey)
		{
			fKey=JsonData.getSafeKey(fKey);
		}
		if (!StringX.isBlank(fKey))
		{
			@SuppressWarnings("ConstantConditions")
			Object objectValue = (null==value) ? value : this.normalize(value);
			if (this.hasKey(fKey))
			{
				Object object=this.getObjectFromPath(fKey);
				object=this.normalize(object);

				if ((null!=objectValue) && !StringX.equals(objectValue.toString(), object.toString()))
				{
					if (object instanceof JSONArray)
					{
						JSONArray jsonArray=(JSONArray) object;

						if (objectValue instanceof JSONArray)
						{
							this.putAll(fKey, (JSONArray) objectValue);
						}
						else
						{
							jsonArray.put(objectValue);
						}
					}
					else
					{
						this.remove(fKey);

						JSONArray ja=new JSONArray();
						ja.put(object);

						if (!(objectValue instanceof JSONArray))
						{
							ja.put(objectValue);
						}

						this.put(fKey, ja);

						if (objectValue instanceof JSONArray)
						{
							this.putAll(fKey, (JSONArray) objectValue);
						}
					}
				}
			}
			else
			{
				if (this.isJSONObject())
				{
					try
					{
						JSONObject jo=this.toJSONObject();
						if(jo.has(fKey)){
							jo.remove(fKey);
						}
						jo.put(fKey, objectValue);
					}
					catch (StackOverflowError soe){
						if(null!=ReportHandler.getInstance()){
							ReportHandler.getInstance().warning(soe.getMessage());
						}
					}
					catch (JSONException ignored){}
				}
				else if (this.isJSONArray())
				{
					JsonData jo=JsonData.createObject();
					jo.put(fKey, objectValue);
					this.put(jo);
				}
			}
		}

		return this;
	}

	@SuppressWarnings("MethodMayBeStatic")
	public Object normalize(Object value)
	{
		Object objectValue = value;
		if (objectValue instanceof JsonData)
		{
			JsonData jsonData = (JsonData) objectValue;
			objectValue = jsonData.isJSONArray() ? jsonData.toJSONArray() : jsonData.toJSONObject();
		}
		else if ((objectValue instanceof URI)
			|| (objectValue instanceof DateTime)
			|| ClassX.isKindOf(value.getClass(), Enum.class))
		{
			objectValue = objectValue.toString();
		}
		else if (objectValue instanceof Class)
		{
			//noinspection ConstantConditions
			objectValue = StringX.stripStart(objectValue.toString(), "class").trim();
		}

		return objectValue;
	}

	/**
	 * Put a value into the underlying JSON array.
	 *
	 * @param value Value to put.
	 */
	public synchronized JsonData put(Object value)
	{
		if (this.isJSONArray())
		{
			Object objectValue = this.normalize(value);
			JSONArray jsonArray = this.toJSONArray();
			jsonArray.put(objectValue);
		}

		return this;
	}

	/**
	 * Put a value into the underlying JSON array.
	 *
	 * @param value Value to put.
	 */
	public synchronized JsonData put(Integer index, Object value)
	{
		if (this.isJSONArray() && (null!=index))
		{
			try
			{
				JSONArray jsonArray=(JSONArray) this.json;
				jsonArray.put(index, value);
			}
			catch (Exception ignored)
			{
			}
		}

		return this;
	}

	public Object getObjectFromPath(Enum path)
	{
		return this.getObjectFromPath(path.toString());
	}

	/**
	 * Get an untyped value from the specified path.
	 *
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return Untyped value.
	 */
	public Object getObjectFromPath(String... keys)
	{
		Object result = null;

		JSONObject jo = this.toJSONObject();
		if (null!=jo)
		{
			String path = this.getPath(keys);
			result = JsonEngine.getFromPath(jo, path);

			if ((result instanceof JSONArray) || (result instanceof JSONObject))
			{
				JsonData jd=new JsonData();
				jd.json=result;
				result=jd;
			}
		}

		return result;
	}

	/**
	 * Get the length of underlying JSON array.
	 *
	 * @return Length of underlying JSON array; or null if it is not a JSON array.
	 */
	public int length()
	{
		return this.isJSONArray() ? this.toJSONArray().length() : (this.isJSONObject() ? this.keys().size() : 0);
	}

	@SuppressWarnings({"TypeMayBeWeakened", "IfMayBeConditional"})
	public boolean has(String... keys)
	{
		boolean result = this.isValid();

		if (result)
		{
			Object o = this.getObjectFromPath(keys);
			if (o instanceof JsonData)
			{
				JsonData jsonData = (JsonData) o;
				result = jsonData.isValid();
			}
			else if (o instanceof String)
			{
				result = !StringX.isBlank(o.toString());
			}
			else
			{
				result = ((o instanceof Boolean) || (o!=null));
			}
		}
		return result;
	}

	public JsonRepresentation toJsonRepresentation()
	{
		JsonRepresentation result = null;

		if (this.isValid())
		{
			result = new JsonRepresentation(
				this.isJSONArray() ? this.toJSONArray().toString() : this.toJSONObject().toString());
		}
		return result;
	}

	@SuppressWarnings("OverlyNestedMethod")
	public JsonData search(String key, String value)
	{
		JsonData result = null;
		if (this.isJSONArray() && !StringX.isBlank(key) && !StringX.isBlank(value))
		{
			String expected = value.toLowerCase();

			for (Object o : this)
			{
				if (o instanceof JSONObject)
				{
					JsonData jd = new JsonData(o);

					if (jd.keys().contains(key))
					{
						String actual = jd.getString(key);

						if (!StringX.isBlank(actual))
						{
							actual = actual.toLowerCase();
							if (actual.equals(expected))
							{
								result = jd;
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}

	public Collection<String> keys()
	{
		Collection<String> results = new ArrayList<>();
		if (this.isJSONObject())
		{
			@SuppressWarnings("unchecked") Iterator<String> iterator = this.toJSONObject().keys();
			while (iterator.hasNext())
			{
				String key = iterator.next();
				results.add(key);
			}
		}

		return results;
	}

	@SuppressWarnings("ReturnOfThis")
	public void sort(String key)
	{
		if (this.isJSONArray())
		{
			TreeMap<Integer, String> sortedMap = new TreeMap<>();

			JsonData result = JsonData.createArray();

			List<JsonSortable> items = new ArrayList<>();

			int on = 0;
			for (Object o : this)
			{
				if (o instanceof JSONObject)
				{
					JsonData item = new JsonData(o);
					Object value = item.getObjectFromPath(key);
					if(null!=value){
						JsonSortable sortable = new JsonSortable(value, item);
						items.add(sortable);
					}
					on++;
				}
			}

			items.sort(new Comparator<JsonSortable>()
			{
// Overrides
				@Override
				public int compare(JsonSortable o1, JsonSortable o2)
				{
					return ComparatorX.compare(o1.getValue(), o2.getValue());
				}
			});

			for(JsonSortable item: items){
				result.put(item.getData());
			}

			if (!result.isEmpty())
			{
				this.load(result);
			}
		}
	}

	public boolean evalEqual(Object value, String... keys)
	{
		boolean result = false;
		Object obj = this.getObjectFromPath(keys);
		if ((null!=obj) && (null!=value))
		{
			result = obj.equals(value);
		}
		else if ((null==obj) && (null==value))
		{
			result = true;
		}
		return result;
	}

	public String[] getArray(String delimiter, String... keys)
	{
		String[] result = null;
		String item = this.getString(keys);
		if (!StringX.isBlank(item))
		{
			result = StringX.split(item, delimiter);
		}
		return result;
	}

	/**
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return BigDecimal or null.
	 */
	public BigDecimal getBigDecimal(String... keys)
	{
		BigDecimal result = null;
		try
		{
			String value = this.getString(keys);
			if (!StringX.isBlank(value))
			{
				result = new BigDecimal(value);
			}
		} catch (Exception ignored)
		{
		}
		return result;
	}

	public boolean isEqualTo(CharSequence value, String... keys)
	{
		String a = this.getString(keys);
		String b = (null!=value) ? value.toString() : null;
		return (!StringX.isBlank(a) && StringX.equals(a, b));
	}

	public Byte[] getByteArray(String... keys)
	{
		throw new NotImplementedException("Not implemented.");
	}

	public Byte getByte(String... keys)
	{
		throw new NotImplementedException("Not implemented.");
	}

	public File getFile(String... keys)
	{
		String value = this.getString(keys);
		return StringX.isBlank(value) ? null : new File(value);
	}

	public Protocol getProtocol(String... keys)
	{
		Protocol result = null;
		try
		{
			String value = this.getString(keys);
			if (!StringX.isBlank(value))
			{
				result = Protocol.valueOf(value.toLowerCase());
			}
		} catch (Exception ignored)
		{
		}
		return result;
	}

	public boolean hasError()
	{
		return !StringX.isBlank(this.error);
	}

	@SuppressWarnings("TypeMayBeWeakened")
	public <T extends Enum<T>> T getEnum(Class<T> enumType, Object _default, String... keys)
	{
		T result=this.getEnum(enumType, keys);
		if((null==result) && (null!=_default)){
			//noinspection unchecked
			result = (T)_default;
		}
		return result;
	}

	@SuppressWarnings("TypeMayBeWeakened")
	public <T extends Enum<T>> T getEnum(Class<T> enumType, String... keys)
	{
		T result = null;
		String value = this.getString(keys);
		if (!StringX.isBlank(value))
		{
			try
			{
				for (T each : enumType.getEnumConstants())
				{
					if (each.name().compareToIgnoreCase(value)==0)
					{
						result = each;
						break;
					}
				}
			} catch (Exception ignored)
			{
			}
		}
		return result;
	}

	/**
	 * @param keys The path to the value "prop1:prop2:prop_value" or (prop1, prop2, prop_value).
	 * @return A class or null.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	public Class<?> getClassFromName(String... keys)
	{
		Class<?> result = null;
		String value = this.getString(keys);
		if (!StringX.isBlank(value))
		{
			try
			{
				result = Class.forName(value);
			} catch (Exception ignored)
			{
			}
		}
		return result;
	}

	/**
	 * merge all keys and value into this object, duplicate keys will become arrays if they are not already.
	 * or if this is an array add the other objects to this.  Does not validate type.
	 * @param other The json data to merge with.
	 * @param callerWins Does the object calling the merge win over the other object.
	 * @param clearArrays Remove items from all arrays being updated.
	 * @param allowDuplicateKeys Allowing duplicate keys can cause the key value to become an array.
	 */
	public boolean merge(JsonData other, boolean callerWins, boolean clearArrays, boolean allowDuplicateKeys)
	{
		boolean result = false;
		if (this.isJSONObject() && other.isJSONObject())
		{
			Collection<String> keys = other.keys();
			for (String key : keys)
			{
				if (!allowDuplicateKeys)
				{
					if (!this.has(key))
					{
						this.update(key, other.getObjectFromPath(key));
					}
					else{
						Object incoming = other.getObjectFromPath(key);
						if(incoming instanceof JsonData){
							JsonData oData = JsonData.create(incoming);
							Object ot = this.getObjectFromPath(key);
							if(!(ot instanceof JsonData)){
								this.update(key, oData);
							}
							else {
								JsonData tData = JsonData.create(ot);
								if(tData.isJSONArray()){
									JsonData values = tData.mergeArray(oData, clearArrays);
									this.update(key, values);
								}
								else{
									//noinspection ConstantConditions
									tData.merge(oData, true, clearArrays, allowDuplicateKeys);
								}
							}
						}
						else{
							this.merge(callerWins, key, incoming);
						}
					}
				}
				else
				{
					this.merge(callerWins, key, other.getObjectFromPath(key));
				}
			}
			result = true;
		}
		else if(this.isJSONArray() && other.isJSONArray()){
			JsonData items =this.mergeArray(other, clearArrays);
			this.json = items.toJSONArray();
			result = true;
		}

		return result;
	}

	/**
	 * merge all keys and value into this object, duplicate keys will become arrays if they are not already.
	 * or if this is an array add the other objects to this.  Does not validate type.
	 * @param other The json data to merge with, other always wins.
	 */
	public boolean update(JsonData other)
	{
		boolean result = false;
		if (this.isJSONObject() && other.isJSONObject())
		{
			Collection<String> keys = other.keys();
			for (String key : keys)
			{
				// deep updating is not yet supported
				this.update(key, other.getObjectFromPath(key));
			}
			result = true;
		}
		return result;
	}

	/**
	 * Merge two json objects.
	 * inserts new items into existing arrays, otherwise removes old object and adds the new one.
	 *
	 * @param that The json data to merge with.
	 *             Not working yet!!!!!!!!!!!!!!!
	 */
	public JsonData concat(JsonData that)
	{
		JsonData results = this;

		if (this.isJSONObject() && that.isJSONObject())
		{
			Collection<String> keys = that.keys();
			for (String key : keys)
			{
				if (!this.has(key))
				{
					results.put(key, that.getFromPath(key));
				}
				else
				{
					Object thisValue = this.getObjectFromPath(key);
					if (thisValue instanceof JsonData)
					{
						JsonData thisData = (JsonData) thisValue;
						Object thatObject = that.getObjectFromPath(key);
						JsonData thatData = null;
						if (thatObject instanceof JsonData)
						{
							thatData = (JsonData) thatObject;
						}
						if (thisData.isJSONArray())
						{
							if (thatData!=null)
							{
								if (thatData.isJSONArray())
								{
									thisData.addAll(thatData);
								}
								else if (thatData.isJSONObject())
								{
									thisData.put(thatData);
								}
							}
							else if (null!=thatObject)
							{
								boolean exists = false;
								for (Object thisObject : thisData)
								{
									if (Objects.equals(thisObject, thatObject))
									{
										exists = true;
										break;
									}
								}

								if (!exists)
								{
									thisData.put(key, thatObject);
								}
							}

							results.remove(key);
							results.put(key, thisData);
						}
						else if (thisData.isJSONObject())
						{
							Object dirtyO = that.getObjectFromPath(key);
							if (dirtyO instanceof JsonData)
							{
								results = thisData.concat((JsonData) dirtyO);
							}
							else
							{
								results.remove(key);
								results.put(key, dirtyO);
							}
						}
					}
					else
					{
						Object thatValue = that.getObjectFromPath(key);
						// overwrites a singular object.
						if (!Objects.equals(thisValue, thatValue))
						{
							results.remove(key);
							results.put(key, thatValue);
						}
					}
				}
			}
		}

		throw new NotImplementedException("Not implemented.");
	}

	public boolean hasValue(Enum key)
	{
		return this.hasValue(key.toString());
	}

	/**
	 * Does the specified path have a value.
	 *
	 * @param keys the path to the property
	 * @return true if the specified path has a value.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	public boolean hasValue(String... keys)
	{
		String path=this.getPath(keys);
		Object o=this.getObjectFromPath(path);
		boolean result;
		if (o instanceof String)
		{
			result = !StringX.isBlank(o.toString().trim());
		}
		else
		{
			result = (null!=o);
		}
		return result;
	}

	public boolean hasKey(Enum key)
	{
		return this.hasKey(key.toString());
	}

	public boolean hasKey(String key)
	{
		boolean result =  this.keys().contains(key);
		if(!result){
			result = !StringX.isBlank(this.getString(key));
		}
		return result;
	}

	public void addAll(JsonData arrayItems)
	{
		if ((null!=arrayItems) && this.isJSONArray() && arrayItems.isJSONArray())
		{
			for (Object o : arrayItems)
			{
				this.put(o);
			}
		}
	}

	public void append(JsonData jsonData)
	{
		if ((null!=jsonData) && jsonData.isJSONObject())
		{
			Collection<String> keys = jsonData.keys();
			if (null!=keys)
			{
				for (String key : keys)
				{
					this.put(key, jsonData.getObjectFromPath(key));
				}
			}
		}
	}

	public void setValue(String key, Object value)
	{
		if (this.hasKey(key))
		{
			this.remove(key);
		}
		this.put(key, value);
	}

	/**
	 * Each key is evaluated for an object if an object exists with that key it stops and returns.
	 *
	 * @param keys individual keys to check for a JsonData object.
	 * @return Returns a JsonData object.
	 */
	public JsonData getOr(String... keys)
	{
		JsonData result = this;

		for (String key : keys)
		{
			if (this.hasKey(key))
			{
				result = this.getFromPath(key);
				break;
			}
		}

		return result;
	}

	public Object getObjectOrNull(String... keys)
	{
		Object result = null;

		for (String key : keys)
		{
			if (this.hasKey(key))
			{
				result =this.getObjectFromPath(key);
				break;
			}
		}

		return result;
	}

	public void remove(Enum key)
	{
		this.remove(key.toString());
	}

	public void remove(String key)
	{
		if (this.isJSONObject())
		{
			JSONObject jsonObject = this.toJSONObject();
			if(jsonObject.has(key))
			{
				jsonObject.remove(key);
			}
		}
	}

	public Integer getInteger(Enum key)
	{
		return this.getInteger(key.toString());
	}

	public File save(File outputFolder, int maxFilesInFolder)
	{
		File result = outputFolder;
		File[] files = outputFolder.listFiles();
		if ((null!=files) && (files.length>=maxFilesInFolder))
		{
			String folderName = String.format("%d", DateTime.now().getMillis());
			File parent = result.getParentFile();
			result = new File(String.format("%s/%s", parent.getAbsolutePath(), folderName));
			//noinspection ResultOfMethodCallIgnored
			result.mkdir();
		}

		File file = new File(
			String.format("%s/%d.json", result.getAbsolutePath(), DateTime.now().getMillis()));
		String str=this.toString(5);
		FileX.write(file, str);

		return result;
	}

	public String toString(int i)
	{
		String result=null;

		try
		{
			if (this.isJSONObject())
			{
				result=this.toJSONObject().toString(i);
			}
			else if (this.isJSONArray())
			{
				result = this.toJSONArray().toString(i);
			}
		} catch (Exception ignored)
		{
		}

		return result;
	}

	public File save(File outputFolder, String fileName, int maxFilesInFolder)
	{
		File result = outputFolder;
		String fn = fileName;
		File[] files = result.listFiles();
		if ((null!=files) && (files.length>=maxFilesInFolder))
		{
			fn = StringX.stripEnd(fn, ".json");
			String folderName = String.format("%d", DateTime.now().getMillis());
			File parent = result.getParentFile();
			result = new File(String.format("%s/%s", parent.getAbsolutePath(), folderName));
			//noinspection ResultOfMethodCallIgnored
			result.mkdir();
		}

		File file = new File(String.format("%s/%s.json", result.getAbsolutePath(), fn));
		String str = this.toString(5);
		FileX.write(file, str);

		return result;
	}

	public File save(File outputFolder, boolean pretty)
	{
		File result;
		if(StringX.endsWithAnyIgnoreCase(outputFolder.toString(), ".json")){
			result = outputFolder;
		}
		else
		{
			result=new File(
				String.format("%s/%d.json", outputFolder.getAbsolutePath(), DateTime.now().getMillis()));
		}
		FileX.write(result, pretty ? this.toString(5) : this.toString());

		return result;
	}

	public boolean save(File outputFolder, String fileName, boolean pretty)
	{
		String fn = fileName;
		if(!StringX.endsWith(fn, ".json")){
			fn+=".json";
		}
		File file = new File(String.format("%s/%s", outputFolder.getAbsolutePath(), fn));
		FileX.write(file, pretty ? this.toString(5) : this.toString());
		return file.exists();
	}

	public String getKeyAt(int index)
	{
		String result = null;
		Collection<String> keys = this.keys();
		int on = 0;
		for (String key : keys)
		{
			if (on==index)
			{
				result = key;
				break;
			}
		}
		return result;
	}

	public Map<String, Object> toHashMap()
	{
		Map<String, Object> result = new HashMap<>();
		if (this.isJSONObject())
		{
			Collection<String> keys = this.keys();
			for (String key : keys)
			{
				Object o = this.getObjectFromPath(key);
				if (o instanceof JsonData)
				{
					JsonData jd = (JsonData) o;
					result.put(key, jd.toHashMap());
				}
				else
				{
					result.put(key, o);
				}
			}
		}
		else if (this.isJSONArray())
		{
			int len = this.length();
			int on = 1;
			for (Object o : this)
			{
				if (o instanceof JSONObject)
				{
					JsonData jd = new JsonData(o);
					result.put(String.valueOf(on), jd.toHashMap());
				}
				else
				{
					result.put(String.valueOf(on), o);
				}
				on++;
			}
		}

		return result;
	}

	public Map<String, Object> toFlatMap(){
		Map<String, Object> result = new HashMap<>();
		if (this.isJSONObject())
		{
			Collection<String> keys = this.keys();
			for (String key : keys)
			{
				Object o = this.getObjectFromPath(key);
				if (o instanceof JsonData)
				{
					JsonData jd = (JsonData) o;
					if (jd.isJSONArray())
					{
						result.put(key, jd.toString());
					}
					else
					{
						result.put(key, jd.toHashMap());
					}
				}
				else
				{
					result.put(key, o);
				}
			}
		}
		else if (this.isJSONArray())
		{
			throw new IllegalArgumentException("Cannot transform an arry to a flat Map.");
		}

		return result;
	}

	public Map<String, Object> toSingleRoot()
	{
		Map<String, Object> result = new HashMap<>();
		if (this.isJSONObject())
		{
			Collection<String> keys = this.keys();
			for (String key : keys)
			{
				Object o = this.getObjectFromPath(key);
				if (o instanceof JsonData)
				{
					JsonData jd = (JsonData) o;
					if (jd.isJSONArray())
					{
						result.put(key, jd.toString());
					}
					else
					{
						result.put(key, jd.toHashMap());
					}
				}
				else
				{
					result.put(key, o);
				}
			}
		}
		else if (this.isJSONArray())
		{
			int len = this.length();
			int on = 1;
			for (Object o : this)
			{
				if (o instanceof JSONObject)
				{
					JsonData jd = new JsonData(o);
					result.put(String.valueOf(on), jd.toHashMap());
				}
				else
				{
					result.put(String.valueOf(on), o);
				}
				on++;
			}
		}

		return result;
	}

	public void buildClass(String className, File directory)
	{
		JsonDataClassBuilder jdcb = new JsonDataClassBuilder(this);
		jdcb.build(className, directory);
	}

	@SuppressWarnings("SameParameterValue")
	public String toList(String delimiter){
		StringBuilder result = new StringBuilder();

		if(this.isJSONArray())
		{
			for (Object o : this)
			{
				if (null != o)
				{
					if (result.length() > 0)
					{
						result.append(delimiter);
					}
					result.append(o.toString());
				}
			}
		}
		return result.toString();
	}

	/**
	 * Remove an element by index from an array.
	 * @param idx Array index.
	 */
	public void removeAt(int idx)
	{
		if (this.isJSONArray() && (idx<this.length()))
		{
			JSONArray jsonArray=this.toJSONArray();
			jsonArray.remove(idx);
			this.json=jsonArray;
		}
	}

	private void readObject(ObjectInputStream stream)
		throws IOException, ClassNotFoundException
	{
		String s=(String) stream.readObject();
		this.load(s);
	}

	private void writeObject(ObjectOutputStream stream)
		throws IOException
	{
		stream.writeObject(this.toString());
	}

	/**
	 * Is this a JSONObject?
	 *
	 * @return true or false
	 */
	public boolean isJSONObject()
	{
		return (this.json instanceof JSONObject);
	}

	/**
	 * If this is a JSONObject, it will return the JSONObject, otherwise null.
	 *
	 * @return A JSONObject or null.
	 */
	public JSONObject toJSONObject()
	{
		return (this.isJSONObject()) ? (JSONObject) this.json : null;
	}

	/**
	 * Is this a JSONArray?
	 *
	 * @return true or false
	 */
	public boolean isJSONArray()
	{
		return (this.json instanceof JSONArray);
	}

	/**
	 * If this is a JSONArray, it will return the JSONArray, otherwise null.
	 *
	 * @return A JSONArray or null.
	 */
	public JSONArray toJSONArray()
	{
		return (this.isJSONArray()) ? (JSONArray) this.json : null;
	}

	private void readObjectNoData()
		throws ObjectStreamException
	{

	}

	private void load(Object data)
	{
		if (null!=data)
		{
			//noinspection ChainOfInstanceofChecks,IfStatementWithTooManyBranches
			if (data instanceof String)
			{
				String test=data.toString();
				try
				{
					if (StringX.startsWith(test, "["))
					{
						if (test.indexOf('[')==1)
						{
							test=test.substring(1).trim();
						}
						this.json=new JSONArray(test);
					}
					else
					{
						if (test.indexOf('{')==1)
						{
							test=test.substring(1).trim();
						}
						this.json=new JSONObject(test);
					}
				}
				catch (Exception ignored)
				{
					this.error=test;
				}
			}
			else if ((data instanceof JSONObject) || (data instanceof JSONArray))
			{
				this.json=data;
			}
			else if (data instanceof JsonData)
			{
				this.json=this.normalize(data);
			}
			else if (data.getClass().isArray())
			{
				Object[] items=(Object[]) data;
				this.json=new JSONArray();
				for (Object item : items)
				{
					this.put(item);
				}
			}

			// fix first nodes
			if (this.isJSONObject())
			{
				Collection<String> keys=this.keys();
				for (String key : keys)
				{
					Object value=this.getObjectFromPath(key);
					if (value instanceof String)
					{
						String test=value.toString().trim();
						if (!StringX.isBlank(test))
						{
							JsonData item=null;
							try
							{
								if ((StringX.startsWith(test, "[") && StringX.endsWith(test, "]")) ||
								    (StringX.startsWith(test, "{") && StringX.endsWith(test, "}")))
								{
									item=new JsonData(test);
									if ((item.length()<=0))
									{
										item=null;
									}

								}
							}
							catch (Exception ignored)
							{
							}
							if (null!=item)
							{
								this.remove(key);
								this.put(key, item);
							}
						}
					}
				}
			}
		}
		else
		{
			this.error="The json object was null.";
		}
	}

	/**
	 * This method will either fill an existing JSONArray, replace a JSONObject with the Array while inserting the
	 * original object in with the new Array.
	 * This does not check for existing values.
	 *
	 * @param key   Property key to check.
	 * @param value A JSONArray.
	 */
	private synchronized void putAll(String key, JSONArray value)
	{

		if (null!=value)
		{
			if (this.hasValue(key))
			{
				Object object=this.getObjectFromPath(key);
				object=this.normalize(object);

				if (object instanceof JSONArray)
				{
					JSONArray jsonArray=(JSONArray) object;

					@SuppressWarnings("TypeMayBeWeakened")
					JsonData jsonData=new JsonData(value);

					for (Object o : jsonData)
					{
						jsonArray.put(o);
					}
				}
				else
				{
					this.remove(key);

					JSONArray ja=new JSONArray();
					ja.put(object);

					this.put(key, ja);
					this.putAll(key, value);
				}
			}
			else
			{
				this.put(key, value);
			}
		}
	}

	private void putAll(Enum key, JSONArray value)
	{
		this.putAll(key.toString(), value);
	}

	private void merge(boolean callerWins, String key, Object incoming)
	{
		Object actual=this.getObjectFromPath(key);
		boolean update=(!callerWins) || (ObjectX.isDifferent(actual, incoming));
		if (update && callerWins)
		{
			update=Objects.isNull(actual) && !Objects.isNull(incoming);
		}
		if (update)
		{
			this.update(key, incoming);
		}
	}

	private JsonData mergeArray(JsonData other, boolean clearArrays)
	{
		JsonData result=JsonData.createArray();
		if (clearArrays)
		{
			result=other;
		}
		else
		{
			// in order to merge the arrays you could iterate the end result of the put
			// get a count of the keys assign a weight to each key
			// if key matches with value then add to weight total
			// if weight is greater than ? then it is a duplicate and should be removed.
			for (Object o : this)
			{
				result.put(o);
			}
			for (Object o : other)
			{
				result.put(o);
			}
		}
		return result;
	}

	// Getters and setters
	public boolean isValid()
	{
		return (null!=this.json) && (this.isJSONArray() || this.isJSONObject()) && !this.isEmpty();
	}

	public boolean isEmpty()
	{
		return this.isJSONArray() ? !(this.length()>0) : this.keys().isEmpty();
	}

	public int getId()
	{
		return this.getInteger("@lid");
	}

	public String getError()
	{
		return this.error;
	}

	/**
	 * This is an approximate value of this object in bytes.
	 * @return This objects size in bytes.
	 */
	public
	long getSizeInBytes()
	{
		long result = 0;
		String str = this.toString();
		if(null!=str){
			int len = str.length();
			//noinspection IntegerMultiplicationImplicitCastToLong
			result =(len*2);
		}
		return result;
	}

	/**
	 * Will not check nested objects.
	 * @param afterData JsonData after data is changed.
	 * @return The Key and values before and after.
	 */
	public JsonData changes(JsonData afterData, List<String> exclusions){
		JsonData result = JsonData.createObject();

		Collection<String> afterKeys = afterData.keys();

		Iterator<String> bks = this.keys().iterator();

		//noinspection WhileLoopReplaceableByForEach
		while(bks.hasNext()){
			String key = bks.next();
			afterKeys.remove(key);
			if((null!=exclusions) && exclusions.contains(key)){
				continue;
			}
			Object bValue = this.getObjectFromPath(key);
			Object aValue = afterData.getObjectFromPath(key);
			if((bValue instanceof JsonData) || (aValue instanceof JsonData)){
				continue;
			}
			if(!Objects.equals(bValue, aValue)){
				result.put(key, JsonData.createObject().put("before", bValue).put("after", ((null==aValue) ? "null": aValue)));
			}
		}

		Iterator<String> aks = afterKeys.iterator();
		//noinspection WhileLoopReplaceableByForEach
		while(aks.hasNext()){
			String key = aks.next();
			if((null!=exclusions) && exclusions.contains(key)){
				continue;
			}
			Object value = afterData.getObjectFromPath(key);
			if(null==value){
				value = "null";
			}
			result.put(key, JsonData.createObject().put("added", JsonData.createObject().put(key, value)));
		}
		return result;
	}
}
