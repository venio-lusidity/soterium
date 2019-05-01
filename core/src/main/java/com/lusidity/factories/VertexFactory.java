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

package com.lusidity.factories;


import com.lusidity.Environment;
import com.lusidity.collections.ElementAttributes;
import com.lusidity.collections.PropertyAttributes;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.IndexHelper;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;

public class VertexFactory implements IElementFactory
{

	private static VertexFactory instance=null;

// Overrides
	@Override
	public <T extends DataVertex> T get(Class<? extends DataVertex> store, URI uri)
	{
		T result=null;
		if (null!=uri)
		{
			String dataStoreId=StringX.getLast(uri.toString(), "/");
			result=this.get(store, dataStoreId);
		}
		return result;
	}

	/**
	 * Get a vertex from the data staore.
	 *
	 * @param dataStoreId The id of the vertex, expected format is /domains/the_class/the_id.
	 * @return A Vertex.
	 */
	@Override
	public <T extends DataVertex> T get(String dataStoreId)
		throws Exception
	{
		if (StringX.isBlank(dataStoreId))
		{
			throw new InvalidParameterException("The dataStoreId cannot be null or empty.");
		}
		String id=StringX.replace(dataStoreId, "/domains/", "");
		String domain=StringX.getFirst(id, "/");
		if (StringX.isBlank(domain))
		{
			throw new ApplicationException("The dataStoreId is not in the expected format.");
		}

		Class<? extends DataVertex> store=BaseDomain.getDomainType(domain);

		if (null==store)
		{
			throw new InvalidParameterException(String.format("The domain, %s, does not exist.", domain));
		}

		String finalId=Environment.getInstance().getDataStore().formatDataStoreId(dataStoreId);
		if (StringX.isBlank(finalId))
		{
			throw new ApplicationException("The dataStoreId is not in the expected format.");
		}

		T result;
		if(ClassX.isKindOf(store, Edge.class)){
			BaseQueryBuilder qb = Environment.getInstance().getIndexStore().getQueryBuilder(store, null, 0, 1);
			qb.filter(BaseQueryBuilder.Operators.must, IDataStore.DATA_STORE_ID, BaseQueryBuilder.StringTypes.raw, finalId);
			result = qb.execute().getFirst();
		}
		else{
			result = VertexFactory.getInstance().get(store, finalId);
		}

		return result;
	}

	public static synchronized VertexFactory getInstance()
	{
		if (null==VertexFactory.instance)
		{
			VertexFactory.instance=new VertexFactory();
		}
		return VertexFactory.instance;
	}

	/**
	 * This will not work for edges as a partition is required to retrieve from the proper index/type  (store/partition)
	 * @param store A data Vertex
	 * @param dataStoreId The id of the Element as represented in the data store.  @return An Element of type T.
	 * @param <T> A DataVertex type.
	 * @return A DataVertex or null
	 */
	@Override
	public <T extends DataVertex> T get(Class<? extends DataVertex> store, Object dataStoreId)
	{
		T result=null;
		if (null!=dataStoreId)
		{
			IDataStore ds = Environment.getInstance().getDataStore();
			String id=Environment.getInstance().getDataStore().formatDataStoreId(dataStoreId);
			result = ds.getObjectById(store, store, id);
		}
		return result;
	}

	@Override
	public boolean delete(DataVertex dataVertex, Class<? extends DataVertex> store)
	{
		IDataStore dataStore=Environment.getInstance().getDataStore();
		return dataStore.delete(store, dataVertex, 0);
	}

	@Override
	public <T extends DataVertex> ElementAttributes<T> attributes(Class<? extends ElementAttributes> cls, PropertyAttributes config)
	{
		return config.getVertex().attributes(cls, config);
	}

	@Override
	public DataVertex merge(DataVertex fromVertex, DataVertex toVertex)
		throws ApplicationException
	{
		if ((null==fromVertex) || (null==toVertex))
		{
			throw new ApplicationException("Neither the fromEntity or toEntity can be null.");
		}

		Environment.getInstance().getReportHandler().notImplemented();

		return null;
	}

	public <T extends DataVertex> T getById(String id)
		throws ApplicationException
	{
		Class<? extends DataVertex> store = null;
		if(StringX.startsWith(id, "/domains")){
			String key = StringX.replace(id, "/domains/", "");
			key = StringX.getFirst(key, "/");
			store = BaseDomain.getDomainType(key);
		}

		String fId = Environment.getInstance().getDataStore().formatDataStoreId(id);
		return this.getById(store, store, fId);
	}

