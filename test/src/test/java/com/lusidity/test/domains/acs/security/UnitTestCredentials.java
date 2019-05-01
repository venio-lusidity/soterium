

package com.lusidity.test.domains.acs.security;

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;
import org.restlet.Request;
import org.restlet.data.ClientInfo;

import java.net.URI;

public class UnitTestCredentials implements UserCredentials
{
	private BasePrincipal principal=null;

// Overrides
	@Override
	public Boolean isRegistered()
	{
		return this.getPrincipal().isAdmin(true);
	}

	@Override
	public Boolean isValidated()
	{
		return this.getPrincipal().isAdmin(true);
	}

	@Override
	public Boolean isAuthenticated()
	{
		return true;
	}

	@Override
	public Boolean isServerCertificate()
	{
		return false;
	}

	@Override
	public JsonData toJson()
	{
		return null;
	}

	private LogEntry.OperationTypes activity = UserActivity.OperationTypes.none;

	@Override
	public UserActivity.OperationTypes getActivity()
	{
		return this.activity;
	}

	@Override
	public void setActivity(UserActivity.OperationTypes activity)
	{
		this.activity = activity;
	}

	@Override
	public Request getRequest()
	{
		return null;
	}

	@Override
	public Identity getIdentity()
	{
		return null;
	}

	@Override
	public ClientInfo getClientInfo()
	{
		return null;
	}

	@Override
	public String getCommonName()
	{
		return "Anonymous";
	}

	@Override
	public String getFirstName()
	{
		return "Anonymous";
	}

	@Override
	public String getIdentifier()
	{
		return "AnonymousAnonymousAnonymous";
	}

	@Override
	public String getLastName()
	{
		return "user";
	}

	@Override
	public String getMiddleName()
	{
		return null;
	}

	@Override
	public String getCountry()
	{
		return "US";
	}

	@Override
	public String getOrigin()
	{
		return "System";
	}

	@Override
	public String getReferrer()
	{
		return "System";
	}

	@Override
	public String getOrganizationalUnit1()
	{
		return "System";
	}

	@Override
	public String getOrganizationalUnit2()
	{
		return "System";
	}

	@Override
	public String getProvider()
	{
		return "local";
	}

	@Override
	public URI getPrincipalUri()
	{
		return null;
	}

	@Override
	public BasePrincipal getPrincipal()
	{
		if (null==this.principal)
		{
			try
			{
				this.principal=new Person();
				this.principal.fetchTitle().setValue("admin");
				this.principal.save();
				Group group=Group.getGroup("admin");
				group.getPrincipals().add(this.principal);
				this.principal.setCredentials(this);
			}
			catch (Exception ignore)
			{

			}
		}
		return this.principal;
	}
}
