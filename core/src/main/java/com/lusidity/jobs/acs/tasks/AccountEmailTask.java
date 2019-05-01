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

package com.lusidity.jobs.acs.tasks;

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.domains.people.Person;
import com.lusidity.email.EmailMessage;
import com.lusidity.email.EmailX;
import com.lusidity.framework.text.StringX;

import java.util.Collection;
import java.util.concurrent.Callable;

public class AccountEmailTask implements Callable<Boolean>
{
	private final BasePrincipal principal;
	private final LogEntry.OperationTypes operationType;
	private final EmailMessage message;

	public AccountEmailTask(BasePrincipal principal, EmailMessage message, LogEntry.OperationTypes operationType)
	{
		super();
		this.principal = principal;
		this.operationType = operationType;
		this.message = (null==message) ? new EmailMessage(null, null) : message;
	}

	@Override
	public Boolean call()
		throws Exception
	{
		try
		{
			if(this.principal instanceof PersonnelPosition){
				PersonnelPosition pp =(PersonnelPosition) this.principal;
				for(BasePrincipal bp: pp.getPrincipals()){
					if (bp instanceof Person)
					{
						this.notify((Person) bp);
					}
				}
			}

		}
		catch (Exception ignored){}
		return true;
	}

	private void notify(Person person)
	{
		try
		{
			boolean notify=false;
			for (Identity identity : person.getIdentities())
			{
				if (!identity.fetchDeprecated().getValue() && (identity.fetchStatus().getValue()==Identity.Status.approved))
				{
					notify=true;
					break;
				}
			}
			if (notify)
			{
				Collection<Email> emails=this.principal.getEmailAddresses(BaseContactDetail.CategoryTypes.work_email);

				if (!emails.isEmpty())
				{
					String subject=StringX.isBlank(this.message.getSubject()) ? String.format("Permissions: %s", this.operationType.toString()) : this.message.getSubject();
					String body=StringX.isBlank(this.message.getBody()) ? "Your account permissions have been modified." : this.message.getBody();

					String[] receivers=new String[emails.size()];
					int on=0;
					for (Email email : emails)
					{
						receivers[on]=email.fetchValue().getValue();
						on++;
					}

					EmailX.sendMail(EmailX.getDefaultServerKey(),
						EmailX.getDefaultFrom(), receivers, null, null, subject, body, true, null
					);
				}
			}
		}
		catch (Exception ignored){}
	}
}
