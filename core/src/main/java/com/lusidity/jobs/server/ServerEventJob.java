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

import com.lusidity.Environment;
import com.lusidity.collections.VertexIterator;
import com.lusidity.configuration.ServerEventConfiguration;
import com.lusidity.configuration.SoteriumConfiguration;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.system.event.ServerEvent;
import com.lusidity.jobs.BaseJob;
import org.joda.time.DateTime;

public class ServerEventJob extends BaseJob
{
	private VertexIterator iterator= null;
	private DateTime processedAt = null;

	// Constructors
	public ServerEventJob(ProcessStatus processStatus)
	{
		super(processStatus);
	}

	// Overrides
	@Override
	public boolean start(Object... args)
	{
		if((null==this.processedAt) || DateTime.now().minusMinutes(ServerEventConfiguration.getInstance().interval()).isAfter(this.processedAt)){
			BaseQueryBuilder qb =Environment.getInstance().getIndexStore().getQueryBuilder(ServerEvent.class, ServerEvent.class, 0, 0);
			qb.filter(BaseQueryBuilder.Operators.must, "serverName", BaseQueryBuilder.StringTypes.raw, SoteriumConfiguration.getInstance().getServerName());
			qb.sort("createdWhen", BaseQueryBuilder.Sort.asc);

			this.iterator= new VertexIterator(ServerEvent.class, qb, 0, 0);
			this.iterator.iterate(new ServerEventHandler(), this.getProcessStatus(), 0);
			this.processedAt = DateTime.now();
		}

		return true;
	}

	@Override
	public String getTitle()
	{
		return "Monitor Server Event";
	}

	@Override
	public String getDescription()
	{
		return "Monitors events from other servers.";
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

	@Override
	public boolean stop()
	{
		boolean result=super.stop();
		if (null!=this.iterator)
		{
			this.iterator.stop();
		}
		return result;
	}

}
