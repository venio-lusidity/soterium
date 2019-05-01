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

package com.lusidity.domains.organization;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.collections.TermEdges;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.interfaces.SuggestItem;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.location.Location;
import com.lusidity.domains.object.edge.OrganizationEdge;
import com.lusidity.domains.object.edge.ScopedEdge;
import com.lusidity.domains.object.edge.TermEdge;
import com.lusidity.domains.organization.discovery.OrganizationItem;
import com.lusidity.domains.system.primitives.Text;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.security.cbac.policies.PolicyOrganization;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.cbac.ISecurityPolicy;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;

import java.util.Collection;

@SuppressWarnings({
	"UnusedDeclaration",
	"OverlyCoupledClass"
})
@AtSchemaClass(name="Organization", discoverable=true, description="An Organization or department.")
public class Organization extends BaseDomain
{
	@SuppressWarnings("FinalClass")
	public static final class Queries
	{

		private Queries()
		{
		}

		// Methods
		public static <T extends Organization> Collection<T> getStartsWith(String phrase)
		{
			return Organization.Queries.getStartsWith(phrase, 0, 0);
		}

		@SuppressWarnings("SameParameterValue")
		public static <T extends Organization> Collection<T> getStartsWith(String phrase, int start, int limit)
		{
			return VertexFactory.getInstance().startsWith(Organization.class, "title", phrase, start, limit);
		}

		public static <T extends Organization> T getByTitle(String title)
		{
			return Organization.Queries.getByTitle(Organization.class, title);
		}

		public static <T extends Organization> T getByTitle(Class<? extends DataVertex> store, String phrase)
		{
			T result=VertexFactory.getInstance().getByPropertyIgnoreCase(store, "title", phrase);
			if (null==result)
			{
				result=VertexFactory.getInstance().getByPropertyIgnoreCase(store, "abbreviation", phrase);
			}
			return result;
		}
	}

	// Fields
	public static final int INIT_ORDINAL=1200;
	public static final String KEY_SYSTEM_ROOT_ORG="Root Organizations";
	public static final String KEY_SYSTEM_TAG_ISSUES="Organization Tag Issues";
	public static final String KEY_SYSTEM_TAG_MANAGED_ISSUES="Managed By Tag Issues";
	public static final String KEY_SYSTEM_TAG_OWNED_ISSUES="Owned By Tag Issues";
	private static Organization root=null;
	private static Organization tagIssues=null;
	private static Organization managedTagIssues=null;
	private static Organization ownedTagIssues=null;
	private KeyData<String> abbreviation=null;
	private KeyData<String> prefixTree=null;
	@AtSchemaProperty(name="Contact Info", expectedType=BaseContactDetail.class,
		description="Contact information for this organization.")
	private ElementEdges<BaseContactDetail> contactDetails=null;
	@AtSchemaProperty(name="Organization", expectedType=Organization.class, edgeType=OrganizationEdge.class,
		description="Child organizations.")
	private ElementEdges<Organization> organizations=null;
	@AtSchemaProperty(name="Organization", expectedType=Organization.class, edgeType=OrganizationEdge.class,
		fieldName="organizations", description="Parent organizations.", direction=Common.Direction.IN)
	private ElementEdges<Organization> parents=null;
	@AtSchemaProperty(name="Personnel Positions", edgeType=TermEdge.class, expectedType=PersonnelPosition.class,
		description="A position within this organization.")
	private TermEdges<PersonnelPosition> positions=null;
	@AtSchemaProperty(name="Scoped Positions", edgeType=ScopedEdge.class, expectedType=PersonnelPosition.class,
		description="A position with an area of responsibility within this organization.")
	private TermEdges<PersonnelPosition> scopedPositions=null;
	/*
		This value is specific to the COAMS owned by the government
	 */
	private KeyData<Integer> nameId=null;
	/*
		This value is specific to the COAMS owned by the government
	 */
	private KeyData<Integer> superiorNameId=null;

	// Constructors
	public Organization()
	{
		super();
	}

