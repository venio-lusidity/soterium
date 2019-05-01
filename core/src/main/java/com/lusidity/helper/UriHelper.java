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
import com.lusidity.data.DataVertex;
import com.lusidity.framework.text.StringX;

import java.net.URI;

public class UriHelper
{
	// Fields
	public static final String URI_FORMAT="/domains/%s/%s";

// Methods
	/**
	 * @param cls   The class the URI represents.
	 * @param parts Normally the source and id are the first values passed.  The source is typically the origin of the data.
	 * @return A URI.
	 */
	public static URI createCanonicalIdentifier(Class<? extends DataVertex> cls, String... parts)
	{
		URI result=null;
		try
		{
			String uri="";
			if (((null!=cls) && (null!=parts)))
			{
				String name=StringX.insertStringAtCapitol(cls.getSimpleName(), "_").toLowerCase();
				uri=String.format("%s/%s", uri, name);
			}
			if (null!=parts)
			{
				for (String part : parts)
				{
					if (!StringX.isBlank(part))
					{
						uri=StringX.stripEnd(uri, "/");
						String value=StringX.removeNonAlphaNumericCharacters(part, "/");
						uri=String.format("%s/%s", uri, value);
					}
				}
			}
			if (!StringX.isBlank(uri))
			{
				uri=String.format("lid:/%s", uri);
				result=URI.create(uri);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}
}
