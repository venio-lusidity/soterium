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

package com.lusidity.apollo.elasticSearch.jobs;

import com.lusidity.Environment;
import com.lusidity.collections.VertexIterator;
import com.lusidity.data.ApolloVertex;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.jobs.BaseJob;
import org.joda.time.DateTime;

import java.util.Set;

public class ReindexJob extends BaseJob
{
	private static DateTime lastRun=null;
	private VertexIterator iterator=null;

	// Constructors
	public ReindexJob(ProcessStatus processStatus)
	{
		super(processStatus);
	}

	// Overrides
	@Override
	public boolean start(Object... args)
	{
		try
		{
			/*
			 todo: re-index
			 have to map class property indexes @AtIndexedField() particularly the lid
			 figure out a way to pull from existing and yet push to new version
			 apply version to index store and partition, account for null which is version 0
			 Is there a way to use the AtIndexedField to specify version for backwards compatibility?
			 data.config contains the production version
			 jobs.config ...ReindexJob contains reindex version.
			 */
			Set<Class<? extends ApolloVertex>> vertices=Environment.getInstance().getReflections().getSubTypesOf(ApolloVertex.class);
			for (Class<? extends ApolloVertex> cls : vertices)
			{
				if (!(ClassX.isAbstract(cls) && ClassX.isInterface(cls)))
				{
					continue;
				}
				this.iterator=new VertexIterator(cls);
				ReindexHandler handler=new ReindexHandler();
				this.iterator.iterate(handler, this.getProcessStatus(), this.getMaxThreads());
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning(ex);
		}
		return true;
	}

	@Override
	public String getTitle()
	{
		return "Reindex";
	}

	@Override
	public String getDescription()
	{
		return "Reindex all vertices into a new index version.";
	}

	@Override
	public boolean stop()
	{
		boolean result=true;
		if (null!=this.iterator)
		{
			this.iterator.stop();
		}
		return result;
	}
}
