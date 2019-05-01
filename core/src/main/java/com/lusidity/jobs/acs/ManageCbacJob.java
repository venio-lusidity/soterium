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
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.edge.UserDirFilterHandler;
import com.lusidity.domains.system.assistant.message.CbacMessage;
import com.lusidity.email.EmailMessage;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.io.ScopedDirectory;
import com.lusidity.jobs.BaseJob;
import com.lusidity.jobs.acs.tasks.AccountEmailTask;
import com.lusidity.system.security.authorization.IAuthorizationPolicy;
import com.lusidity.tasks.TaskManager;
import com.lusidity.workers.importer.BaseFileImporter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

public class ManageCbacJob extends BaseJob
{
    private static boolean busy = false;
	Collection<BaseFileImporter> importers=new ArrayList<>();
	private TaskManager tm = new TaskManager();
	private Collection<Future<Boolean>> futures = new ArrayList<>();

	// Constructors
	public ManageCbacJob(ProcessStatus processStatus)
	{
		super(processStatus);
	}

	// Overrides
	@Override
	public boolean start(Object... args)
	{
		if (!ManageCbacJob.isBusy())
		{
			ManageCbacJob.setBusy(true);
			try
			{
				this.tm.startFixed(2);

				CbacMessages instance=CbacMessages.getInstance();
				Collection<CbacMessage> messages=new ArrayList<>();
				for (CbacMessage message : instance.getMessages())
				{
					if (null!=message)
					{
						messages.add(message);
					}
				}

				for (CbacMessage message : messages)
				{
					if (null!=message)
					{
						try
						{
							this.handle(message);
						}
						catch (Exception ex)
						{
							ReportHandler.getInstance().warning(ex);
						}
						finally
						{
							instance.getMessages().remove(message);
							message.delete();
						}
					}
				}
				this.tm.callAndWait(this.futures);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
			finally
			{
				ManageCbacJob.setBusy(false);
			}
		}

		return true;
	}

	@Override
	public String getTitle()
	{
		return "CBAC Manager";
	}

	@Override
	public String getDescription()
	{
		return "Processes all CRUD operations for CBAC permissions.  Should be continuously running in the background on any Athena server.";
	}

	private static synchronized boolean isBusy()
	{
		return ManageCbacJob.busy;
	}

	protected void handle(CbacMessage message)
		throws Exception
	{
		DataVertex context=VertexFactory.getInstance().getById(message.fetchContextRelatedId().getValue());
		BasePrincipal principal=VertexFactory.getInstance().getById(message.fetchPositionRelatedId().getValue());
		LogEntry.OperationTypes operationType=message.fetchOperationType().getValue();
		Class<? extends IAuthorizationPolicy> policyCls=message.fetchPolicy().getValue();

		if ((null!=context) && (null!=principal) && (null!=operationType) && (null!=policyCls))
		{
			Constructor constructor=policyCls.getConstructor();
			IAuthorizationPolicy policy=(IAuthorizationPolicy) constructor.newInstance();

			EmailMessage msg = null;
			//noinspection SwitchStatementWithoutDefaultBranch,EnumSwitchStatementWhichMissesCases
			switch (operationType)
			{
				case create:
					msg = policy.create(context, principal, this.getProcessStatus());
					break;
				case update:
					msg = policy.update(context, principal, this.getProcessStatus());
					break;
				case delete:
					msg = policy.delete(context, principal, this.getProcessStatus());
					break;
			}

			Collection<ScopedDirectory> directories = ScopedConfiguration.getInstance().getScopedDirectories();
			UserDirFilterHandler ffh = new UserDirFilterHandler("cbac", directories, principal);
			Environment.getInstance().getPrincipalFilterCache().reset(principal, ffh);

			AccountEmailTask task = new AccountEmailTask(principal, msg, operationType);
			Future<Boolean> future = this.tm.submit(task);
			this.futures.add(future);
		}
	}

	public static synchronized void setBusy(boolean busy)
	{
		ManageCbacJob.busy=busy;
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
