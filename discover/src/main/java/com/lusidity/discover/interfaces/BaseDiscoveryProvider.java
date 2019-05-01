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

package com.lusidity.discover.interfaces;

import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.discover.DiscoveryItems;
import com.lusidity.discover.DiscoveryProvider;
import com.lusidity.framework.java.ClassX;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;

public abstract class BaseDiscoveryProvider implements DiscoveryProvider
{
	public static class Query
	{
		// Methods
		public static <T extends DataVertex> Collection<T> startsWith(DiscoveryItems discoveryItems, Class<? extends DataVertex> store, String property, String phrase, int start, int limit)
		{
			return BaseDiscoveryProvider.Query.startsWith(discoveryItems, store, property, phrase, "title", start, limit);
		}

		public static <T extends DataVertex> Collection<T> startsWith(DiscoveryItems discoveryItems, Class<? extends DataVertex> store, String property, String phrase, String sortProperty, int
			start, int limit)
		{
			Collection<T> results=new ArrayList<>();
			BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, BaseQueryBuilder.API._search, start, limit);
			qb.filter(BaseQueryBuilder.Operators.must, property, BaseQueryBuilder.StringTypes.starts_with, phrase);
			qb.sort(String.format("%s.raw", sortProperty), BaseQueryBuilder.Sort.asc);
			QueryResults queryResults=qb.execute();
			discoveryItems.addHits(queryResults.getHits());
			if (!queryResults.isEmpty())
			{
				for (IQueryResult queryResult : queryResults)
				{
					DataVertex dataVertex=queryResult.getVertex();
					if (null!=dataVertex)
					{
						//noinspection unchecked
						results.add((T) dataVertex);
					}
				}
			}
			return results;
		}
	}
	private double relevancy = 1.0;

	// Constructors
	public BaseDiscoveryProvider(double relevancy)
	{
		super();
		this.relevancy = relevancy;
	}

	public BaseDiscoveryProvider()
	{
		super();
	}

	// Overrides
	@Override
	public Collection<SuggestItem> suggest(String phrase, int start, int limit, UserCredentials credentials)
	{
		return new ArrayList<>();
	}

	@Override
	public int getHits(BaseQueryBuilder queryBuilder)
	{
		queryBuilder.setApi(BaseQueryBuilder.API._count);
		int result = queryBuilder.execute().getCount();
		queryBuilder.setApi(BaseQueryBuilder.API._search);
		return result;
	}

	@Override
	public boolean providerHandles(Class<? extends ApolloVertex> cls)
	{
		return ClassX.isKindOf(cls, this.getVertexType());
	}

	// Getters and setters
	public double getRelevancy()
	{
		return this.relevancy;
	}
}
