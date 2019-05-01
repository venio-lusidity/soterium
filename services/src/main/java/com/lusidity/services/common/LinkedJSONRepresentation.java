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

package com.lusidity.services.common;

import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public
class LinkedJSONRepresentation
	extends WriterRepresentation
{

	private static final Pattern PATTERN_CURLY_START = Pattern.compile("\\{");
	private static final Pattern PATTERN_CURLY_END = Pattern.compile("\\}");
	private static final Pattern PATTERN_CURLY_START_QUOTED = Pattern.compile("\\\"\\{");
	private static final Pattern PATTERN_CURLY_END_QUOTED_C = Pattern.compile("\\}\\\"\\}");
	private static final Pattern PATTERN_CURLY_END_QUOTED_B = Pattern.compile("\\}\\\"\\]");
	private static final Pattern PATTERN_CURLY_QUOTE_COMMA = Pattern.compile("\\}\\\"\\,");
	private static final Pattern PATTERN_BRACKET_START = Pattern.compile("\\[}");
	private static final Pattern PATTERN_BRACKET_END = Pattern.compile("\\]");
	private static final Pattern PATTERN_BRACKET_START_QUOTED = Pattern.compile("\\\"\\[");
	private static final Pattern PATTERN_BRACKET_END_QUOTED_C = Pattern.compile("\\]\\\"\\}");
	private static final Pattern PATTERN_BRACKET_END_QUOTED_B = Pattern.compile("\\]\\\"\\]");
	private static final Pattern PATTERN_BRACKET_QUOTE_COMMA = Pattern.compile("\\}\\\"\\,");
	private static final Pattern PATTERN_COMMA_SPACE = Pattern.compile("\\,[\\s]");
	private static final Pattern PATTERN_COMMA_NO_SPACE = Pattern.compile("\\,\\S");
	private static final Pattern PATTERN_EQUAL = Pattern.compile("\\=");
	private static final Pattern PATTERN_DOUBLE_QUOTES = Pattern.compile("\\\"\\\"");
	@SuppressWarnings("CollectionDeclaredAsConcreteClass")
	private LinkedHashMap<String, Object> linkedHashMap = null;

	public
	LinkedJSONRepresentation(LinkedHashMap<String, Object> linkedHashMap)
	{
		super(MediaType.APPLICATION_JSON);
		this.setLinkedHashMap(linkedHashMap);
	}

	private
	void setLinkedHashMap(LinkedHashMap<String, Object> linkedHashMap)
	{
		this.linkedHashMap = linkedHashMap;
	}

	@Override
	public
	void write(Writer writer)
		throws IOException
	{
		String json = this.linkedHashMap.toString();
		json = LinkedJSONRepresentation.PATTERN_CURLY_START.matcher(json).replaceAll("{\\\"");
		json = LinkedJSONRepresentation.PATTERN_BRACKET_START.matcher(json).replaceAll("[\\\"");
		json = LinkedJSONRepresentation.PATTERN_COMMA_SPACE.matcher(json).replaceAll("\\\",\\\"");
		json = LinkedJSONRepresentation.PATTERN_EQUAL.matcher(json).replaceAll("\\\":\\\"");
		json = LinkedJSONRepresentation.PATTERN_CURLY_END.matcher(json).replaceAll("\\\"}");
		json = LinkedJSONRepresentation.PATTERN_BRACKET_END.matcher(json).replaceAll("\\\"]");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_CURLY_START_QUOTED, json, "{");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_CURLY_QUOTE_COMMA, json, "},");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_CURLY_END_QUOTED_C, json, "}}");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_CURLY_END_QUOTED_B, json, "}]");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_BRACKET_START_QUOTED, json, "[");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_BRACKET_QUOTE_COMMA, json, "],");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_BRACKET_END_QUOTED_C, json, "]}");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_BRACKET_END_QUOTED_B, json, "]]");
		json = LinkedJSONRepresentation.deep(LinkedJSONRepresentation.PATTERN_DOUBLE_QUOTES, json, "\\\"");
		writer.write(json);
	}

	private static
	String deep(Pattern pattern, String text, String replace)
	{
		String result = text;
		if (pattern.matcher(result).find())
		{
			result = pattern.matcher(text).replaceAll(replace);
			result = LinkedJSONRepresentation.deep(pattern, result, replace);
		}

		return result;
	}
}
