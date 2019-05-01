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

package com.lusidity.discover.tasks;

import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.discover.DiscoveryEngine;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.DiscoveryItems;
import com.lusidity.discover.generic.GenericProvider;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.system.security.UserCredentials;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

public class SearchTask implements Callable<DiscoveryItems>
{
	private static final Double DEFALUT_SCORE = 0.25;
	private final String phrase;
	private final int limit;
	private final Class<? extends DataVertex> store;
	private final int start;
	private final boolean suggest;
	private final UserCredentials credentials;

	// Constructors
	public SearchTask(UserCredentials credentials, Class<? extends DataVertex> store, boolean suggest, String phrase, int start, int limit){
		super();
		this.start = start;
		this.limit = limit;
		this.phrase = phrase;
		this.store=store;
		this.suggest = suggest;
		this.credentials = credentials;
	}

	// Overrides
	@SuppressWarnings("unchecked")
	@Override
	public DiscoveryItems call()
		throws Exception
	{
		DiscoveryItems results = new DiscoveryItems(new GenericProvider(), 0);
		if(StringX.startsWith(this.phrase, "/domains/")){
			try
			{
				String indexName =ClassHelper.getIndexKey(this.store);
				if(StringX.contains(this.phrase, indexName))
				{
					DataVertex vertex=VertexFactory.getInstance().get(this.phrase);
					if ((null!=vertex) && ClassX.isKindOf(vertex, ApolloVertex.class))
					{
						ApolloVertex av=(ApolloVertex) vertex;
						DiscoveryItem item=vertex.getDiscoveryItem(av.fetchTitle().getValue(), this.credentials, "uri", this.phrase, this.suggest);
						results.add(item);
						results.setHits(1);
					}
				}
			}
			catch (Exception ignored){}
		}
		else
		{
			this.discover(results, this.store, null, 1);
		}

		return results;
	}

	private void discover(DiscoveryItems results, Class<? extends DataVertex> cls, String parentKey, int depth)
		throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		if(depth<3)
		{
			AtSchemaClass schemaClass=cls.getAnnotation(AtSchemaClass.class);
			if ((null!=schemaClass) && (schemaClass.discoverable() || (depth>1)))
			{
				Constructor constructor=cls.getConstructor();
				DataVertex vertex=(DataVertex) constructor.newInstance();
				Map<String, Method> methods=PropertyHelper.getAllMethods(cls);

				for (Map.Entry<String, Method> entry : methods.entrySet())
				{
					try
					{
						Method method=entry.getValue();
						if (ClassX.isKindOf(method.getReturnType(), KeyData.class))
						{
							this.suggest(vertex, results, method, parentKey);
						}
						else if (ClassX.isKindOf(method.getReturnType(), KeyDataCollection.class))
						{
							/*
							//todo:  Do not enable this yet.
							KeyDataCollection kdc=(KeyDataCollection) method.invoke(vertex);
							if(ClassX.isKindOf(kdc.getKeyData().getFieldType(), DataVertex.class))
							{									//noinspection unchecked
								this.discover(results, kdc.getKeyData().getFieldType(), kdc.getKeyData().getKeyName(), depth+1);
							}
							else{
								this.suggest(results, kdc.getKeyData().getFieldType(), kdc.getKeyData().getKeyName(), parentKey);
							}
							*/
						}
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().warning(ex);
					}
				}
			}
		}
	}

	@SuppressWarnings("MethodWithTooExceptionsDeclared")
	private void suggest(DataVertex vertex, DiscoveryItems results, Method method, String parentKey)
	{
		try
		{
			method.setAccessible(true);
			KeyData keyData=(KeyData) method.invoke(vertex);
			if (keyData.isDiscoverable())
			{
				this.suggest(results, keyData.getFieldType(), keyData.getKeyName(), parentKey);
			}
		}
		catch (Exception ex){
			if(Environment.getInstance().isDevOnly())
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
	}

	private void suggest(DiscoveryItems results, Class cls, String keyName, String parentKey)
	{
		String key=String.format("%s%s", (StringX.isBlank(parentKey) ? "" : String.format("%s.", parentKey)), keyName);
		DiscoveryEngine engine = new DiscoveryEngine(this.credentials);
		engine.suggest(this.suggest, this.phrase, this.start, this.limit, results, this.store, key, cls, null, null);
	}
}