	public Organization(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		Organization.root=Organization.Queries.getByTitle(Organization.class, Organization.KEY_SYSTEM_ROOT_ORG);
		if (null==Organization.root)
		{
			//noinspection NonThreadSafeLazyInitialization
			Organization.root=new Organization();
			Organization.root.fetchTitle().setValue(Organization.KEY_SYSTEM_ROOT_ORG);
			Organization.root.fetchTypes().add(new Text("global"));

			if (!Organization.root.save())
			{
				throw new ApplicationException("The Global Organization could not be created");
			}
			Environment.getInstance().getReservedNames().put(Organization.KEY_SYSTEM_ROOT_ORG, Organization.class);
		}

		Organization.managedTagIssues=Organization.Queries.getByTitle(Organization.class, Organization.KEY_SYSTEM_TAG_MANAGED_ISSUES);
		if (null==Organization.managedTagIssues)
		{
			//noinspection NonThreadSafeLazyInitialization
			Organization.managedTagIssues=new Organization();
			Organization.managedTagIssues.fetchTitle().setValue(Organization.KEY_SYSTEM_TAG_MANAGED_ISSUES);
			Organization.managedTagIssues.fetchTypes().add(new Text("managed issues"));
			if (!Organization.managedTagIssues.save())
			{
				throw new ApplicationException("The Managed By Organization tag issues could not be created");
			}
		}

		if (!Organization.root.getOrganizations().contains(Organization.managedTagIssues))
		{
			Organization.root.getOrganizations().add(Organization.managedTagIssues);
		}

		Organization.ownedTagIssues=Organization.Queries.getByTitle(Organization.class, Organization.KEY_SYSTEM_TAG_OWNED_ISSUES);
		if (null==Organization.ownedTagIssues)
		{
			//noinspection NonThreadSafeLazyInitialization
			Organization.ownedTagIssues=new Organization();
			Organization.ownedTagIssues.fetchTitle().setValue(Organization.KEY_SYSTEM_TAG_OWNED_ISSUES);
			Organization.ownedTagIssues.fetchTypes().add(new Text("owned issues"));
			if (!Organization.ownedTagIssues.save())
			{
				throw new ApplicationException("The Organization tag issues could not be created");
			}
			Organization.root.getOrganizations().add(Organization.ownedTagIssues);
		}

		if (!Organization.root.getOrganizations().contains(Organization.ownedTagIssues))
		{
			Organization.root.getOrganizations().add(Organization.ownedTagIssues);
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return Organization.INIT_ORDINAL;
	}

	public ElementEdges<Organization> getOrganizations()
	{
		if (null==this.organizations)
		{
			this.buildProperty("organizations");
		}
		return this.organizations;
	}

	@Override
	public boolean enforcePolicy()
	{
		return !this.equals(Organization.getRoot());
	}

	@SuppressWarnings("NonThreadSafeLazyInitialization")
	public static Organization getRoot()
	{
		if (null==Organization.root)
		{
			Organization.root=Organization.Queries.getByTitle(Location.class, Organization.KEY_SYSTEM_ROOT_ORG);
		}
		return Organization.root;
	}

	@Override
	public PolicyDecisionPoint getSecurityPolicy(BasePrincipal principal, ISecurityPolicy... policies)
	{
		//noinspection ClassReferencesSubclass
		boolean isOrg=ClassX.isKindOf(this, Organization.class);
		int size=((null!=policies) ? policies.length : 0)+((isOrg) ? 1 : 0);
		ISecurityPolicy[] args=new ISecurityPolicy[size];
		boolean exists=false;
		if (null!=policies)
		{
			for (ISecurityPolicy policy : policies)
			{
				if (policy instanceof PolicyOrganization)
				{
					exists=true;
					break;
				}
			}
		}
		if (isOrg && !exists)
		{
			//PolicyBaseInfrastructure policy=new PolicyBaseInfrastructure(principal, this);
			PolicyOrganization policy=new PolicyOrganization(principal, this, PolicyOrganization.ScanDirection.none);
			args[0]=policy;
		}

		if (null!=policies)
		{
			for (ISecurityPolicy policy : policies)
			{
				args[args.length-1]=policy;
			}
		}

		return super.getSecurityPolicy(principal, args);
	}

	@Override
	public boolean delete()
	{
		boolean result=false;
		boolean process=(!this.equals(Organization.getRoot())
		                 && !this.equals(Organization.getManagedTagIssues())
		                 && !this.equals(Organization.getOwnedTagIssues()));
		if (process)
		{
			result=super.delete();
		}
		return result;
	}

	@SuppressWarnings("NonThreadSafeLazyInitialization")
	public static Organization getManagedTagIssues()
	{
		if (null==Organization.managedTagIssues)
		{
			Organization.managedTagIssues=Organization.Queries.getByTitle(Organization.class, Organization.KEY_SYSTEM_TAG_MANAGED_ISSUES);
		}
		return Organization.managedTagIssues;
	}

	@SuppressWarnings("NonThreadSafeLazyInitialization")
	public static Organization getOwnedTagIssues()
	{
		if (null==Organization.ownedTagIssues)
		{
			Organization.ownedTagIssues=Organization.Queries.getByTitle(Organization.class, Organization.KEY_SYSTEM_TAG_OWNED_ISSUES);
		}
		return Organization.ownedTagIssues;
	}

	@Override
	public boolean deleteAllEdges()
	{
		return super.deleteAllEdges();
	}

	@Override
	public DiscoveryItem getDiscoveryItem(String phrase, UserCredentials userCredentials, String key, Object value, boolean suggest)
	{
		DiscoveryItem result;
		if (suggest)
		{
			result=SuggestItem.getSuggestion(this, userCredentials, phrase);
		}
		else
		{
			result=new OrganizationItem(phrase, this, userCredentials, key, value, 0);
		}
		return result;
	}

	// Methods
	@SuppressWarnings("NonThreadSafeLazyInitialization")
	public static Organization getTagIssues()
	{
		if (null==Organization.tagIssues)
		{
			Organization.tagIssues=Organization.Queries.getByTitle(Organization.class, Organization.KEY_SYSTEM_TAG_ISSUES);
		}
		return Organization.tagIssues;
	}

	public void addOrganizations(Organization organization)
	{
		if (null!=organization)
		{
			boolean added=this.organizations.add(organization);
			if (!added)
			{
				Environment.getInstance().getReportHandler().warning("Could not add organization %s.", organization.fetchTitle().getValue());
			}
		}
	}

	public KeyData<String> fetchAbbreviation()
	{
		if (null==this.abbreviation)
		{
			this.abbreviation=new KeyData<>(this, "abbreviation", String.class, false, null);
		}
		return this.abbreviation;
	}

	public KeyData<String> fetchPrefixTree()
	{
		if (null==this.prefixTree)
		{
			this.prefixTree=new KeyData<>(this, "prefixTree", String.class, false, null);
		}
		return this.prefixTree;
	}

	/**
	 * This value is specific to COAMS a government owned organizational system.
	 *
	 * @return A COAMS Id.
	 */
	public KeyData<Integer> fetchNameId()
	{
		if (null==this.nameId)
		{
			this.nameId=new KeyData<>(this, "nameId", Integer.class, true, null);
		}
		return this.nameId;
	}

	/**
	 * This value is specific to COAMS a government owned organizational system and relates to a parent organization.
	 *
	 * @return A COAMS Id.
	 */
	public KeyData<Integer> fetchSuperiorNameId()
	{
		if (null==this.superiorNameId)
		{
			this.superiorNameId=new KeyData<>(this, "superiorNameId", Integer.class, true, null);
		}
		return this.superiorNameId;
	}

	// Getters and setters
	public TermEdges<PersonnelPosition> getPositions()
	{
		if (null==this.positions)
		{
			this.buildProperty("positions");
		}
		return this.positions;
	}

	public TermEdges<PersonnelPosition> getScopedPositions()
	{
		if (null==this.scopedPositions)
		{
			this.buildProperty("scopedPositions");
		}
		return this.scopedPositions;
	}

	public ElementEdges<BaseContactDetail> getContactDetails()
	{
		if (null==this.contactDetails)
		{
			this.buildProperty("contactDetails");
		}
		return this.contactDetails;
	}

	public ElementEdges<Organization> getParents()
	{
		if (null==this.parents)
		{
			this.buildProperty("parents");
		}
		return this.parents;
	}
}
