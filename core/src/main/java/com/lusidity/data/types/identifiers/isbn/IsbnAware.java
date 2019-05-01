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

package com.lusidity.data.types.identifiers.isbn;


import com.lusidity.Environment;
import com.lusidity.data.types.identifiers.UriAware;
import com.lusidity.framework.text.StringX;

import java.net.URI;

public class IsbnAware implements UriAware
{
	@SuppressWarnings("ProtectedField")
	private String value=null;

// Overrides
	@Override
	public URI toUri()
	{
		return null;
	}

// Methods
	public static IsbnAware transform(String value)
	{
		IsbnAware result=null;
		if (!StringX.isBlank(value))
		{
			try
			{
				result=(value.length()<Isbn13.SIZE) ? new Isbn10(value) : new Isbn13(value);
			}
			catch (Exception ignored)
			{
				Environment.getInstance().getReportHandler().warning("Could not create and ISBN from %s", value);
			}
		}

		return result;
	}

// Getters and setters
	public String getValue()
	{
		return this.value;
	}

	public void setValue(String value)
	{
		this.value=value;
	}
}
