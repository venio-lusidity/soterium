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
import com.lusidity.collections.PrincipalEdges;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.authorization.AuthorizationRule;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.object.edge.AuthorizedEdge;
import com.lusidity.domains.object.edge.PrincipalEdge;
import com.lusidity.domains.object.edge.UserActivityEdge;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.security.IPrincipal;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({
	"FieldMayBeFinal",
	"BooleanParameter"
})
@AtSchemaClass(name="Principal", discoverable=false)
public
abstract class BasePrincipal
	extends BaseDomain implements IPrincipal
{
	@AtSchemaProperty(name="User Activity", expectedType=UserActivity.class, discoverable=false, edgeType = UserActivityEdge.class)
	private ElementEdges<UserActivity> userActivities=null;

	@AtSchemaProperty(name="Principals", expectedType=BasePrincipal.class,
		description="Principals associated to this object.")
	private PrincipalEdges<BasePrincipal> principals=null;
	@AtSchemaProperty(name="Parent Principals",
		labelType=BasePrincipal.class,
		expectedType=BasePrincipal.class,
		edgeType=PrincipalEdge.class,
		fieldName="principals",
		description="Principals associated to this object.",
		direction=Common.Direction.IN)
	private PrincipalEdges<BasePrincipal> parentPrincipals=null;

	@AtSchemaProperty(name="Identities", expectedType=Identity.class,description="Identities associated with this principal.", allowDeprecated = true)
	private ElementEdges<Identity> identities=null;

	@AtSchemaProperty(name="Authorizations",
		labelType=BasePrincipal.class,
		expectedType=DataVertex.class,
		edgeType=AuthorizedEdge.class,
		fieldName="authorized",
		description="Vertices this principal has some permission to.",
		direction=Common.Direction.IN)
	private ElementEdges<DataVertex> authorizations=null;
	private Boolean privileged = null;

	// Constructors
	public BasePrincipal()
	{
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param dso Underlying dynamic object.
	 */
	public BasePrincipal(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

    // Overrides
	@Override
	public boolean logActivity(UserActivity userActivity)
	{
		boolean result=(null!=userActivity) && BasePrincipal.isUser(this);
		if (result)
		{
			result=this.getUserActivities().add(userActivity);
		}
		return result;
	}

	public static boolean isUser(BasePrincipal principal)
	{
		//noinspection ClassReferencesSubclass
		return (null!=principal) && (!((principal instanceof SystemUser) || (principal instanceof AnonymousUser)));
	}

	public ElementEdges<UserActivity> getUserActivities()
	{
		if (null==this.userActivities)
		{
			this.buildProperty("userActivities");
		}
		return this.userActivities;
	}

	public boolean isAdmin(boolean validateCredentials)
	{
		return this.checkPermissions(validateCredentials, AuthorizationRule.RuleTypes.admin);
	}

	public boolean checkPermissions(boolean validateCredentials, AuthorizationRule.RuleTypes perm)
	{
		boolean configResult=this.isValidated(validateCredentials, perm);

		boolean webResult=false;
		if (Environment.getInstance().getWebServer()!=null)
		{
			webResult=!Environment.getInstance().getWebServer().credentialsRequired();
		}
		return configResult || webResult;

	}

	private boolean isValidated(boolean validateCredentials, AuthorizationRule.RuleTypes ruleType)
	{
		// Working with principals cached can cause problems because if one user set the credentials
		// and another user sets the credentials last in wins, so principals cannot be cached using EchoCache.
		// Redis cache should not have this issue as each object is another instance of the same object.
		// The below setting of credentials is a hack mainly for development
		// where principals may be cached using EchoCache.
		UserCredentials userCredentials=this.getCredentials();
		// The context permissions are not implemented yet so just pass null here.
		boolean result=AuthorizationRule.validate(this, ruleType);

		this.setCredentials(userCredentials);

		if (validateCredentials)
		{
			result=((null!=this.getCredentials()) && this.getCredentials().isAuthenticated());
		}

		this.setCredentials(userCredentials);

		return result;
	}

	@SuppressWarnings("unused")
	public boolean canComment(boolean validateCredentials)
	{
		return this.checkPermissions(validateCredentials, AuthorizationRule.RuleTypes.comment);
	}

	public boolean canDelete(boolean validateCredentials)
	{
		return this.checkPermissions(validateCredentials, AuthorizationRule.RuleTypes.delete);

	}

	public boolean canRead(boolean validateCredentials)
	{
		return this.checkPermissions(validateCredentials, AuthorizationRule.RuleTypes.read);
	}

	public boolean canWrite(boolean validateCredentials)
	{
		return this.checkPermissions(validateCredentials, AuthorizationRule.RuleTypes.write);
	}

	public boolean isDenied(boolean validateCredentials)
	{
		boolean result=AuthorizationRule.validate(this, AuthorizationRule.RuleTypes.deny);
		if (!result && validateCredentials)
		{
			result=!((null!=this.getCredentials()) && (null!=this.getCredentials().getPrincipal()) &&
			         this.getCredentials().isAuthenticated());
		}
		return result;
	}

	public Collection<Email> getEmailAddresses(BaseContactDetail.CategoryTypes categoryType)
	{
		// This will prevent infinite recursion.
		Collection<BasePrincipal> checked=new ArrayList<>();
		Collection<Email> emails = this.getEmailAddresses(checked, categoryType);
		Collection<Email> results = new ArrayList<>();
		for(Email actual: emails){
			boolean found = false;
			for(Email expected: results){
				found = StringX.equalsIgnoreCase(expected.fetchValue().getValue(), actual.fetchValue().getValue());
				if(found){
					break;
				}
			}
			if(!found){
				results.add(actual);
			}
		}
		return results;
	}

	private Collection<Person> getPeople(Collection<BasePrincipal> checked)
	{
		Collection<Person> results=new ArrayList<>();
		this.getPerson(checked, this, results);
		checked.add(this);
		for (BasePrincipal principal : this.getPrincipals())
		{
			if (!checked.contains(principal))
			{
				this.getPerson(checked, principal, results);
				// Ensure to pass the checked to prevent infinite recursion.
				Collection<Person> items=principal.getPeople(checked);
				if (null!=items)
				{
					results.addAll(items);
				}
			}
		}
		return results;
	}

	@SuppressWarnings("ClassReferencesSubclass")
	private void getPerson(Collection<BasePrincipal> checked, BasePrincipal principal, Collection<Person> results)
	{
		if (!checked.contains(principal) && ClassX.isKindOf(principal, Person.class))
		{
			Person person=(Person) principal;
			if (!results.contains(person))
			{
				checked.add(person);
				results.add(person);
			}
		}
	}

	private Collection<Email> getEmailAddresses(Collection<BasePrincipal> checked, BaseContactDetail.CategoryTypes categoryType)
	{
		Collection<Email> results=new ArrayList<>();
		this.getEmailAddresses(checked, categoryType, this, results);
		checked.add(this);
		for (BasePrincipal principal : this.getPrincipals())
		{
			if (!checked.contains(principal))
			{
				this.getEmailAddresses(checked, categoryType, principal, results);
				// Ensure to pass the checked to prevent infinite recursion.
				Collection<Email> items=principal.getEmailAddresses(checked, categoryType);
				if (null!=items)
				{
					results.addAll(items);
				}
			}
		}
		return results;
	}

	@SuppressWarnings("ClassReferencesSubclass")
	private void getEmailAddresses(Collection<BasePrincipal> checked, BaseContactDetail.CategoryTypes categoryType, BasePrincipal principal, Collection<Email> results)
	{
		if (!checked.contains(principal) && ClassX.isKindOf(principal, Person.class))
		{
			Person person=(Person) principal;
			for (BaseContactDetail contactDetail : person.getContactDetails())
			{
				if (ClassX.isKindOf(contactDetail, Email.class) && (contactDetail.fetchCategory().getValue()==categoryType) && !StringX.isBlank(contactDetail.fetchValue().getValue()))
				{
					results.add((Email) contactDetail);
				}
			}
		}
	}

// Getters and setters
public ElementEdges<DataVertex> getAuthorizations()
{
	if (null==this.authorizations)
	{
		this.buildProperty("authorizations");
	}
	return this.authorizations;
}

	public PrincipalEdges<BasePrincipal> getPrincipals()
	{
		if ((null==this.principals))
		{
			this.buildProperty("principals");
		}
		return this.principals;
	}

	public PrincipalEdges<BasePrincipal> getParentPrincipals()
	{
		if (null==this.parentPrincipals)
		{
			this.buildProperty("parentPrincipals");
		}
		return this.parentPrincipals;
	}

	public ElementEdges<Identity> getIdentities()
	{
		if (null==this.identities)
		{
			this.buildProperty("identities");
		}
		return this.identities;
	}

	public Collection<Person> getPeople()
	{
		// This will prevent infinite recursion.
		Collection<BasePrincipal> checked=new ArrayList<>();
		return this.getPeople(checked);
	}

	@SuppressWarnings("ClassReferencesSubclass")
	public boolean isPerson()
	{
		return ClassX.isKindOf(this.getClass(), Person.class);
	}

	public synchronized boolean isPrivileged(){
		if(null == this.privileged){
			this.privileged = false;
			Collection<String> impersonators = ScopedConfiguration.getInstance().getPrivileged();
			for(String impersonator: impersonators){
				if(Group.isInGroup(this, impersonator)){
					this.privileged = true;
					break;
				}
			}
		}
		return this.privileged;
	}
}
