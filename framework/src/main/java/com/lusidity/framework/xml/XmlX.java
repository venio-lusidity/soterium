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

package com.lusidity.framework.xml;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.text.StringX;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XMLTokener;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Iterator;

public class XmlX {

    /**
     * The Character '&amp;'.
     */
    @SuppressWarnings("ConstantNamingConvention")
    public static final Character AMP = '&';
    /**
     * The Character '''.
     */
    public static final Character APOS = '\'';
    /**
     * The Character '!'.
     */
    public static final Character BANG = '!';
    /**
     * The Character '='.
     */
    @SuppressWarnings("ConstantNamingConvention")
    public static final Character EQ = '=';
    /**
     * The Character '>'.
     */
    @SuppressWarnings("ConstantNamingConvention")
    public static final Character GT = '>';
    /**
     * The Character '&lt;'.
     */
    @SuppressWarnings("ConstantNamingConvention")
    public static final Character LT = '<';
    /**
     * The Character '?'.
     */
    public static final Character QUEST = '?';
    /**
     * The Character '"'.
     */
    public static final Character QUOT = '"';
    /**
     * The Character '/'.
     */
    public static final Character SLASH = '/';

    /**
     * Replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>
     *
     * @param string The string to be escaped.
     * @return The escaped string.
     */
    @SuppressWarnings("ConstantConditions")
    public static String escape(String string) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = string.length(); i < length; i++) {
            char c = string.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Throw an exception if the string contains whitespace.
     * Whitespace is not allowed in tagNames and attributes.
     *
     * @param string A String.
     * @throws org.json.JSONException
     */
    public static void noSpace(String string) throws JSONException {
        int i, length = string.length();
        if (length == 0) {
            throw new JSONException("Empty string.");
        }
        for (i = 0; i < length; i += 1) {
            if (Character.isWhitespace(string.charAt(i))) {
                throw new JSONException(String.format("'%s' contains a space character.", string));
            }
        }
    }

    /**
     * Scan the content following the named tag, attaching it to the context.
     *
     * @param x       The XMLTokener containing the source string.
     * @param context The JSONObject that will include the new material.
     * @param name    The tag name.
     * @return true if the close tag is processed.
     * @throws org.json.JSONException
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyNestedMethod", "MethodWithMultipleReturnPoints", "OverlyLongMethod"})
    private static boolean parse(XMLTokener x, JSONObject context,
                                 String name) throws JSONException {
        char c;
        int i;
        JSONObject jsonobject = null;
        String string;
        String tagName;
        Object token;

// Test for and skip past these forms:
//      <!-- ... -->
//      <!   ...   >
//      <![  ... ]]>
//      <?   ...  ?>
// Report errors for these forms:
//      <>
//      <=
//      <<

        token = x.nextToken();

// <!

        if (token.equals(XmlX.BANG)) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (!string.isEmpty()) {
                            context.accumulate("content", string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token.equals(XmlX.LT)) {
                    i += 1;
                } else if (token.equals(XmlX.GT)) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token.equals(XmlX.QUEST)) {

// <?

            x.skipPast("?>");
            return false;
        } else if (token.equals(XmlX.SLASH)) {

// Close tag </

            token = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (!x.nextToken().equals(XmlX.GT)) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

// Open tag <

        } else {
            tagName = (String) token;
            token = null;
            jsonobject = new JSONObject();
            for (; ; ) {
                if (token == null) {
                    token = x.nextToken();
                }

// attribute = value

                if (token instanceof String) {
                    string = (String) token;
                    token = x.nextToken();
                    if (token.equals(XmlX.EQ)) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }
                        jsonobject.accumulate(string,
                                XmlX.stringToValue((String) token));
                        token = null;
                    } else {
                        jsonobject.accumulate(string, "");
                    }

// Empty tag <.../>

                } else if (token.equals(XmlX.SLASH)) {
                    if (!x.nextToken().equals(XmlX.GT)) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (jsonobject.length() > 0) {
                        context.accumulate(tagName, jsonobject);
                    } else {
                        context.accumulate(tagName, "");
                    }
                    return false;

// Content, between <...> and </...>

                } else if (token.equals(XmlX.GT)) {
                    for (; ; ) {
                        token = x.nextContent();
                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {
                            string = (String) token;
                            if (!string.isEmpty()) {
                                jsonobject.accumulate("content",
                                        XmlX.stringToValue(string));
                            }

// Nested element

                        } else if (token.equals(XmlX.LT)) {
                            if (XmlX.parse(x, jsonobject, tagName)) {
                                if (jsonobject.length() == 0) {
                                    context.accumulate(tagName, "");
                                } else if ((jsonobject.length() == 1) &&
                                        (jsonobject.opt("content") != null)) {
                                    context.accumulate(tagName,
                                            jsonobject.opt("content"));
                                } else {
                                    context.accumulate(tagName, jsonobject);
                                }
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }


    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string. This is much less ambitious than
     * JSONObject.stringToValue, especially because it does not attempt to
     * convert plus forms, octal forms, hex forms, or E forms lacking decimal
     * points.
     *
     * @param string A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(String string) {
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }

        // If you need the value to be numeric you will need to change it yourself.
        // The original code attempted to do this but missed more than it hit.
        // Even vor doubles .45000 does not equal .45 once converted to double and compared as strings.

        return string;
    }


    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation
     * because JSON is a data format and XML is a document format. XML uses
     * elements, attributes, and content text, while JSON uses unordered
     * collections of name/value pairs and arrays of values. JSON does not
     * does not like to distinguish between elements and attributes.
     * Sequences of similar elements are represented as JSONArrays. Content
     * text may be placed in a "content" member. Comments, prologs, DTDs, and
     * <code>&lt;[ [ ]]></code> are ignored.
     *
     * @param string The source string.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws org.json.JSONException
     */
    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject jo = new JSONObject();
        XMLTokener x = new XMLTokener(string);
        while (x.more() && x.skipPast("<")) {
            XmlX.parse(x, jo, null);
        }
        return jo;
    }


    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object A JSONObject.
     * @return A string.
     * @throws org.json.JSONException
     */
    public static String toString(Object object) throws JSONException {
        return XmlX.toString(object, null);
    }


    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object  A JSONObject.
     * @param tagName The optional name of the enclosing tag.
     * @return A string.
     * @throws org.json.JSONException
     */
    public static String toString(Object object, String tagName)
            throws JSONException {
        StringBuffer sb = new StringBuffer();
        int i;
        JSONArray ja;
        JSONObject jo;
        String key;
        Iterator keys;
        int length;
        String string;
        Object value;
        if (object instanceof JSONObject) {

// Emit <tagName>

            if (tagName != null) {
                sb.append('<');
                sb.append(tagName);
                sb.append('>');
            }

// Loop thru the keys.

            jo = (JSONObject) object;
            keys = jo.keys();
            while (keys.hasNext()) {
                key = keys.next().toString();
                value = jo.opt(key);
                if (value == null) {
                    value = "";
                }
                if (value instanceof String) {
                    string = (String) value;
                } else {
                    string = null;
                }

// Emit content in body

                if ("content".equals(key)) {
                    if (value instanceof JSONArray) {
                        ja = (JSONArray) value;
                        length = ja.length();
                        for (i = 0; i < length; i += 1) {
                            if (i > 0) {
                                sb.append('\n');
                            }
                            sb.append(XmlX.escape(ja.get(i).toString()));
                        }
                    } else {
                        sb.append(XmlX.escape(value.toString()));
                    }

// Emit an array of similar keys

                } else if (value instanceof JSONArray) {
                    ja = (JSONArray) value;
                    length = ja.length();
                    for (i = 0; i < length; i += 1) {
                        value = ja.get(i);
                        if (value instanceof JSONArray) {
                            sb.append('<');
                            sb.append(key);
                            sb.append('>');
                            sb.append(XmlX.toString(value));
                            sb.append("</");
                            sb.append(key);
                            sb.append('>');
                        } else {
                            sb.append(XmlX.toString(value, key));
                        }
                    }
                } else if ("".equals(value)) {
                    sb.append('<');
                    sb.append(key);
                    sb.append("/>");

// Emit a new tag <k>

                } else {
                    sb.append(XmlX.toString(value, key));
                }
            }
            if (tagName != null) {

// Emit the </tagname> close tag

                sb.append("</");
                sb.append(tagName);
                sb.append('>');
            }
            return sb.toString();

// XML does not have good support for arrays. If an array appears in a place
// where XML is lacking, synthesize an <array> element.

        } else {
            if (object.getClass().isArray()) {
                object = new JSONArray(object);
            }
            if (object instanceof JSONArray) {
                ja = (JSONArray) object;
                length = ja.length();
                for (i = 0; i < length; i += 1) {
                    sb.append(XmlX.toString(ja.opt(i), (tagName == null) ? "array" : tagName));
                }
                return sb.toString();
            } else {
                string = (object == null) ? "null" : XmlX.escape(object.toString());
                return (tagName == null) ? ('"' + string + '"') :
                        (string.isEmpty()) ? ('<' + tagName + "/>") :
                                ('<' + tagName + '>' + string + "</" + tagName + '>');
            }
        }
    }

    public static Document parse(String xml) throws ApplicationException {
        Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource inputSource = new InputSource(new StringReader(xml));
            document = db.parse(inputSource);

        }
        catch (Exception e)
        {
            throw new ApplicationException(e);
        }
        return document;
    }

    public static
    boolean isNullValue(String value)
    {
        return StringX.isBlank(value) || StringX.containsIgnoreCase(value, "xs_nil") || StringX.equalsIgnoreCase(value, "null");
    }
}
