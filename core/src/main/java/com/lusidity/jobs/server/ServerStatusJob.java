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

import com.lusidity.AuthorizationResponse;
import com.lusidity.ClientConfiguration;
import com.lusidity.Environment;
import com.lusidity.RegisterResponse;
import com.lusidity.configuration.ServerEventConfiguration;
import com.lusidity.configuration.SoteriumConfiguration;
import com.lusidity.data.interfaces.BaseServerConfiguration;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.email.EmailConfiguration;
import com.lusidity.email.EmailX;
import com.lusidity.jobs.BaseJob;
import com.lusidity.server.AthenaServer;
import org.joda.time.DateTime;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class ServerStatusJob extends BaseJob
{
	private DateTime processedAt = null;

	private static Collection<URI> offline=new ArrayList<>();

	// Constructors
	public ServerStatusJob(ProcessStatus processStatus)
	{
		super(processStatus);
	}

	// Overrides
	@Override
	public boolean start(Object... args)
	{
		if((null==this.processedAt) || DateTime.now().minusMinutes(SoteriumConfiguration.getInstance().getTimeCheckServers()).isAfter(this.processedAt)){
			Collection<AthenaServer> servers = SoteriumConfiguration.getInstance().getAthenaServers();

			try
			{
				String path = SoteriumConfiguration.getInstance().getKeyStoreConfigPath();
				ClientConfiguration config=null;
				File file =BaseServerConfiguration.getFile(path);

				if(file.exists()){
					config = new ClientConfiguration(file);
				}
				if((null!=config) && config.isValid())
				{
					AuthorizationResponse authorizationResponse=AuthorizationResponse.authenticate(config);

					if(null!=authorizationResponse)
					{
						if (!authorizationResponse.isRegistered())
						{
							// now register the API key.
							RegisterResponse ipr=RegisterResponse.post(config);
							if((null==ipr) || !ipr.isRegistered()){
								Environment.getInstance().getReportHandler().warning("%s failed to register the API key.", this.getClass().getSimpleName());
							}
						}
						else if (authorizationResponse.isAuthenticated())
						{
							for (AthenaServer server : servers)
							{
								if((null!=server) && server.isValid() && !AthenaServer.getServers().contains(server)){
									AthenaServer.getServers().add(server);
								}
							}

							Collection<AthenaServer> removals = new ArrayList<>();
							for(AthenaServer server: AthenaServer.getServers()){
								if(!servers.contains(server)){
									removals.add(server);
								}
							}

							for(AthenaServer remove: removals){
								AthenaServer.getServers().remove(remove);
							}

							for (AthenaServer server: AthenaServer.getServers()){
								this.handle(server, config);
							}
						}
					}
				}
				this.processedAt = DateTime.now();
			}
			catch (Exception ex){
				AthenaServer.getServers().clear();
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}

		return true;
	}

	private void handle(AthenaServer server, ClientConfiguration config)
	{
		try
		{
			boolean online = server.isOnline(config);

			if(!online){
				if(!ServerStatusJob.offline.contains(server.getHost()))
				{
					ServerStatusJob.offline.add(server.getHost());
					String subject = String.format("%s: offline", server.getHost());
					String message = String.format("%s is reporting that %s, %s, is offline.", Environment.getInstance().getConfig().getServerName(), server.getTitle(), server.getHost());
					if(!EmailConfiguration.getInstance().isDisabled())
					{
						EmailX.sendMail(EmailX.getDefaultServerKey(),
							EmailX.getDefaultFrom(), EmailX.getDefaultTo(), null, null, subject, message, true, null
						);
					}
					Environment.getInstance().getReportHandler().warning(message);
				}
			}
			else{
				if(ServerStatusJob.offline.contains(server.getHost()))
				{
					ServerStatusJob.offline.remove(server.getHost());
					String subject = String.format("%s: online", server.getHost());
					String message = String.format("%s is reporting that %s, %s, is online.", Environment.getInstance().getConfig().getServerName(), server.getTitle(), server.getHost());
					if(!EmailConfiguration.getInstance().isDisabled())
					{
						EmailX.sendMail(EmailX.getDefaultServerKey(),
							EmailX.getDefaultFrom(), EmailX.getDefaultTo(), null, null, subject, message, true, null
						);
					}
					Environment.getInstance().getReportHandler().info(message);
				}
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	@Override
	public String getTitle()
	{
		return "Server Status Check";
	}

	@Override
	public String getDescription()
	{
		return "Retrieves a list of servers from the configuration file and checks to see if they are available." +
		       "  If not available it will be recorded in the log and an email will be sent.";
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

}
