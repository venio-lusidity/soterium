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

package com.lusidity.domains.acs.security;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.people.Person;
import com.lusidity.email.EmailConfiguration;
import com.lusidity.email.EmailTemplate;
import com.lusidity.email.EmailX;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.security.ObfuscateX;
import com.lusidity.framework.text.StringX;
import com.lusidity.security.KeyDataIdentityStatusHandler;
import com.lusidity.system.security.cbac.ISecurityPolicy;
import com.lusidity.system.security.cbac.IdentityUpdatePolicy;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;


@AtSchemaClass(name="Identity", writable=true, discoverable=false)
public class Identity
	extends BaseDomain
{
	public enum Status
	{
		approved,
		inactive,
		disapproved,
		eval,
		waiting;
	}

	public enum LoginType
	{
		pki,
		token,
		apiKey
	}

	// Fields
	public static final int DEFAULT_USER_LOG_INTERVAL=60;
	public static final int INIT_ORDINAL=1120;
	// ------------------------------ FIELDS ------------------------------
	private KeyData<String> identifier=null;
	private KeyData<String> provider=null;
	private KeyData<DateTime> lastLoggedIn=null;
	private KeyData<DateTime> lastAttempt=null;
	private KeyData<DateTime> lastApproved=null;
	//private Identity.Status status = Identity.Status.waiting;
	//private Identity.LoginType loginType = Identity.LoginType.pki;
	private KeyData<Identity.Status> status=null;
	private KeyData<Identity.LoginType> loginType=null;
	@AtSchemaProperty(name="Permissions",
		expectedType=Person.class,
		fieldName="identities",
		labelType=Identity.class,
		direction=Common.Direction.IN,
		allowDeprecated = true,
		description="Get the principals that this identity is associated with.")
	private ElementEdges<BasePrincipal> principals=null;
	private boolean updating = false;

	// Constructors
	public Identity()
	{
		super();
	}

	@SuppressWarnings("unused")
	public Identity(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public JsonData toJson(boolean storing, Collection<? extends DataVertex> items)
	{
		JsonData result=super.toJson(storing, items);
		if (!storing)
		{
			result.remove("identifier");
		}
		else
		{
			result.remove("/common/base_contact_detail/contactDetails");
		}
		return result;
	}

	@Override
	public JsonData toJson(boolean storing, String... languages)
	{
		JsonData result=super.toJson(storing, languages);
		if (!storing)
		{
			result.remove("identifier");
		}
		else
		{
			result.remove("/common/base_contact_detail/contactDetails");
		}
		return result;
	}
// -------------------------- STATIC METHODS --------------------------

	@Override
	public PolicyDecisionPoint getSecurityPolicy(BasePrincipal principal, ISecurityPolicy... policies)
	{
		IdentityUpdatePolicy iup=new IdentityUpdatePolicy(principal, this);
		int size=((null!=policies) ? policies.length : 0)+1;
		ISecurityPolicy[] args=new ISecurityPolicy[size];
		args[size-1]=iup;
		return super.getSecurityPolicy(principal, args);
	}

	@SuppressWarnings("HardcodedLineSeparator")
	@Override
	public void afterUpdate(LogEntry.OperationTypes operationType, boolean success)
	{
		if(!this.updating)
		{
			this.updating = true;
			if (null==this.getCredentials())
			{
				this.setCredentials(new SystemCredentials());
			}

			if (null!=this.getBeforeData())
			{
				try
				{
					Identity before=new Identity(this.getBeforeData(), null);
					before.setCredentials(this.getCredentials());

					StringBuilder comment=new StringBuilder();
					comment.append(String.format("%s operation was performed by %s.\r\n", operationType, this.getCredentials().getPrincipal().fetchTitle().getValue()));
					JsonData deltas=ClassHelper.getDeltas(before, this);
					deltas.remove("identifier");
					deltas.remove("types");
					Collection<String> keys=deltas.keys();
					for (String key : keys)
					{
						if (StringX.equals(key, "title"))
						{
							JsonData item=deltas.getFromPath(key);
							String aValue=item.getString("after");
							aValue=StringX.isBlank(aValue) ? "null" : aValue;
							String prepended=String.format("Account: %s%s%s", aValue, "\r\n", comment.toString());
							comment=new StringBuilder();
							comment.append(prepended);
						}
						else
						{
							comment.append(System.lineSeparator());
							JsonData item=deltas.getFromPath(key);
							String bValue=item.getString("before");
							String aValue=item.getString("after");
							bValue=StringX.isBlank(bValue) ? "null" : bValue;
							aValue=StringX.isBlank(aValue) ? "null" : aValue;
							comment.append(String.format("property: %s before: %s after %s %s", key, bValue, aValue, "\r\n"));
						}
					}
					UserActivity.logActivity(this, operationType, comment.toString(), success);

					BasePrincipal principal=this.getPrincipal();
					if ((null!=principal) && ClassX.isKindOf(principal, Person.class))
					{
						String st=this.getBeforeData().getString("status");
						boolean log=true;

						Person person=(Person) principal;
						LogEntry.OperationTypes ot=null;
						if (this.fetchStatus().getValue()==Identity.Status.approved)
						{
							ot=LogEntry.OperationTypes.approved;
						}
						else if (this.fetchStatus().getValue()==Identity.Status.disapproved)
						{
							ot=LogEntry.OperationTypes.disapproved;
						}
						if (!StringX.isBlank(st))
						{
							try
							{
								LogEntry.OperationTypes bot=LogEntry.OperationTypes.valueOf(this.getBeforeData().getString("status"));
								log=bot!=ot;
							}
							catch (Exception ignored)
							{
							}
						}
						if (log && (null!=ot))
						{
							UserActivity.logActivity(person, ot,
								String.format("%s account was %s by %s", person.fetchTitle().getValue(), ot.toString(), person.getCredentials().getPrincipal().fetchTitle().getValue()), true
							);
						}
						this.notify(person, before, deltas);
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}

			this.setBeforeData(null);
		}

		this.updating = false;
	}

	/**
	 * Get principal associated with this identity.
	 *
	 * @return Associated principal, or null if none.
	 */
	@Override
	public BasePrincipal getPrincipal()
	{
		return (this.getPrincipals().isEmpty() ? null : this.getPrincipals().get());
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod"
	})
	private void notify(Person person, Identity before, JsonData deltas)
		throws Exception
	{
		person.setCredentials(this.getCredentials());
		String subject=Environment.getInstance().getConfig().getApplicationName();
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

			String message="";
			if (!this.hasStatus(Identity.Status.waiting))
			{
				Collection<String> keys=deltas.keys();
				boolean registered=((this.fetchStatus().getValue()==Identity.Status.approved) && (before.fetchStatus().getValue()==Identity.Status.waiting));
				boolean registeredDisapproved=((this.fetchStatus().getValue()==Identity.Status.disapproved) && (before.fetchStatus().getValue()==Identity.Status.waiting));
				boolean inactive=((this.fetchStatus().getValue()==Identity.Status.inactive) && (before.fetchStatus().getValue()!=Identity.Status.inactive));
				boolean disapproved=((this.fetchStatus().getValue()==Identity.Status.disapproved) && (before.fetchStatus().getValue()!=Identity.Status.disapproved));
				boolean approved=((this.fetchStatus().getValue()==Identity.Status.approved) && (before.fetchStatus().getValue()!=Identity.Status.approved));

				if (inactive)
				{
					EmailTemplate template=EmailConfiguration.getInstance().getTemplate("account_expired");
					if (null!=template)
					{
						subject = template.getSubject();
						message = template.getBody();
					}
					else
					{
						subject=String.format("%s: %s", subject, "Account Disabled");
						message="Your account has been flagged inactive due to inactivity and has been disabled.";
					}
				}
				else if (disapproved || registeredDisapproved)
				{
					EmailTemplate template=EmailConfiguration.getInstance().getTemplate(registeredDisapproved ? "account_denied" : "account_disapproved");
					if (null!=template)
					{
						subject = template.getSubject();
						message = template.getBody();
					}
					else
					{
						subject=String.format("%s: %s", subject, "Account Disapproved/Denied");
						message="Your account request has been disapproved or disabled.";
					}
				}
				else if (registered || approved)
				{
					EmailTemplate template=EmailConfiguration.getInstance().getTemplate(registered ? "account_approved" : "account_reactivation");
					if (null!=template)
					{
						subject=template.getSubject();
						message=template.getBody();
					}
					else
					{
						Environment.getInstance().getReportHandler().warning("Email template for \"account_approved\" is not configured.");
					}
					this.fetchLastApproved().setValue(DateTime.now());
					this.save();
				}
			}

			if (!StringX.isBlank(message))
			{
				EmailX.sendMail(EmailX.getDefaultServerKey(),
					EmailX.getDefaultFrom(), receivers, null, null, subject, message, true, null
				);
			}
		}
	}

	public ElementEdges<BasePrincipal> getPrincipals()
	{
		if (null==this.principals)
		{
			this.buildProperty("principals");
		}
		return this.principals;
	}

	public boolean hasStatus(Identity.Status expected)
	{
		return Objects.equals(expected, this.fetchStatus().getValue());
	}

	public KeyData<Identity.Status> fetchStatus()
	{
		if (null==this.status)
		{
			this.status=new KeyData<>(this, "status", Identity.Status.class, false, Identity.Status.waiting, new KeyDataIdentityStatusHandler());
		}
		return this.status;
	}

	@Override
	public void beforeUpdate(LogEntry.OperationTypes operationType)
	{
		if(!this.updating)
		{
			super.beforeUpdate(operationType);
			try
			{
				if ((null!=this.getCredentials()))
				{
					Identity identity=Environment.getInstance().getDataStore().getObjectById(Identity.class, Identity.class, this.fetchId().getValue(), true);
					this.setBeforeData(identity.getVertexData().clone());
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
	}

	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		Environment.getInstance().getConfig().initializePrincipals();
	}

	@Override
	public int getInitializeOrdinal()
	{
		return Identity.INIT_ORDINAL;
	}

// Methods

	/**
	 * Create a new identity.
	 *
	 * @param provider   Provider.
	 * @param identifier Provider-specific identifier.
	 * @param loginType  The login type for the identity.
	 * @return Newly-created Identity.
	 * @throws ApplicationException throws ApplicationException
	 */
	public static Identity create(String provider, String identifier, Identity.LoginType loginType)
		throws ApplicationException
	{
		Identity result=Identity.get(provider, identifier);
		if (null!=result)
		{
			throw new ApplicationException("Duplicate identity.");
		}

		try
		{
			String key=Identity.composeKey(provider, identifier);
			result=new Identity();
			result.fetchIdentifier().setValue(key);
			result.fetchProvider().setValue(provider);
			result.fetchLoginType().setValue(loginType);
			result.save();
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}

		return result;
	}

	/**
	 * Get an existing Identity.
	 *
	 * @param provider Provider.
	 * @param id       Provider-specific identifier.
	 * @return Identity matching the parameters, or null if no match.
	 */
	public static Identity get(String provider, String id)
	{
		String key=Identity.composeKey(provider, id);
		return VertexFactory.getInstance().getByPropertyExact(Identity.class, "identifier", key);
	}

	public static String composeKey(String provider, String identifier)
	{
		String result=null;
		try
		{
			String temp=String.format("%s:%s", provider, identifier);
			result=ObfuscateX.obfuscate(temp, Environment.getInstance().getSetting("encryption_key"), null);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	public KeyData<String> fetchIdentifier()
	{
		if (null==this.identifier)
		{
			this.identifier=new KeyData<>(this, "identifier", String.class, false, null);
		}
		return this.identifier;
	}

	public KeyData<String> fetchProvider()
	{
		if (null==this.provider)
		{
			this.provider=new KeyData<>(this, "provider", String.class, false, null);
		}
		return this.provider;
	}

	public KeyData<Identity.LoginType> fetchLoginType()
	{
		if (null==this.loginType)
		{
			this.loginType=new KeyData<>(this, "loginType", Identity.LoginType.class, false, Identity.LoginType.pki);
		}
		return this.loginType;
	}

	public static boolean isMatch(String apiKey, String provider, String identifier)
	{
		boolean result=false;
		try
		{
			String temp=String.format("%s:%s", provider, identifier);
			String match=ObfuscateX.obfuscate(temp, Environment.getInstance().getSetting("encryption_key"), null);
			result=StringX.equals(apiKey, match);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	@SuppressWarnings("unused")
	public boolean isLoginType(Identity.LoginType expected)
	{
		return Objects.equals(expected, this.fetchLoginType().getValue());
	}

	// Getters and setters
	public boolean isExpired()
	{
		int daysLeft=this.daysLeftBeforeExpiring();
		return ((daysLeft<=0) && Objects.equals(this.fetchStatus().getValue(), Identity.Status.approved));
	}

	public int daysLeftBeforeExpiring()
	{
		DateTime morning=DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
		@SuppressWarnings("NestedConditionalExpression")
		DateTime last = ((null==this.fetchLastApproved().getValue()) ? this.getLastLoggedIn() :
			(this.getLastLoggedIn().isAfter(this.fetchLastApproved().getValue()) ? this.getLastLoggedIn() : this.fetchLastApproved().getValue()));
		if(null==last){
			last = this.fetchCreatedWhen().getValue();
		}
		DateTime check=last.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
		return (Environment.getInstance().getConfig().getUserDaysInactive()-Days.daysBetween(check, morning).getDays());
	}

	public DateTime getLastLoggedIn()
	{
		if (null!=this.fetchLastLoggedIn().getValue())
		{
			return this.fetchLastLoggedIn().getValue();
		}
		else
		{
			return this.fetchLastLoggedIn().getDefaultValue();
		}
	}

	public KeyData<DateTime> fetchLastLoggedIn()
	{
		if (null==this.lastLoggedIn)
		{
			this.lastLoggedIn=new KeyData<>(this, "lastLoggedIn", DateTime.class, false, null);
		}
		return this.lastLoggedIn;
	}

	public KeyData<DateTime> fetchLastApproved()
	{
		if (null==this.lastApproved)
		{
			this.lastApproved=new KeyData<>(this, "lastApproved", DateTime.class, false, null);
		}
		return this.lastApproved;
	}

	public KeyData<DateTime> fetchLastAttempt()
	{
		if (null==this.lastAttempt)
		{
			this.lastAttempt=new KeyData<>(this, "lastAttempt", DateTime.class, false, null);
		}
		return this.lastAttempt;
	}

	public boolean isExpiring()
	{
		int daysLeft = this.daysLeftBeforeExpiring();
		int days = Environment.getInstance().getConfig().getUserDaysLeft();
		boolean result = false;
		if(daysLeft >=0){
		result = (daysLeft<=days) && Objects.equals(this.fetchStatus().getValue(), Identity.Status.approved);
		}

		return result;
	}

	public void setUpdating(boolean updating)
	{
		this.updating=updating;
	}
}