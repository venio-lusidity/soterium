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

package com.lusidity.domains.acs.security.authorization;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.domains.system.primitives.Text;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("EqualsAndHashcode")
@AtSchemaClass(name="Group", discoverable=false, description="A title which defines an authority level.", writable=true)
public class Group extends BasePrincipal
{
	@SuppressWarnings({
		"NonFinalUtilityClass",
		"UtilityClassWithoutPrivateConstructor"
	})
	public static class Queries
	{
		// Methods
		public static Group getGroup(Double groupId)
		{
			Group result=null;

			if (null!=groupId)
			{
				Collection<Group> groups=VertexFactory.getInstance().getAllByPropertyIgnoreCase(Group.class, "identifier", groupId, 0, 0);
				result=CollectionX.getFirst(groups);
			}

			return result;
		}
	}

	// Fields
	public static final int INIT_ORDINAL=1110;
	private static final String KEY_SYSTEM_ROOT="Root Security Group";
	private static Group INSTANCE=null;
	private KeyData<Double> identifier=null;
	@AtSchemaProperty(name="Permissions", expectedType=Permission.class, description="Permissions for this group.")
	private ElementEdges<Permission> permissions=null;

	// Constructors
	public Group()
	{
		super();
	}

	public Group(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@SuppressWarnings("NonThreadSafeLazyInitialization")
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		if (Objects.equals(this.getClass(), Group.class))
		{
			Group.INSTANCE=VertexFactory.getInstance().getByTitle(Group.class, Group.KEY_SYSTEM_ROOT);
			if (null==Group.INSTANCE)
			{
				Group.INSTANCE=new Group();
				Group.INSTANCE.fetchTitle().setValue(Group.KEY_SYSTEM_ROOT);
				Group.INSTANCE.fetchTypes().add(new Text("global"));
				Group.INSTANCE.save();
				if ((null==Group.INSTANCE) || StringX.isBlank(Group.INSTANCE.fetchId().getValue()))
				{
					throw new ApplicationException("The Global GroupTypes could not be created");
				}
				Environment.getInstance().getReservedNames().put(Group.KEY_SYSTEM_ROOT, Group.class);
			}
			JsonData data=Environment.getInstance().getConfig().getGroups();
			if ((null!=Group.getRootGroup()) && (null!=data) && data.isJSONArray())
			{
				for (Object o : data)
				{
					if (o instanceof JSONObject)
					{
						JsonData item=new JsonData(o);
						this.initialize(item);
					}
				}
			}
		}
	}

