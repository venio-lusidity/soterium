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

package com.lusidity.jobs.acs;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.lusidity.Environment;
import com.lusidity.collections.VertexIterator;
import com.lusidity.configuration.SplunkLoggingConfiguration;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.jobs.BaseJob;
import org.joda.time.DateTime;

import java.net.URI;

public class SplunkUserLogging extends BaseJob
{
	private VertexIterator iterator = null;

	// Constructors
	public SplunkUserLogging(ProcessStatus processStatus)
	{
		super(processStatus);
	}

	// Overrides
	@Override
	public boolean start(Object... args)
	{
		try
		{
			DateTime pushed=this.getLastRun();
			DateTime current=DateTime.now();
			DateTime diff=(null==pushed) ? null : pushed.plusMinutes(SplunkLoggingConfiguration.getInstance().getInterval());
			boolean process=(null==diff) || diff.isBefore(current);

			if (process || SplunkLoggingConfiguration.getInstance().getDebug())
			{
				this.handle(current, pushed);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}

		return false;
	}

	private void handle(DateTime current, DateTime pushed)
		throws Exception
	{
		int start=0;
		int limit=1000;

		//DateTime from= DateTimeX.beginingOfTime();

		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(UserActivity.class, UserActivity.class, start, limit);
		//BaseQueryDateFormat qdf=Environment.getInstance().getIndexStore().getQueryDateFormat(from, current);
		qb.filter(BaseQueryBuilder.Operators.lte, "createdWhen", current);
		qb.sort("createdWhen", BaseQueryBuilder.Sort.asc);

		JsonData queue = JsonData.createObject();
		queue.put("batched", JsonData.createArray());

		this.iterator = new VertexIterator(UserActivity.class, qb, 0, 1);
		this.iterator.iterate(new SplunkLogHandler(queue), this.getProcessStatus(), 0);

		// because of failures there could be duplicates sent to Splunk.
		if(!queue.getBoolean("failed"))
		{
			JsonData batched = queue.getFromPath("batched");
			if (!batched.isEmpty())
			{
				SplunkUserLogging.post(batched);
			}
				this.iterator.delete(new ProcessStatus(), this.getData().getInteger(1, "maxDeleteThreads"));
			int stop = 0;
		}
	}

	protected static boolean post(JsonData batched)
		throws Exception
	{
		batched.remove("failed");
		SplunkLoggingConfiguration slc=SplunkLoggingConfiguration.getInstance();
		URI uri=HttpClientX.buildUri(
			slc.getProtocol(),
			String.format("%s:%d", slc.getHost(), slc.getPort()),
			"/services/collector"
		);

		HttpContent content=new ByteArrayContent("application/json", batched.toString().getBytes());

		JsonData result=HttpClientX.getResponse(uri.toURL(), HttpClientX.Methods.POST, content, Environment.getInstance().getConfig().getReferer(),
			"Authorization", String.format("%s %s", slc.getUsername(), slc.getToken()),
			"sourcetype", slc.getSourcetype()
		);

		return result.getBoolean("http_ok");
	}

	@Override
	public String getTitle()
	{
		return "Splunk User Logging";
	}

	@Override
	public String getDescription()
	{
		return "Send User Activity Logs to SPLUNK.";
	}

	@Override
	public boolean stop()
	{
		boolean result = super.stop();
		if(null!=this.iterator){
			this.iterator.stop();
		}
		return result;
	}
}
