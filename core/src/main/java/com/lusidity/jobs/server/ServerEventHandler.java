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
import com.lusidity.collections.IVertexHandler;
import com.lusidity.configuration.ServerEventConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.system.event.ServerEvent;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.jobs.server.events.IServerEventHandler;

import java.lang.reflect.Constructor;

public class ServerEventHandler implements IVertexHandler
{
	// Overrides
	@Override
	public boolean handle(DataVertex vertex, ProcessStatus processStatus, int on, int hits, int start, int limit)
	{
		if(vertex instanceof ServerEvent){
			boolean failed = false;
			ServerEvent serverEvent = (ServerEvent)vertex;
			try
			{
				if (this.valid(serverEvent))
				{
					//noinspection unchecked
					if((null==serverEvent.fetchHandler().getValue()) || !ClassX.isKindOf(serverEvent.fetchHandler().getValue(), IServerEventHandler.class)){
						throw new ApplicationException("Invalid object type, expected %s got %s.", IServerEventHandler.class.getSimpleName(), serverEvent.fetchHandler().getValue().getSimpleName());
					}

					@SuppressWarnings("unchecked")
					Constructor constructor=serverEvent.fetchHandler().getValue().getConstructor(ServerEvent.class);
					IServerEventHandler event = (IServerEventHandler) constructor.newInstance(serverEvent);
					serverEvent.fetchIdentifiers().add(ServerEvent.getUriValue());

					boolean delete = event.handle(processStatus);
					if (delete)
					{
						serverEvent.delete();
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
				failed = true;
			}
			this.cleanup(serverEvent, failed);
		}
		return false;
	}

	private boolean valid(ServerEvent serverEvent)
	{
		return ServerEventConfiguration.getInstance().isEnabled((Class<? extends IServerEventHandler>)serverEvent.fetchHandler().getValue());
	}

	private void cleanup(ServerEvent serverEvent, boolean failed)
	{
		// how do i know when to remove the event?
	}
}
