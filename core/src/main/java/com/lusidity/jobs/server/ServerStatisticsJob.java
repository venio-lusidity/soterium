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

package com.lusidity.jobs.server;

import com.lusidity.ClientConfiguration;
import com.lusidity.Environment;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.email.EmailX;
import com.lusidity.jobs.BaseJob;
import com.lusidity.server.AthenaServer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class ServerStatisticsJob extends BaseJob
{

	private static Collection<URI> failures=new ArrayList<>();

	// Constructors
	public ServerStatisticsJob(ProcessStatus processStatus)
	{
		super(processStatus);
	}

	// Overrides
	@Override
	public boolean start(Object... args)
	{
		try{
			if(Environment.getInstance().getDataStore().isStatisticsAvailable())
			{
				Environment.getInstance().getDataStore().getStatistics(false).save();
			}
		}catch (Exception ignored){}

		return true;
	}

	@Override
	public String getTitle()
	{
		return "Server Statistics";
	}

	@Override
	public String getDescription()
	{
		return "Periodically saves statistical data about data operations.";
	}

	@Override
	public boolean recordInLog()
	{
		return false;
	}

	@Override
	public boolean recordHistory()
	{
		return false;
	}

	private void handle(AthenaServer server, ClientConfiguration config)
	{
		try
		{
			boolean online = server.isOnline(config);

			if(!online){
				if(!ServerStatisticsJob.failures.contains(server.getHost()))
				{
					ServerStatisticsJob.failures.add(server.getHost());
					String subject = String.format("%s: offline", server.getHost());
					String message = String.format("%s is reporting that %s, %s, is offline.", Environment.getInstance().getConfig().getServerName(), server.getTitle(), server.getHost());
					EmailX.sendMail(EmailX.getDefaultServerKey(),
						EmailX.getDefaultFrom(), EmailX.getDefaultTo(), null, null, subject, message, true, null
					);
					Environment.getInstance().getReportHandler().warning(message);
				}
			}
			else{
				if(ServerStatisticsJob.failures.contains(server.getHost()))
				{
					ServerStatisticsJob.failures.remove(server.getHost());
					String subject = String.format("%s: online", server.getHost());
					String message = String.format("%s is reporting that %s, %s, is online.", Environment.getInstance().getConfig().getServerName(), server.getTitle(), server.getHost());
					EmailX.sendMail(EmailX.getDefaultServerKey(),
						EmailX.getDefaultFrom(), EmailX.getDefaultTo(), null, null, subject, message, true, null
					);
					Environment.getInstance().getReportHandler().info(message);
				}
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

}
