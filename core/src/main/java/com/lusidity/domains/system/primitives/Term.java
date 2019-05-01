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

package com.lusidity.domains.system.primitives;

import com.lusidity.Environment;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

import java.net.URI;

@AtSchemaClass(name="Term", discoverable=false, writable=true)
public class Term
	extends Text
{
// Constructors
	public Term(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public Term()
	{
		super();
	}

	public Term(URI sourceUri, String title)
	{
		super(sourceUri, title);
	}

	public Term(URI source, String lang, String text)
	{
		super(source, lang, text);
	}

	public Term(String lang, String text)
	{
		super(lang, text);
	}

	public Term(String text)
	{
		super(Environment.getSourceUri(), text);
	}

// Methods
	public static String getPrimaryIndexValue(String text)
	{
		return Text.getPrimaryIndexValue(text);
	}
}
