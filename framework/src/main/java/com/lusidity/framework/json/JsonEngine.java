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


import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.framework.xml.LazyXmlNode;
import com.lusidity.framework.xml.XmlX;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

@SuppressWarnings({
	"unused",
	"NonFinalUtilityClass"
	,
	"OverlyComplexClass"
})
public
class JsonEngine
{
	@SuppressWarnings({
		"InterfaceNamingConvention",
		"InterfaceNeverImplemented"
	})
	public interface EvalObjectIterator
	{
		/**
		 * Evaluate a property from an collection of properties.
		 *
		 * @param jsonData The JsonData.
		 * @param key      the property key.
		 * @return true to continue iteration, false to stop iteration.
		 */
		boolean evaluate(JsonData jsonData, String key);
	}

	/**
	 * Evaluating iterator.
	 */
	@SuppressWarnings({
		"InterfaceNamingConvention",
		"InterfaceNeverImplemented"
	})
	public interface EvalIterator
	{
		/**
		 * Evaluate an element from an array.
		 *
		 * @param element Element to evaluate.
		 * @param index   Element index (for reference).
		 * @return true to continue iteration, false to stop iteration.
		 */
		boolean evaluate(Object element, int index);
	}

	/**
	 * Evaluating iterator. If the Object is not null the iteration will stop.
	 */
	@SuppressWarnings({
		"InterfaceNamingConvention",
		"InterfaceNeverImplemented"
	})
	public interface EvalValueOrNullIterator
	{
		/**
		 * Evaluate an element from an array.
		 *
		 * @param element Element to evaluate.
		 * @param index   Element index (for reference).
		 * @return If the return value is null, iteration with continue; if the return value is not null,
		 * iteration will stop and the result will be
		 * returned from the evaluation loop.
		 */
		Object evaluate(Object element, int index);
	}
	private static final Pattern DELIMITER = Pattern.compile("::");
	private static final Pattern PATTERN_SLASHES = Pattern.compile("/");
	private static final Pattern PATTERN_YEAR_START = Pattern.compile("^((19|20)[\\d]{2}).*");
	private static final Pattern PATTERN_YEAR_END = Pattern.compile("^.+((19|20)[\\d]{2}).*");

	/** Private construct for utility class. */
	private JsonEngine()
	{
		super();
	}

// Methods
	/**
	 * Parse the file as either a JSONArray or JSONObject.
	 *
	 * @param file
	 * 	A json file.
	 *
	 * @return A JSONArray, JSONObject or null.
	 */
	public static
	Object getFromFile(File file)
	{
		Object result = null;
		FileReader fileReader = null;
		try
		{
			String ext = StringX.getLast(file.getAbsolutePath(), ".");
            if(StringX.equalsIgnoreCase(ext, "json")){
                String json = FileX.getString(file);
                if(!StringX.isBlank(json)){
                    result = new JsonData(json);
                }
            }
            else if(StringX.equalsIgnoreCase(ext, "xml")){
	            //this is a file, so disbable DTD
	            LazyXmlNode node = LazyXmlNode.load(file, true);
	            result = JsonEngine.fromXml(node);
            }
            else {
                //noinspection IOResourceOpenedButNotSafelyClosed
	            String str =FileX.getString(file);
	            result = new JSONObject(str);
            }
		}
		catch (Exception ignored)
		{
			result=null;
		}
		return result;
	}

