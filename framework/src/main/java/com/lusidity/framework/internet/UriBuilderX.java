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

package com.lusidity.framework.internet;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URI Builder
 * <p>
 * from https://gist.github.com/enginer/230e2dc2f1d213a825d5
 * <p>
 * by Sllouyssgort Smaay-Grriyss
 */

public class UriBuilderX extends org.apache.http.client.utils.URIBuilder
{

	// Constructors
	public UriBuilderX()
	{
		super();
	}

	public UriBuilderX(String string)
		throws URISyntaxException
	{
		super(string);
	}

	public UriBuilderX(URI uri)
	{
		super(uri);
	}

	public org.apache.http.client.utils.URIBuilder addPath(String subPath)
	{
		if (subPath==null || subPath.isEmpty() || "/".equals(subPath))
		{
			return this;
		}
		return this.setPath(this.appendSegmentToPath(this.getPath(), subPath));
	}

	private String appendSegmentToPath(String path, String segment)
	{
		if (path==null || path.isEmpty())
		{
			path="/";
		}

		if (path.charAt(path.length()-1)=='/' || segment.startsWith("/"))
		{
			return path+segment;
		}

		return path+"/"+segment;
	}
}
