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

package com.lusidity.apollo.elasticSearch;

import com.lusidity.Environment;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.net.URL;

public class EsActionReport
{
	private final JsonData options;

	// Constructors
	public EsActionReport(JsonData options)
	{
		super();
		this.options=options;
	}

	public JsonData execute()
	{
		JsonData result=null;
		if (null!=this.options)
		{
			String relativePath=this.options.getString("relativePath");
			if (!StringX.isBlank(relativePath))
			{
				result=this.request(relativePath, null);
			}
		}
		return result;
	}

	private JsonData request(String relativePath, Object... args)
	{
		JsonData result=null;
		try
		{
			URL url=Environment.getInstance().getIndexStore().getEngine().getIndexUrl(StringX.stripStart(relativePath, "/"));
			result=Environment.getInstance().getIndexStore().getEngine().getResponse(url, HttpClientX.Methods.GET, null);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}
}
