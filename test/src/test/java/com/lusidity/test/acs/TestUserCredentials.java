

package com.lusidity.test.acs;

import com.lusidity.Environment;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;
import org.restlet.Request;
import org.restlet.data.ClientInfo;

import java.net.URI;

public class TestUserCredentials implements UserCredentials
{
	private final Identity identity;
	private final Person person;

	// Constructors
	public TestUserCredentials(Identity identity, Person person){
		super();
		this.identity = identity;
		this.person = person;
	}

	@Override
	public Request getRequest()
	{
		return null;
	}

	@Override
	public Identity getIdentity()
	{
		return this.identity;
	}

	@Override
	public Boolean isRegistered()
	{
		return (null!=this.identity);
	}

	@Override
	public Boolean isValidated()
	{
		return !this.identity.fetchDeprecated().isTrue() && !this.person.fetchDeprecated().isTrue() && this.identity.hasStatus(Identity.Status.approved);
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
	public ClientInfo getClientInfo()
	{
		return null;
	}

	@Override
	public String getCommonName()
	{
		return this.person.getWesternName().toString();
	}

	@Override
	public String getFirstName()
	{
		return this.person.fetchFirstName().getValue();
	}

	@Override
	public String getIdentifier()
	{
		return this.identity.fetchId().getValue();
	}

	@Override
	public String getLastName()
	{
		return this.person.fetchLastName().getValue();
	}

	@Override
	public String getMiddleName()
	{
		return this.person.fetchMiddleName().getValue();
	}

	@Override
	public String getCountry()
	{
		return null;
	}

	@Override
	public String getOrigin()
	{
		return "Unit Test";
	}

	@Override
	public String getReferrer()
	{
		return Environment.getInstance().getConfig().getReferer();
	}

	@Override
	public String getOrganizationalUnit1()
	{
		return "Unit";
	}

	@Override
	public String getOrganizationalUnit2()
	{
		return "Test";
	}

	@Override
	public String getProvider()
	{
		return "Test";
	}

	@Override
	public URI getPrincipalUri()
	{
		return this.person.getUri();
	}

	@Override
	public JsonData toJson()
	{
		JsonData result = new JsonData();
		if(this.isServerCertificate()){
			result.put("commonName", this.getCommonName());
		}
		else{
			result.put("firstName", this.getFirstName());
			result.put("lastName", this.getLastName());
			result.put("middleName", this.getMiddleName());
		}
		result.put("identifier", this.getIdentifier());
		if(this.isRegistered() && (null!=this.getIdentity()))
		{
			result.put("approved", this.getIdentity().fetchStatus().getValue());
		}
		result.put("authenticated", this.isAuthenticated());
		result.put("validated", this.isValidated());
		result.put("registered", this.isRegistered());
		result.put("principalUri", this.getPrincipalUri());
		return result;
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
	public BasePrincipal getPrincipal()
	{
		return this.person;
	}
}
