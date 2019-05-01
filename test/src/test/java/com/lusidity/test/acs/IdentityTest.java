

package com.lusidity.test.acs;

import com.lusidity.Environment;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.people.Person;
import com.lusidity.email.EmailX;
import com.lusidity.framework.java.ClassX;
import com.lusidity.test.BaseTest;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class IdentityTest extends BaseTest
{
	private static final String IDENTIFIER= "3333333333";
	private static final String PROVIDER= "x509";

	// Overrides
	@Override
	public boolean isDisabled()
	{
		return false;
	}

	@Test
	public void expired(){
		try{
			Identity identity = Identity.get(IdentityTest.PROVIDER, IdentityTest.IDENTIFIER);
			this.check(identity, false, false);
			identity.fetchLastLoggedIn().setValue(DateTime.now().minusDays(25));
			this.check(identity, true, false);
			identity.fetchLastLoggedIn().setValue(DateTime.now().minusDays(30));
			this.check(identity, true, false);
			// account should be disabled here.
			identity.fetchLastLoggedIn().setValue(DateTime.now().minusDays(31));
			this.check(identity, false, true);
		}
		catch (Exception ex){
			System.out.println("");
		};

	}

	private void check(Identity identity, boolean shouldBeExpiring, boolean shouldBeExpired){
		boolean expired = identity.isExpired();
		Assert.assertEquals("Wrong value.", shouldBeExpired, expired);
		boolean expiring = identity.isExpiring();
		Assert.assertEquals("Wrong value.", shouldBeExpiring, expiring);
		int days = identity.daysLeftBeforeExpiring();
		if (expired)
		{
			try
			{
				if (!identity.fetchDeprecated().getValue() && identity.hasStatus(Identity.Status.approved))
				{
					identity.fetchStatus().setValue(Identity.Status.disapproved);
					identity.fetchDeprecated().setValue(true);
					identity.save();

					String subject=String.format("%s: Account", Environment.getInstance().getConfig().getApplicationName());
					String message = "Your account account has been disabled do to inactivity.";
					this.sendMessage(identity, subject, message);
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		else if(expiring){
			String subject=String.format("%s: Account", Environment.getInstance().getConfig().getApplicationName());
			String message = String.format("Your account will be disabled in %d days.  Please log in before then to avoid your account being disabled.", days);
			this.sendMessage(identity, subject, message);
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
}
