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

package com.lusidity.index;

import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.framework.java.ClassX;

import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

public class IndexHelper
{
	private static final Pattern PATTERN_ID=Pattern.compile("^#(\\d*):(\\d*)?");

// Methods
	public static Object getValueForIndex(Object value)
	{
		Object result = value;
		if (null!=result)
		{
			Class cls=result.getClass();
			if (result instanceof Class)
			{
				cls=((Class) result);
				if (ClassX.isKindOf(cls, DataVertex.class))
				{
					//noinspection unchecked
					result=ClassHelper.getClassKey(cls);
				}
				else
				{
					result=cls.getName();
				}
			}
			else if ((result instanceof String) || (result instanceof URI) || (result instanceof URL) || ClassX.isKindOf(cls, Enum.class))
			{
				result=result.toString().trim();
			}
		}
		return result;
	}
}