	public static Group getRootGroup()
	{
		return Group.INSTANCE;
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod"
	})
	private void initialize(JsonData item)
		throws Exception
	{
		Double id=item.getDouble("id");
		String strTitle=item.getString("title");
		String description=item.getString("description");
		if (!StringX.isBlank(strTitle) && (null!=id))
		{
			Group group=Group.getOrCreate(id, strTitle, description);
			if ((null!=group) && group.hasId())
			{
				boolean exists=Group.getRootGroup().getPrincipals().contains(group);
				if (!exists)
				{
					Group.getRootGroup().getPrincipals().add(group);
				}
				JsonData pt=item.getFromPath("permissions");
				if ((null!=pt) && pt.isJSONArray())
				{
					for (Object obj : pt)
					{
						if (obj instanceof Double)
						{
							Double value=(Double) obj;
							Permission permission=Permissions.getInstance().getPermission(value);
							if (null!=permission)
							{
								group.getPermissions().add(permission);
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("OverlyComplexMethod")
	public static Group getOrCreate(Double id, String title, String description)
		throws Exception
	{
		Group result=null;
		if (null!=id)
		{
			Collection<Group> groups=VertexFactory.getInstance().getAllByPropertyIgnoreCase(Group.class, "identifier", title, 0, 10);
			if ((null!=groups) && !groups.isEmpty())
			{
				result=CollectionX.getFirst(groups);
			}
		}
		if ((null==result) && !StringX.isBlank(title))
		{
			result=VertexFactory.getInstance().getByTitle(Group.class, title);
		}
		if ((null==result) && !StringX.isBlank(title))
		{
			result=new Group();
			result.fetchTitle().setValue(title);
			result.fetchIdentifier().setValue(id);
			result.save();
		}
		if ((null!=result) && !StringX.isBlank(description) && !result.getDescriptions().isEmpty())
		{
			result.getDescriptions().add(new RawString(description));
		}
		return result;
	}

	public ElementEdges<Permission> getPermissions()
	{
		if (null==this.permissions)
		{
			this.buildProperty("permissions");
		}
		return this.permissions;
	}

	public KeyData<Double> fetchIdentifier()
	{
		if (null==this.identifier)
		{
			this.identifier=new KeyData<>(this, "identifier", Double.class, false, null);
		}
		return this.identifier;
	}

	@Override
	public int getInitializeOrdinal()
	{
		return Group.INIT_ORDINAL;
	}

	@Override
	public boolean enforcePolicy()
	{
		return !this.equals(Group.getRootGroup());
	}

	@Override
	public boolean equals(Object o)
	{
		boolean result=super.equals(o);
		if (!result)
		{
			//noinspection ChainOfInstanceofChecks
			if (o instanceof Group)
			{
				Double id=((Group) o).fetchIdentifier().getValue();
				if (null!=id)
				{
					result=Objects.equals(id, this.fetchIdentifier().getValue());
				}
			}
			else if (o instanceof String)
			{
				result=Objects.equals(o, this.fetchTitle().getValue());
			}
		}
		return result;
	}

	// Methods
	public static boolean isInGroupOrChild(Person person, Group group)
	{
		boolean authorized=false;
		if (null!=group)
		{
			authorized=Group.isInGroup(person, group);
		}
		if (!authorized && !group.getPrincipals().isEmpty())
		{
			authorized=Group.isInChildGroup(group, person);
		}

		return authorized;
	}

	public static boolean isInChildGroup(Group group, Person person)
	{
		boolean authorized=false;
		for (BasePrincipal bp : group.getPrincipals())
		{
			if (bp instanceof Group)
			{
				Group child=(Group) bp;
				authorized=Group.isInGroup(person, child);
				if (authorized)
				{
					break;
				}
				if (!authorized && !child.getPrincipals().isEmpty())
				{
					authorized=Group.isInChildGroup(child, person);
					if (authorized)
					{
						break;
					}
				}
			}
		}
		return authorized;
	}

	@SuppressWarnings("unused")
	public static boolean isInGroup(BasePrincipal principal, String title)
	{
		boolean result=false;
		Group r=VertexFactory.getInstance().getByTitle(Group.class, title);
		if (null!=r)
		{
			result=Group.isInGroup(principal, r);
		}
		return result;
	}

	@SuppressWarnings("unused")
	public static boolean isInGroup(BasePrincipal principal, Double groupId)
	{
		boolean result=false;
		Group r=Group.Queries.getGroup(groupId);
		if (null!=r)
		{
			result=Group.isInGroup(principal, r);
		}
		return result;
	}

	@SuppressWarnings("ClassReferencesSubclass")
	public static boolean isInGroup(BasePrincipal principal, Group group)
	{
		boolean result=false;
		String key=group.getPrincipals().getKey();
		Class<? extends Edge> edgeType=group.getPrincipals().getEdgeType();
		Edge edge=group.getEdgeHelper().getEdge(edgeType, principal, key, Common.Direction.OUT);
		if (null!=edge)
		{
			result=!edge.fetchDeprecated().getValue();
		}
		if (!result && !ClassX.isKindOf(principal, PersonnelPosition.class))
		{
			for (BasePrincipal bp : group.getPrincipals())
			{
				if (ClassX.isKindOf(bp, PersonnelPosition.class))
				{
					PersonnelPosition position=(PersonnelPosition) bp;
					result=Group.isInGroup(principal, position);
					if (result)
					{
						break;
					}
				}
			}
		}
		return result;
	}

	public static boolean remove(BasePrincipal principal, String title)
	{
		boolean result=false;
		Group group=Group.getGroup(title);
		if (null!=group)
		{
			group.setCredentials(principal.getCredentials());
			result=Group.remove(principal, group);
		}

		return result;
	}

	public static Group getGroup(String title)
	{
		Group result=VertexFactory.getInstance().getByTitle(Group.class, title);
		if (null==result)
		{
			Set<Class<? extends Group>> subtypes=Environment.getInstance().getReflections().getSubTypesOf(Group.class);
			for (Class<? extends Group> sub : subtypes)
			{
				result=VertexFactory.getInstance().getByTitle(sub, title);
				if (null!=result)
				{
					break;
				}
			}
		}
		return result;
	}

	public static boolean remove(BasePrincipal principal, Group group)
	{
		boolean result=false;

		if (null!=group)
		{
			result=group.getPrincipals().remove(principal);
		}

		return result;
	}

	public static boolean remove(BasePrincipal principal, Double groupId)
	{
		boolean result=false;

		Group group=Group.Queries.getGroup(groupId);
		if (null!=group)
		{
			result=Group.remove(principal, group);
		}

		return result;
	}

	@SuppressWarnings("unused")
	public static Group getGroup(Double groupId)
	{
		return Group.Queries.getGroup(groupId);
	}
}
