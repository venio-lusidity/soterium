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

package com.lusidity.domains;


import com.lusidity.Environment;
import com.lusidity.Initializer;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.object.edge.AuthorizedEdge;
import com.lusidity.domains.system.primitives.Text;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ClassReferencesSubclass")
@AtSchemaClass(name="Base Domain", discoverable=false)
public abstract class BaseDomain extends ApolloVertex implements Initializer
{

	private static Collection<String> DOMAIN_NAMES=new ArrayList<>();

	private KeyDataCollection<UriValue> identifiers=null;

	private KeyDataCollection<UriValue> volatileIdentifiers=null;

	private KeyDataCollection<Text> types=null;

	@AtSchemaProperty(name = "Authorized Principals", expectedType = BasePrincipal.class, edgeType = AuthorizedEdge.class, discoverable = false)
	private ElementEdges<BasePrincipal> authorized = null;

	// Constructors
	public BaseDomain()
	{
		super();
	}

	public BaseDomain(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public boolean save(Class<? extends DataVertex> store)
		throws Exception
	{
		return super.save(store);
	}

	@Override
	public boolean isWritable()
	{
		boolean result=super.isWritable();
		if (result)
		{
			result=(null==Environment.getInstance()) || !Environment.getInstance().isReserved(this.fetchTitle().getValue(), this.getClass());
		}
		return result;
	}

	@Override
	public void initialize()
		throws Exception
	{
		try
		{
			if (Environment.getInstance().getConfig().isInitializeDomains())
			{
				// Create new Indices on startup.
				int count=Environment.getInstance().getDataStore().count(this.getClass());
				if (count==0)
				{
					Constructor constructor=this.getClass().getConstructor();
					BaseDomain test=(BaseDomain) constructor.newInstance();

					String title=String.format("init_test_%d", DateTime.now().getMillis());

					test.fetchTitle().setValue(title);
					boolean saved=test.save();

					if (!saved)
					{
						throw new ApplicationException("%s index is not properly configured.", this.getClass());
					}
					else
					{
						test.delete();
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return Integer.MAX_VALUE;
	}

	// Methods
	@SuppressWarnings("unused")
	public static Collection<String> getDomainNames()
	{
		if (BaseDomain.DOMAIN_NAMES.isEmpty() && !Environment.getInstance().getApolloVertexTypes().isEmpty())
		{
			for (Map.Entry<String, Class<? extends ApolloVertex>> entry : Environment.getInstance().getApolloVertexTypes().entrySet())
			{
				String id=entry.getKey();
				if (!StringX.isBlank(id))
				{
					id=StringX.stripStart(id, "/");
					String name=StringX.getFirst(id, "/");
					if (!StringX.isBlank(name))
					{
						name=String.format("/%s", name);
						if (!BaseDomain.DOMAIN_NAMES.contains(name))
						{
							BaseDomain.DOMAIN_NAMES.add(name);
						}
					}
				}
			}
		}
		return BaseDomain.DOMAIN_NAMES;
	}

	public static Class<? extends DataVertex> getDomainType(String key)
	{
		return Environment.getInstance().getApolloVertexType(key);
	}

	@SuppressWarnings("unused")
	private static Class<? extends DataVertex> getBaseDomainType(Class<? extends DataVertex> type)
	{
		Class<? extends DataVertex> result=type;
		if (((!Objects.equals(result.getSuperclass(), ApolloVertex.class)) && (!Objects.equals(result.getSuperclass(), DataVertex.class))))
		{
			//noinspection unchecked
			result=BaseDomain.getBaseDomainType((Class<? extends DataVertex>) result.getSuperclass());
		}

		return result;
	}

	public KeyDataCollection<UriValue> fetchIdentifiers()
	{
		if (null==this.identifiers)
		{
			this.identifiers = new KeyDataCollection<>(this, "identifiers", UriValue.class, true, false, true, null);
		}
		return this.identifiers;
	}

	public KeyDataCollection<UriValue> fetchVolatileIdentifiers()
	{
		if (null==this.volatileIdentifiers)
		{
			this.volatileIdentifiers = new KeyDataCollection<>(this, "volatileIdentifiers", UriValue.class, true, false, true, null);
		}
		return this.volatileIdentifiers;
	}

	public KeyDataCollection<Text> fetchTypes()
	{
		if (null==this.types)
		{
			this.types = new KeyDataCollection<>(this, "types", Text.class, false, false, false, null);
		}
		return this.types;
	}

	// Getters and setters
	public ElementEdges<BasePrincipal> getAuthorized()
	{
		if(null==this.authorized){
			this.buildProperty("authorized");
		}
		return this.authorized;
	}
}
