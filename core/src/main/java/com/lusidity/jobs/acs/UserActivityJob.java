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
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.IdentityVerification;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.edge.UserDirFilterHandler;
import com.lusidity.domains.people.Person;
import com.lusidity.email.EmailConfiguration;
import com.lusidity.email.EmailTemplate;
import com.lusidity.email.EmailX;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.io.ScopedDirectory;
import com.lusidity.jobs.BaseJob;
import com.lusidity.jobs.IJob;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public class UserActivityJob extends BaseJob
{
// Fields
	public static final int DEFAULT_INTERVAL=60;
	private static final int DEFAULT_LIMIT=1000;
	private DateTime lastChecked=null;

	// Constructors
	public UserActivityJob(ProcessStatus processStatus)
	{
		super(processStatus);
	}

// Overrides
	@SuppressWarnings({
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	@Override
	public boolean start(Object... args)
	{
		this.setStatus(IJob.Status.processing);
		DateTime morning=DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
		if ((null==this.lastChecked) || this.lastChecked.plusMinutes(UserActivityJob.DEFAULT_INTERVAL).isBeforeNow())
		{
			try
			{
				Environment.getInstance().getReportHandler().info("Checking accounts for inactivity.");
				this.lastChecked=DateTime.now();
				int start=0;
				BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(Identity.class, Identity.class, start, UserActivityJob.DEFAULT_LIMIT);
				qb.matchAll();
				qb.sort("lastLoggedIn", BaseQueryBuilder.Sort.desc);
				boolean process=true;
				while (process && !this.isStopping())
				{
					qb.setStart(start);
					QueryResults results=qb.execute();
					start=results.getNext();
					process=results.getHits()>results.getNext();
					for (IQueryResult result : results)
					{
						try
						{
							Identity identity=result.getVertex();
							BasePrincipal principal=identity.getPrincipal();
							if (null==principal)
							{
								try
								{
									Environment.getInstance().getReportHandler().warning("A dead identity was deleted.");
									identity.delete();
								}
								catch (Exception ignored){}
							}
							else if (identity.hasStatus(Identity.Status.approved))
							{
								this.check(identity, principal);
							}
							else {
								this.invalidate(identity, principal);
							}
							Collection<ScopedDirectory> directories = ScopedConfiguration.getInstance().getScopedDirectories();
							UserDirFilterHandler ffh = new UserDirFilterHandler("cbac", directories, principal);
							Environment.getInstance().getPrincipalFilterCache().reset(principal, ffh);
						}
						catch (Exception ex){
							Environment.getInstance().getReportHandler().warning(ex);
						}
					}
					qb.setStart(results.getNext());
				}
				this.setStatus(IJob.Status.processed);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
				this.setStatus(IJob.Status.failed);
			}
		}

		return true;
	}

	private void invalidate(Identity identity, BasePrincipal principal)
		throws Exception
	{
		int daysLeft=identity.daysLeftBeforeExpiring();
		if(daysLeft<0){
			daysLeft*=-1;
			int days = (Environment.getInstance().getConfig().getUserDaysInactive()+daysLeft);
			if(days>=Environment.getInstance().getConfig().getUserIdentityDaysInactive()){
				identity.setUpdating(true);
				identity.fetchStatus().setValue(Identity.Status.eval);
				identity.fetchDeprecated().setValue(true);
				identity.save();
				identity.setUpdating(false);
				this.updateIdentityVerification(principal);
			}
		}
	}

	private void updateIdentityVerification(BasePrincipal principal) throws Exception {
		String pUri = principal.getUri().toString();
		IdentityVerification verification = IdentityVerification.getOrCreate(pUri, this.getProcessStatus());
		if(verification.hasId()){
			verification.fetchStatus().setValue(IdentityVerification.Status.invalidated);
			verification.fetchD787().setValue(false);
			verification.fetchD2875().setValue(false);
		}
		else{
			verification.fetchRelatedId().setValue(pUri);
			verification.fetchStatus().setValue(IdentityVerification.Status.invalidated);
			verification.fetchD787().setValue(false);
			verification.fetchD2875().setValue(false);
		}
		verification.save();
	}

	private void check(Identity identity, BasePrincipal principal)
		throws Exception
	{
		if(identity.hasStatus(Identity.Status.approved) && identity.fetchDeprecated().getValue()){
			this.repair(identity, principal);
		}

		boolean expired = identity.isExpired();
		boolean expiring = identity.isExpiring();
		int days = identity.daysLeftBeforeExpiring();
		if (expired)
		{
			try
			{
				if (!identity.fetchDeprecated().getValue() && identity.hasStatus(Identity.Status.approved))
				{
					identity.fetchStatus().setValue(Identity.Status.inactive);
					identity.fetchDeprecated().setValue(true);
					identity.setCredentials(SystemCredentials.getInstance());
					identity.save();
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		else if(expiring){
			String subject;
			String message;
			EmailTemplate template=EmailConfiguration.getInstance().getTemplate("account_expiring");
			if (null!=template)
			{
				subject = template.getSubject();
				message = template.getBody();
				message = StringX.replace(message, "[DAYS]", String.valueOf(days));
			}
			else
			{
				subject = String.format("%s: Account", Environment.getInstance().getConfig().getApplicationName());
				message = String.format("Your account will be disabled in %d days.  Please log in before then to avoid your account being disabled.", days);
			}
			this.sendMessage(identity, subject, message);
		}
	}

	private void repair(Identity identity, BasePrincipal principal)
		throws Exception
	{
		if (identity.fetchDeprecated().isTrue())
		{
			identity.fetchDeprecated().setValue(false);
			identity.save();
		}
		Edge edge=principal.getEdgeHelper().getEdge(Edge.class, identity, principal.getIdentities().getKey(), Common.Direction.OUT);
		if ((null!=edge) && edge.fetchDeprecated().isTrue())
		{
			edge.fetchDeprecated().setValue(false);
			edge.save();
		}
	}

	private void sendMessage(Identity identity, String subject, String message){
		Person person =(Person) identity.getPrincipal();

		Collection<Email> emails=new ArrayList<>();
		for (BaseContactDetail detail : person.getContactDetails())
		{
			if (ClassX.isKindOf(detail, Email.class))
			{
				emails.add((Email) detail);
			}
		}

		if (!emails.isEmpty())
		{
			String[] receivers=new String[emails.size()];
			int on=0;
			for (Email email : emails)
			{
				receivers[on]=email.fetchValue().getValue();
				on++;
			}

			EmailX.sendMail(EmailX.getDefaultServerKey(),
				EmailX.getDefaultFrom(), receivers, null, null, subject, message, true, null
			);
		}
	}

	@Override
	public String getTitle()
	{
		return "User Activity";
	}

	@Override
	public String getDescription()
	{
		return "Ensures that all users are reasonably active.  If they are not they will be disabled.";
	}

}
