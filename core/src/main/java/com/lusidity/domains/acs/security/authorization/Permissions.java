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
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.domains.system.primitives.Text;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;

import java.util.Objects;

@AtSchemaClass(name="Permissions", discoverable=false, description="A collection of permissions.")
public class Permissions extends BaseDomain
{
// Fields
	public static final int INIT_ORDINAL=1100;
	private static final String KEY_SYSTEM_ROOT="Permissions";
	private static Permissions INSTANCE=null;
	@AtSchemaProperty(name="Permission", expectedType=Permission.class,
		description="A collection of permissions.")
	private ElementEdges<Permission> permissions=null;

// Constructors
	public Permissions()
	{
		super();
	}

	public Permissions(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Overrides
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"OverlyNestedMethod"
	})
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		Permissions.INSTANCE=VertexFactory.getInstance().getByTitle(Permissions.class, Permissions.KEY_SYSTEM_ROOT);
		if (null==Permissions.INSTANCE)
		{
			//noinspection NonThreadSafeLazyInitialization
			Permissions.INSTANCE=new Permissions();
			Permissions.INSTANCE.fetchTitle().setValue(Permissions.KEY_SYSTEM_ROOT);
			Permissions.INSTANCE.fetchTypes().add(new Text("global"));
			Permissions.INSTANCE.save();
			if ((null==Permissions.INSTANCE) || StringX.isBlank(Permissions.INSTANCE.fetchId().getValue()))
			{
				throw new ApplicationException("The Global Permission could not be created");
			}
			Environment.getInstance().getReservedNames().put(Permissions.KEY_SYSTEM_ROOT, Permissions.class);
		}
		JsonData objects=Environment.getInstance().getConfig().getPermissions();
		if ((null!=Permissions.INSTANCE) && (null!=objects) && objects.isJSONArray())
		{
			for (Object o : objects)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=new JsonData(o);
					Double id=item.getDouble("id");
					String title=item.getString("title");
					String description=item.getString("description");
					if (!StringX.isBlank(title) && (null!=id))
					{
						Permission permission=Permission.create(id, title);
						boolean exists=false;
						for (Permission p : Permissions.INSTANCE.getPermissions())
						{
							exists=p.equals(permission);
							if (exists)
							{
								break;
							}
						}
						if (!exists)
						{
							permission.save();
							boolean added=Permissions.INSTANCE.getPermissions().add(permission);
							if (!added)
							{
								Environment.getInstance().getReportHandler().warning("Failed to add permission %s.", permission.fetchTitle().getValue());
							}
							if (added && !StringX.isBlank(description))
							{
								added=permission.getDescriptions().add(new RawString(description));
								if (!added)
								{
									Environment.getInstance().getReportHandler().warning("Failed to add description to permission %s.", permission.fetchTitle().getValue());
								}
							}
						}
					}
				}
			}
		}
	}

	public ElementEdges<Permission> getPermissions()
	{
		if (null==this.permissions)
		{
			this.buildProperty("permissions");
		}
		return this.permissions;
	}

	@Override
	public int getInitializeOrdinal()
	{
		return Permissions.INIT_ORDINAL;
	}

// Methods
	public static Permissions getInstance()
	{
		return Permissions.INSTANCE;
	}

	public Permission getPermission(Double permissionId)
	{
		Permission result=null;
		for (Permission permission : this.getPermissions())
		{
			if (Objects.equals(permission.fetchIdentifier().getValue(), permissionId))
			{
				result=permission;
				break;
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	public Permission getPermission(String title)
	{
		Permission result=null;
		for (Permission permission : this.getPermissions())
		{
			if (StringX.equalsIgnoreCase(permission.fetchTitle().getValue(), title))
			{
				result=permission;
				break;
			}
		}
		return result;
	}
}