	public static JSONObject fromXml(LazyXmlNode node)
	{
		JSONObject result=null;
		try
		{
			StringWriter sw=new StringWriter();
			Transformer t=TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node.getDocument()), new StreamResult(sw));
			result=XML.toJSONObject(sw.toString());
		}
		catch (Exception ignored)
		{
		}
		return result;
	}

	/**
	 * Compare two JSONObjects or JSONArrays.
	 *
	 * @param expected
	 * 	JSONObject or JSONArray
	 * @param actual
	 * 	JSONObject or JSONArray
	 * @param validate
	 * 	Set true to validate values of properties.
	 *
	 * @return Returns true if all properties are equal or both object are null, Values of properties will not be evaluated unless you set
	 * the flag
	 *         validate to true.
	 */
	@SuppressWarnings({
		"ConstantConditions",
		"OverlyNestedMethod",
		"OverlyComplexMethod",
		"OverlyLongMethod"
	})
	public static boolean areSimilar(
		Object expected, Object actual, boolean validate, Collection<String> excluded, String path, boolean debug
	)
	{
		boolean result = true;

		try
		{
			if ((null != expected) && (null != actual))
			{
				if (expected.getClass().equals(JSONObject.class) && actual.getClass().equals(JSONObject.class))
				{
					JSONObject ja = (JSONObject) expected;
					JSONObject jb = (JSONObject) actual;

					Iterator keys = ja.keys();
					while (keys.hasNext())
					{
						boolean validateValue = validate;
						String key = (String) keys.next();
						if (validate && (null != excluded) && excluded.contains(key))
						{
							validateValue = false;
						}

						result = jb.has(key);
						String op = (StringX.isBlank(path) ? key : String.format("%s::%s", path, key));
						if (result)
						{
							Object oa = ja.get(key);
							Object ob = jb.get(key);
							result = JsonEngine.areSimilar(oa, ob, validateValue, excluded, op, debug);
						}

						if (!result)
						{
							if (debug)
							{
								if (ja.has("uri"))
								{
									String uri = ja.get("uri").toString();
								}
							}
							break;
						}
					}
				}
				else if (expected.getClass().equals(JSONArray.class) && actual.getClass().equals(JSONArray.class))
				{
					JSONArray ja = (JSONArray) expected;
					JSONArray jb = (JSONArray) actual;

					if (validate)
					{
						result = (ja.length() == jb.length());
					}

					if (result)
					{
						int len = ja.length();
						for (int i = 0; i < len; i++)
						{
							Object oa = ja.get(i);
							Object ob = jb.get(i);
							String key = String.format("array_%d", i);
							String op = (StringX.isBlank(path) ? key : String.format("%s::%s", path, key));
							result = JsonEngine.areSimilar(oa, ob, validate, excluded, op, debug);
							if (!result)
							{
								break;
							}
						}
					}
				}
				else if (validate)
				{
					String sa = expected.toString();
					String sb = actual.toString();

					result = sa.equals(sb);
				}
			}
			else
			{
				result = ((null == expected) && (null == actual));
			}
		}
		catch (Exception ignored)
		{
		}
		return result;
	}

	/**
	 * Does the specified key have a blank or null value or not exist?
	 *
	 * @param json
	 * 	JSON object to check.
	 * @param key
	 * 	Property key to check.
	 *
	 * @return true if the specified property is (effectively) blank.
	 */
	public static
	boolean isBlank(JSONObject json, String key)
	{
		boolean result;
		try
		{
			result = (null!=json) && (!json.has(key) || json.isNull(key) || StringX.isBlank(json.getString(key)));
		}
		catch (JSONException ignored)
		{
			result = true;
		}
		return result;
	}

	public static
	JSONObject fromXml(Document document)
	{
		JSONObject result = null;
		try
		{
			StringWriter sw = new StringWriter();
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(document), new StreamResult(sw));
			result = XmlX.toJSONObject(sw.toString());
		}
		catch (Exception ignored)
		{
		}
		return result;
	}

	public static
	Integer getInteger(JSONObject json, String key)
	{
		Integer result = null;

		try
		{
			if ((null != json) && json.has(key))
			{
				result = json.getInt(key);
			}
		}
		catch (Exception ignored)
		{
		}

		return result;
	}

	public static
	String getString(JSONObject json, String key)
	{
		String result = null;

		try
		{
			if ((null != json) && json.has(key))
			{
				result = json.getString(key);
			}
		}
		catch (Exception ignored)
		{
			result = null;
		}

		//noinspection ConstantConditions
		if (StringX.equals(result, "null"))
		{
			result = null;
		}

		return result;
	}

	@SuppressWarnings("unused")
	public static
	<T> JSONObject toJSON(T object)
		throws IntrospectionException
	{
		JSONObject result = new JSONObject();

		BeanInfo info = Introspector.getBeanInfo(object.getClass());
		PropertyDescriptor[] pds = info.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds)
		{
			try
			{
				String key = pd.getName();
				Object value = pd.getReadMethod().invoke(object);
				if (!StringX.isBlank(key) && (null != value))
				{
					if (value.getClass().equals(Collection.class))
					{
						JSONArray jsonArray = new JSONArray();
						Iterable collection = (Iterable) value;
						for (Object o : collection)
						{
							JSONObject jsonObject = JsonEngine.toJSON(o);
							jsonArray.put(jsonObject);
						}
						if (jsonArray.length() > 0)
						{
							result.put(key, jsonArray);
						}
					}
					else
					{
						result.put(key, value);
					}
				}
			}
			catch (Exception ignored)
			{
			}
		}
        if(result.length()<=0)
        {
            result = null;
        }
		return result;
	}

	/**
	 * Get a JSON array from the specified path.
	 *
	 * @param jsonObject
	 * 	JSON object.
	 * @param path
	 * 	Path, with elements separated by double colons ('::').
	 *
	 * @return JSON array.
	 */
	public static
	JSONArray getArrayFromPath(JSONObject jsonObject, String path)
	{
		JSONArray result;
		Object object = JsonEngine.getFromPath(jsonObject, path);
		result=(object instanceof JSONArray) ? (JSONArray) object : new JSONArray();
		return result;
	}

	/**
	 * Get the last object in the path.
	 *
	 * @param jsonObject
	 * 	can be a JSONObject, JSONArray or JSON. A json array or object.
	 * @param path
	 * 	The path to the value expected.
	 *
	 * @return An object at the end of the path. Note that if an array is encountered only the first object in the array is used.
	 */
	@SuppressWarnings({"ConstantConditions", "OverlyNestedMethod", "OverlyComplexMethod", "OverlyLongMethod"})
	public static
	Object getFromPath(Object jsonObject, String path)
	{
		Object result = null;
        if(!StringX.isBlank(path))
        {
	        try
            {
	            boolean found=true;
	            Object current=jsonObject;
	            if (null!=current)
                {
                    String[] properties = JsonEngine.DELIMITER.split(path);

                    for (String property : properties)
                    {
                        //noinspection ChainOfInstanceofChecks
                        if (current instanceof JSONObject)
                        {
	                        JSONObject jo=(JSONObject) current;
	                        current=jo.has(property) ? jo.get(property) : null;
	                        if (null==current)
	                        {
		                        found=false;
		                        break;
	                        }
                        }
                        else if (current instanceof JSONArray)
                        {
                            JSONArray ja = (JSONArray) current;
                            int len = ja.length();

                            found = false;
                            for (int i = 0; i < len; i++)
                            {
	                            JSONObject jo=ja.getJSONObject(i);
	                            current=jo.has(property) ? jo.get(property) : null;
	                            if(null!=current){
		                            found = true;
		                            break;
	                            }
                            }
                            if(!found)
                            {
                                current = null;
                                break;
                            }
                        }
                    }
                }
                else
                {
                    found = false;
                }

                if (found)
                {
                    result = current;
                }
            }
            catch (Exception ignored)
            {
                result = null;
            }

	        //noinspection IfStatementWithIdenticalBranches
	        if ((result instanceof String) && result.toString().equals("null"))
	        {
		        result = null;
	        }
	        else if ((result != null) && result.equals(JSONObject.NULL))
            {
                result = null;
            }
        }

		return result;
	}

	/**
	 * Get a JSON object via an HTTP GET call to the specified URI.
	 *
	 * @param uri
	 * 	URI.
	 *
	 * @return JSON object, or null if the GET failed or the response was not properly-formed JSON content.
	 */
	public static
	JSONObject getFromUri(URI uri, String ... headers)
	{
		String str = HttpClientX.getString(uri, headers);
		JSONObject result = null;
		if (JsonEngine.isValid(str))
		{
			try
			{
				result = new JSONObject(str);
			}
			catch (Exception ignored)
			{
			}
		}
		return result;
	}

	public static
	boolean isValid(String jsonResponse)
	{
		return !StringX.isBlank(jsonResponse) && jsonResponse.trim().startsWith("{");
	}

	/**
	 * Safely get a JSON object from a JSON array.
	 *
	 * @param jsonArray
	 * 	Array.
	 * @param index
	 * 	Index.
	 *
	 * @return JSON object at the specified index in the array, or null if there is not a valid JSON object at the specified index.
	 */
	public static
	JSONObject getJsonObject(JSONArray jsonArray, int index)
	{
		JSONObject result;
		try
		{
			result = jsonArray.getJSONObject(index);
		}
		catch (Exception ignored)
		{
			result = null;
		}
		return result;
	}

	/**
	 * Iterate a JSONArray while using the given EvalIterator. If you want to stop iteration the EvalIterator must return false;
	 *
	 * @param jsonArray
	 * 	The JSONArray to iterate.
	 * @param evalIterator
	 * 	An EvalIterator which contains your logic.
	 */
	public static
	void iterate(JSONArray jsonArray, EvalIterator evalIterator)
	{
		int n = jsonArray.length();
		for (int i = 0; i < n; i++)
		{
			try
			{
				Object element = jsonArray.get(i);
				boolean proceed = evalIterator.evaluate(element, i);
				if (!proceed)
				{
					break;
				}
			}
			catch (Exception ignored)
			{
			}
		}
	}

    public static void iterate(JsonData jsonData, EvalObjectIterator evalObjectIterator) {
        if(jsonData.isJSONObject())
        {
            try
            {
                boolean proceed = true;
                Iterator keys = jsonData.toJSONObject().keys();
                while (keys.hasNext() && proceed)
                {
                    Object key = keys.next();
                    proceed = evalObjectIterator.evaluate(jsonData, key.toString());
                }
            }
            catch (Exception ignored){}
        }
    }

	/**
	 * Iterate a JSONArray while using the given EvalIterator. If the returnValue has a value the iteration will stop.
	 *
	 * @param jsonArray
	 * 	The JSONArray to iterate.
	 * @param evaluator
	 * 	An EvalValueOrNullIterator which contains your logic.
	 */
	public static
	Object iterate(JSONArray jsonArray, EvalValueOrNullIterator evaluator)
	{
		Object value = null;
		int n = jsonArray.length();
		for (int i = 0; i < n; i++)
		{
			try
			{
				Object element = jsonArray.get(i);
				value = evaluator.evaluate(element, i);
				if (null != value)
				{
					break;
				}
			}
			catch (Exception ignored)
			{
			}
		}

		return value;
	}

	/**
	 * Get date from path.
	 *
	 * @param jsonObject
	 * 	JSON object.
	 * @param srcPath
	 * 	Path.
	 *
	 * @return Date/time value, or null.
	 */
	public static
	DateTime getDateTimeFromPath(JSONObject jsonObject, String srcPath)
	{
		DateTime result = null;
		String str = JsonEngine.getStringFromPath(jsonObject, srcPath);
		if (null != str)
		{
			try
			{
                result = JsonEngine.parseDateTime(str);
            }
			catch (Exception ignored)
			{
			}
		}
		return result;
	}

    /**
	 * Get a string value from a JSON path.
	 *
	 * @param jsonObject
	 * 	JSON object.
	 * @param srcPath
	 * 	Path.
	 *
	 * @return String value, or null.
	 */
	public static
	String getStringFromPath(JSONObject jsonObject, String srcPath)
	{
		String result = null;
		Object o = JsonEngine.getFromPath(jsonObject, srcPath);
		if (o instanceof String)
		{
			result = (String) o;
		}
		else if (null != o)
		{
			result = o.toString();
		}
		return result;
	}

    public static DateTime parseDateTime(CharSequence dateTime) {
        DateTime result = null;
        //  Replace '/' separators used by some data sources (such as OpenLibrary) with ISO-correct '-' separators
        String str = JsonEngine.PATTERN_SLASHES.matcher(dateTime).replaceAll("-");

        if (!StringX.isBlank(str))
        {
            result = DateTimeX.parse(str);
        }
        return result;
    }

	public static
	JSONObject getJsonObjectFromPath(JSONObject jsonObject, String path)
	{
		JSONObject result = null;
		Object object = JsonEngine.getFromPath(jsonObject, path);
		if (object instanceof JSONObject)
		{
			result = (JSONObject) object;
		}
		return result;
	}

	public static
	JSONArray getJSONArrayFromPath(JSONObject jsonObject, String path)
	{
		Object object = JsonEngine.getFromPath(jsonObject, path);
		return (object instanceof JSONArray) ? (JSONArray) object : null;
	}

	public static
	boolean getBoolean(JSONObject jsonObject, String path)
	{
		boolean result = false;

		if ((null != jsonObject))
		{
			try
			{
				String str = JsonEngine.getStringFromPath(jsonObject, path);
				result = Boolean.parseBoolean(str);
			}
			catch (Exception ignored)
			{
			}
		}

		return result;
	}

	/** From Erel Segal Halevi's answer to http://stackoverflow.com/questions/2403132/concat-multiple-jsonobjects */
	public static
	JSONObject deepMerge(JSONObject source, JSONObject target)
		throws JSONException
	{
		for (String key : JSONObject.getNames(source))
		{
			Object value = source.get(key);
			if (!target.has(key))
			{
				// new value for "key":
				target.put(key, value);
			}
			else
			{
				// existing value for "key" - recursively deep merge:
				if (value instanceof JSONObject)
				{
					JSONObject valueJson = (JSONObject) value;
					JsonEngine.deepMerge(valueJson, target.getJSONObject(key));
				}
				else
				{
					target.put(key, value);
				}
			}
		}
		return target;
	}

    public static void putAll(JSONArray target, JSONArray items) {

        if((null != target) && (null != items))
        {
            int len = items.length();
            for(int i=0;i<len;i++)
            {
                try
                {
                    Object o = items.get(i);
                    target.put(o);
                }
                catch (Exception ignored){}
            }
        }
    }

    public static Object getObject(JSONArray jsonArray, int index) {
        Object result;
        try
        {
            result = jsonArray.get(index);
        }
        catch (Exception ignored)
        {
            result = null;
        }
        return result;
    }

    public static JSONObject fromString(String str) {
        JSONObject result = null;
        try
        {
            result = new JSONObject(str);
        }
        catch (Exception ignored){}
        return result;
    }

    public static String getStringAt(JSONArray jsonArray, int index) {
        String result = null;
        try
        {
            result = jsonArray.getString(index);
        }
        catch (Exception ignored){}
        return  result;
    }
}
