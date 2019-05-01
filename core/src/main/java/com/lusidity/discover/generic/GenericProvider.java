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

package com.lusidity.discover.generic;

import com.lusidity.annotations.AtIndexedField;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.discover.DiscoveryItems;
import com.lusidity.discover.DiscoveryProvider;
import com.lusidity.discover.interfaces.SuggestItem;
import com.lusidity.domains.BaseDomain;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.java.ClassX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class GenericProvider implements DiscoveryProvider
{
	private static final Collection<Class<? extends DataVertex>> PROVIDERS=new ArrayList<>();

	private double relevancy=0.5;
	private UserCredentials credentials = null;

	/**
	 * Add a domain that has a custom query or specific format when returning results.
	 * @param domain A searchable domain that has a custom query and/or format for results.
	 */
	public static void add(Class<? extends BaseDomain> domain){
		GenericProvider.PROVIDERS.add(domain);
	}

// Constructors
	public GenericProvider(double relevancy)
	{
		super();
		this.relevancy=relevancy;
	}

	public GenericProvider()
	{
		super();
	}

// Overrides
	@Override
	public DiscoveryItems discover(String phrase, boolean suggest, int start, int limit, UserCredentials credentials)
	{
		DiscoveryItems results = new DiscoveryItems(this, 0);

		if (this.isValid(phrase))
		{
			this.credentials = credentials;
			for (Class<? extends DataVertex> cls : GenericProvider.PROVIDERS)
			{
				Collection<AtIndexedField> annotations=PropertyHelper.getClassesAnnotatedWithProperty(cls);
				for (AtIndexedField field : annotations)
				{
					if (!Objects.equals(field.type(), String.class))
					{
						continue;
					}

					Collection<? extends DataVertex> vertices=VertexFactory.getInstance().startsWith(cls, field.key(), phrase, start, limit);

					for (DataVertex vertex : vertices)
					{
						if (ClassX.isKindOf(vertex.getClass(), BaseDomain.class))
						{
							Object value = vertex.getVertexData().getObjectFromPath(field.key());
							GenericItem item=this.getItem(phrase, (BaseDomain) vertex, field.key(), value, 0);
							results.add(item);
						}
					}
				}
			}
		}

		return results;
	}

	@Override
	public DiscoveryItems process(String phrase, ApolloVertex vertex, String key, Object value, int start, int limit, boolean suggest)
	{
		return new DiscoveryItems(this, 0);
	}

	@Override
	public boolean handles(DataVertex vertex)
	{
		return false;
	}

	@Override
	public Collection<SuggestItem> suggest(String phrase, int start, int limit, UserCredentials credentials)
	{
		return new ArrayList<>();
	}

	@Override
	public boolean isValid(String phrase)
	{
		return true;
	}

	@Override
	public int getHits(BaseQueryBuilder queryBuilder)
	{
		return 0;
	}

	@Override
	public boolean providerHandles(Class<? extends ApolloVertex> cls)
	{
		return false;
	}

	@Override
	public Class getVertexType()
	{
		return DataVertex.class;
	}

	public GenericItem getItem(String phrase, BaseDomain domain, String key, Object value, int hits)
	{
		GenericItem item=new GenericItem(phrase, domain, this.credentials, key, value, hits);
		String description=domain.getDescription();
		item.build(domain.fetchTitle().getValue(), description, domain.getUri(), null, this.relevancy, domain.getClass(), item.getHits());
		return item;
	}
}
