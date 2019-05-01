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

import com.lusidity.Environment;
import com.lusidity.collections.IVertexHandler;
import com.lusidity.configuration.SplunkLoggingConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.json.JsonData;

public class SplunkLogHandler implements IVertexHandler
{
	private final JsonData queue;
	private final int batchSize;

	// Constructors
	public SplunkLogHandler(JsonData queue)
	{
		super();
		this.queue= queue;
		this.batchSize = SplunkLoggingConfiguration.getInstance().getBatchSize();
	}

	// Overrides
	@Override
	public boolean handle(DataVertex vertex, ProcessStatus processStatus, int on, int hits, int start, int limit)
	{
		boolean failed = false;
		if(vertex instanceof UserActivity){
			UserActivity activity = (UserActivity)vertex;
			JsonData item = activity.toJson(false);

			// if supposed to be inside the activity item...
			// { event: { user activity..., source: "herclues" }}
			//item.put("source", Environment.getInstance().getConfig().getServerName());

			JsonData data = JsonData.createObject();
			// additional Splunk fields add here.
			data.put("event", item);
			// if supposed to be parallel to the event/activity item
			// { event: { event 1 }, source: "hercules"}
			data.put("source", Environment.getInstance().getConfig().getServerName());

			this.queue.getFromPath("batched").put(data);

			if((on % this.batchSize)==0){
				try
				{
					JsonData batched = this.queue.getFromPath("batched");
					failed = !SplunkUserLogging.post(batched);
					if(!failed){
						this.queue.remove("batched");
						this.queue.put("batched", JsonData.createArray());
					}
				}
				catch (Exception e)
				{
					failed = true;
					Environment.getInstance().getReportHandler().warning(e);
				}
				this.queue.update("failed", failed);
			}
		}
		return failed;
	}
}