	public <T extends DataVertex> T getById(Class<? extends DataVertex> store, Class<? extends DataVertex> partition, String id)
		throws ApplicationException
	{
		BaseQueryBuilder builder = Environment.getInstance().getIndexStore().getQueryBuilder(store, partition, 0, 2);
		builder.filter(BaseQueryBuilder.Operators.must, Environment.getInstance().getDataStore().getDataStoreIdKey(), BaseQueryBuilder.StringTypes.raw, id);
		QueryResults results = builder.execute();

		if(results.size()>1){
			throw new ApplicationException("The object requested has more than one result for store: %s partition: %s id: %s");
		}
		T result = null;
		if(!results.isEmpty())
		{
			IQueryResult queryResult=results.get(0);
			result = queryResult.getVertex();
		}
		return result;
	}

	@Override
	public <T extends DataVertex> T getByIdentifier(Class<? extends DataVertex> store, UriValue uriValue)
	{
		T result=null;
		DataVertex vertex=null;
		try
		{
			vertex=Environment.getInstance().getCache().get(store, store, uriValue.fetchValue().getValue());
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		if ((null!=vertex) && ClassX.isKindOf(vertex.getClass(), store))
		{
			//noinspection unchecked
			result=(T) vertex;
		}
		if (null==result)
		{
			result=this.getByPropertyExact(store, "/system/primitives/uri_value/identifiers.value", uriValue.fetchValue().getValue());
			if (null!=result)
			{
				try
				{
					Environment.getInstance().getCache().put(result);
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
		}
		return result;
	}

	@Override
	public <T extends DataVertex> T getByVolatileIdentifier(Class<? extends DataVertex> store, UriValue uriValue)
	{
		T result=null;
		// TODO: if edges every use identifiers this could be a problem area.
		DataVertex vertex=null;
		try
		{
			vertex=Environment.getInstance().getCache().get(store, store, uriValue.fetchValue().getValue());
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		if ((null!=vertex) && ClassX.isKindOf(vertex.getClass(), store))
		{
			//noinspection unchecked
			result=(T) vertex;
		}
		if (null==result)
		{
			Collection<T> items=this.getByVolatileIdentifier(store, uriValue, 0, 10);
			result=(items.isEmpty()) ? null : CollectionX.getFirst(items);
			if (null!=result)
			{
				try
				{
					Environment.getInstance().getCache().put(result);
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
		}
		return result;
	}

	@Override
	public <T extends DataVertex> Collection<T> getByVolatileIdentifier(Class<? extends DataVertex> store, UriValue uriValue, int start, int limit)
	{
		Collection<T> results=new ArrayList<T>();

		BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, start, limit);
		builder.filter(BaseQueryBuilder.Operators.must, "/system/primitives/uri_value/volatileIdentifiers.value", BaseQueryBuilder.StringTypes.raw, uriValue.fetchValue().toString());
		builder.useHttpRequest(false);
		QueryResults queryResults=builder.execute();

		if (null!=queryResults)
		{
			for (IQueryResult queryResult : queryResults)
			{
				//noinspection unchecked
				results.add(queryResult.getVertex());
			}
		}

		return results;
	}

	@Override
	public <T extends DataVertex> T getByIdentifier(Class<? extends DataVertex> store, URI uri)
	{
		return (null==uri) ? null : this.getByIdentifier(store, new UriValue(uri));
	}

	@Override
	public <T extends DataVertex> T getByIdentifiers(Class<? extends DataVertex> store, Collection<UriValue> uriValues)
	{
		T result=null;
		for (UriValue uriValue : uriValues)
		{
			result=this.getByIdentifier(store, uriValue);
			if (null!=result)
			{
				break;
			}
		}
		return result;
	}

	@Override
	public <T extends DataVertex> Collection<T> getAllByPropertyExact(Class<? extends DataVertex> store, String propertyName, Object value, int start, int limit)
	{
		return this.getByProperty(store, propertyName, BaseQueryBuilder.StringTypes.raw, value, start, limit);
	}

	@Override
	public <T extends DataVertex> T getByPropertyExact(Class<? extends DataVertex> store, String propertyName, Object value)
	{
		Collection<T> results=this.getAllByPropertyExact(store, propertyName, value, 0, 5);
		return CollectionX.getFirst(results);
	}

	@Override
	public <T extends DataVertex> Collection<T> getAllByPropertyIgnoreCase(Class<? extends DataVertex> store, String propertyName, Object value, int start, int limit)
	{
		return this.getByProperty(store, propertyName, BaseQueryBuilder.StringTypes.folded, value, start, limit);
	}

	@Override
	public <T extends DataVertex> T getByPropertyIgnoreCase(Class<? extends DataVertex> store, String propertyName, Object value)
	{
		Collection<T> results=this.getAllByPropertyIgnoreCase(store, propertyName, value, 0, 5);
		return CollectionX.getFirst(results);
	}

	public <T extends DataVertex> Collection<T> getByPropertiesIgnoreCase(Class<? extends DataVertex> store, int start, int limit, Object... keyValuePairs)
	{
		Collection<T> results=new ArrayList<>();
		if ((null!=keyValuePairs) && ((keyValuePairs.length%2)==0))
		{
			BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, start, limit);
			builder.sort("createdWhen", BaseQueryBuilder.Sort.asc);

			for (int i=0; i<keyValuePairs.length; i++)
			{
				Object key=keyValuePairs[i];
				i++;
				Object value=keyValuePairs[i];
				builder.filter(BaseQueryBuilder.Operators.must, key.toString(), (value instanceof String) ? BaseQueryBuilder.StringTypes.folded : BaseQueryBuilder.StringTypes.na, value);
			}

			QueryResults queryResults=builder.execute();
			if (null!=queryResults)
			{
				for (IQueryResult queryResult : queryResults)
				{
					T result=queryResult.getVertex();
					if (null!=result)
					{
						results.add(result);
					}
				}
			}
		}
		return results;
	}

	@Override
	public <T extends DataVertex> T getByTitle(Class<? extends DataVertex> store, String phrase)
	{
		BaseQueryBuilder qb=BaseQueryBuilder.getQueryBuilder(store, store, 0, 1);
		qb.filter(BaseQueryBuilder.Operators.must, "title", BaseQueryBuilder.StringTypes.raw, phrase);
		qb.sort("title", BaseQueryBuilder.Sort.asc);
		QueryResults queryResults=qb.execute();
		return queryResults.isEmpty() ? null : queryResults.getFirst();
	}

	@Override
	public <T extends DataVertex> Collection<T> getAll(Class<? extends DataVertex> store, int start, int limit)
	{
		Collection<T> results=new ArrayList<>();
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, BaseQueryBuilder.API._search, start, limit);
		qb.matchAll();
		QueryResults queryResults=qb.execute();
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

	@Override
	public int count(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, int start, int limit)
	{
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, partitionType, BaseQueryBuilder.API._search, start, limit);
		qb.matchAll();
		qb.setApi(BaseQueryBuilder.API._count);
		return qb.execute().getCount();
	}

	@Override
	public <T extends DataVertex> Collection<T> startsWith(Class<? extends DataVertex> store, String query, Integer start, Integer limit)
	{
		Collection<T> results=new ArrayList<>();
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, BaseQueryBuilder.API._search, start, limit);
		qb.filter(BaseQueryBuilder.Operators.must, "title", BaseQueryBuilder.StringTypes.starts_with, query);
		qb.sort("title", BaseQueryBuilder.Sort.asc);
		QueryResults queryResults=qb.execute();
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

	@Override
	public <T extends DataVertex> Collection<T> startsWith(Class<? extends DataVertex> store, String property, String phrase, int start, int limit)
	{
		Collection<T> results=new ArrayList<>();
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, BaseQueryBuilder.API._search, start, limit);
		qb.filter(BaseQueryBuilder.Operators.must, property, BaseQueryBuilder.StringTypes.starts_with, phrase);
		qb.sort(String.format("%s.raw", property), BaseQueryBuilder.Sort.asc);
		QueryResults queryResults=qb.execute();
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

	public <T extends DataVertex> Collection<T> getByProperty(Class<? extends DataVertex> store, String propertyName, BaseQueryBuilder.StringTypes stringType, Object value, int start, int limit)
	{
		Collection<T> results=new ArrayList<>();
		BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, start, limit);

		Object fValue=IndexHelper.getValueForIndex(value);
		if (fValue instanceof String)
		{
			builder.filter(BaseQueryBuilder.Operators.must, propertyName, stringType, fValue);
		}
		else
		{
			builder.filter(BaseQueryBuilder.Operators.must, propertyName, BaseQueryBuilder.StringTypes.na, fValue);
		}
		QueryResults queryResults=builder.execute();
		if (null!=queryResults)
		{
			for (IQueryResult queryResult : queryResults)
			{
				T result=queryResult.getVertex();
				if (null!=result)
				{
					results.add(result);
				}
			}
		}
		return results;
	}

	public <T extends DataVertex> Collection<T> getByPropertySplitPhrase(Class<? extends DataVertex> store, String propertyName, BaseQueryBuilder.StringTypes stringType, Object value, int start, int limit)
	{
		Collection<T> results=new ArrayList<>();
		BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(store, store, start, limit);

		Object fValue=IndexHelper.getValueForIndex(value);
		if (fValue instanceof String)
		{
			builder.filter(BaseQueryBuilder.Operators.must, propertyName, stringType, fValue, BaseQueryBuilder.Operators.and);
		}
		QueryResults queryResults=builder.execute();
		if (null!=queryResults)
		{
			for (IQueryResult queryResult : queryResults)
			{
				T result=queryResult.getVertex();
				if (null!=result)
				{
					results.add(result);
				}
			}
		}
		return results;
	}
}
