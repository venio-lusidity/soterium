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

package com.lusidity.apollo.elasticSearch;

import com.lusidity.Environment;
import com.lusidity.annotations.AtIndexedField;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.helper.PropertyHelper;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class EsIndexBuilder
{

	// Constructors
	public EsIndexBuilder()
	{
		super();
	}

	// Methods
	public static EsIndexBuilder get()
	{
		return new EsIndexBuilder();
	}

	public static boolean exists(IOperation operation, EsIndexEngine engine)
	{
		boolean exists=EsIndexBuilder.exists(operation.getStoreType(), operation.getPartitionType(), engine);
		if (!exists)
		{
			Stopwatch sw=new Stopwatch();
			sw.start();
			exists=EsIndexBuilder.createIndex(operation, engine);
			sw.stop();
			Environment.getInstance().getReportHandler().timed("%s to create an index and partition.", sw.elapsed());
		}
		return exists;
	}

	public static URL getIndexUrl(Object... parts)
	{
		String path="";
		for (Object part : parts)
		{
			if (null!=part)
			{
				if (part instanceof Class)
				{
					//noinspection unchecked
					path=String.format("%s/%s", path, EsIndexBuilder.getTypeName((Class<? extends DataVertex>) part));
				}
				else
				{
					String temp=part.toString();
					if (!StringUtils.isBlank(temp))
					{
						path=String.format("%s/%s", path, temp);
					}
				}
			}
		}
		return HttpClientX.createURL(String.format("%s%s", EsIndexBuilder.getServerUrl().toString(), path));
	}

	public static URL getServerUrl()
	{
		String protocol=EsConfiguration.getInstance().getProtocolString();
		String ip=StringX.stripStart(EsConfiguration.getInstance().getHttpHost(), "/");
		int port=EsConfiguration.getInstance().getHttpPort();
		return HttpClientX.createURL(String.format("%s://%s:%d", protocol, ip, port));
	}

	protected static synchronized boolean createIndex(IOperation operation, EsIndexEngine engine)
	{
		// This index could have been created while waiting.
		boolean iExists=EsIndexBuilder.indexExists(operation.getStoreType(), engine);
		boolean pExists=EsIndexBuilder.partitionExists(operation.getStoreType(), operation.getPartitionType(), engine);
		boolean add=false;

		if (!iExists)
		{
			iExists=EsIndexBuilder.get().addDynamicIndex(operation, engine);
			if (iExists)
			{
				add=true;
			}
		}
		if (iExists && !pExists)
		{
			pExists=EsIndexBuilder.get().addDynamicPartition(operation, engine);
			if (pExists)
			{
				add=true;
			}
		}
		if (add)
		{
			if (!EsIndexEngine.getCache().containsKey(operation.getStoreType().getName()))
			{
				EsIndexEngine.getCache().put(operation.getStoreType().getName(), new ArrayList<>());
			}
			if (pExists)
			{
				EsIndexEngine.getCache().get(operation.getStoreType().getName()).add(operation.getPartitionType().getName());
			}
		}
		return iExists && pExists;
	}

	protected static boolean exists(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, EsIndexEngine engine)
	{
		return EsIndexBuilder.partitionExists(storeType, partitionType, engine);
	}

	private static boolean partitionExists(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, EsIndexEngine engine)
	{
		boolean result=EsIndexBuilder.indexExists(storeType, engine);
		if (result)
		{
			result=EsIndexEngine.getCache().get(storeType.getName()).contains(partitionType.getName());
			if (!result)
			{
				try
				{
					URL url=null;
					if (!result)
					{
						url=EsIndexBuilder.getIndexUrl(storeType);
						url=new URL(String.format("%s/%s", url.toString(), "_mapping"));
					}

					boolean add=false;
					if (!result)
					{
						JsonData response=engine.getResponse(url, HttpClientX.Methods.GET, null);
						JsonData mappings=response.getFromPath(String.format("%s::mappings::%s",
							ClassHelper.getIndexKey(storeType),
							ClassHelper.getIndexKey(partitionType)
						));
						result=((null!=mappings) && mappings.isValid() && (!mappings.isEmpty()));
					}

					if (result)
					{
						add=true;
					}

					if (add)
					{
						if (!EsIndexEngine.getCache().containsKey(storeType.getName()))
						{
							EsIndexEngine.getCache().put(storeType.getName(), new ArrayList<>());
						}
						EsIndexEngine.getCache().get(storeType.getName()).add(partitionType.getName());
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
		}

		return result;
	}

	private static boolean indexExists(Class<? extends DataVertex> storeType, EsIndexEngine engine)
	{
		boolean result=EsIndexEngine.getCache().containsKey(storeType.getName());

		if (!result)
		{
			try
			{
				URL url=EsIndexBuilder.getIndexUrl(storeType);
				url=new URL(String.format("%s/%s", url.toString(), "_mapping"));

				JsonData response=engine.getResponse(url, HttpClientX.Methods.GET, null);
				result=HttpClientX.isValidResponse(response);
				if (!result)
				{
					JsonData mappings=response.getFromPath(String.format("%s::mappings",
						ClassHelper.getIndexKey(storeType)
					));
					result=((null!=mappings) && mappings.isValid() && (!mappings.isEmpty()));
				}

				if (result && !EsIndexEngine.getCache().containsKey(storeType.getName()))
				{
					EsIndexEngine.getCache().put(storeType.getName(), new ArrayList<>());
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		return result;
	}

	protected static String getTypeName(Class<? extends DataVertex> cls)
	{
		return ClassHelper.getIndexKey(cls);
	}

	/**
	 * Add a class as an index and dynamically creating all the known types..
	 *
	 * @param operation An IOperation.
	 */
	protected boolean addDynamicIndex(IOperation operation, EsIndexEngine engine)
	{
		boolean result=false;
		JsonData common=IndexConfiguration.getInstance().getCommon();
		if ((null!=common) && common.isValid())
		{
			try
			{
				URL url=engine.getIndexUrl(operation.getStoreType());
				JsonData mapping=common.getFromPath("mapping");
				JsonData response=engine.getResponse(url, HttpClientX.Methods.PUT, mapping);
				engine.dropIndex(operation.getStoreType(), null);
				response=engine.getResponse(url, HttpClientX.Methods.PUT, mapping);
				result=engine.isValidResponse(response);
				if (!result && response.hasKey("error"))
				{
					String error=response.getString("error");
					result=StringX.containsIgnoreCase(error, "IndexAlreadyExistsException");
					Environment.getInstance().getReportHandler().info("Failed to create index at %s.", url.toString());
				}
				else
				{
					Environment.getInstance().getReportHandler().info("Created index at %s.", String.format("%s/_mapping?pretty=true", url.toString()));
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	/**
	 * Add a specific type to an existing index.
	 *
	 * @param operation An IOperation
	 */
	private boolean addDynamicPartition(IOperation operation, EsIndexEngine engine)
	{
		boolean result=false;
		try
		{
			JsonData common=IndexConfiguration.getInstance().getCommon();
			JsonData mapping=common.getFromPath("dynamic");
			Collection<AtIndexedField> fields=PropertyHelper.getClassesAnnotatedWithProperty(operation.getPartitionType());
			if (!fields.isEmpty())
			{
				JsonData properties=JsonData.createObject();
				mapping.put("properties", properties);
				for (AtIndexedField field : fields)
				{
					if (ClassX.isKindOf(field.type(), DataVertex.class))
					{
						this.addDynamicClass(field, properties);
					}
					else
					{
						JsonData property=EsPartitionHelper.getPropertyIndexMapping(field);
						properties.put(field.key(), property);
					}
				}
			}

			URL url=engine.getIndexUrl(operation.getStoreType(), "_mapping", operation.getPartitionType());
			JsonData response=engine.getResponse(url, HttpClientX.Methods.PUT, mapping);
			result=engine.isValidResponse(response);
			if (!result && (response.hasKey("error") || response.hasKey("http_response_message")))
			{
				@SuppressWarnings("ConditionalExpressionWithIdenticalBranches")
				String error=response.hasKey("error") ? response.getString("error") : response.getString("http_response_message");
				result=StringX.containsIgnoreCase(error, "IndexAlreadyExistsException");
				Environment.getInstance().getReportHandler().info("Failed to create index partition at %s.%s%s", url.toString(), System.lineSeparator(), error);
			}
			else
			{
				Environment.getInstance().getReportHandler().info("Created index partition at %s.", String.format("%s?pretty=true", url.toString()));
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	private void addDynamicClass(AtIndexedField parent, JsonData properties)
	{
		Collection<AtIndexedField> fields=PropertyHelper.getClassesAnnotatedWithProperty(parent.type());
		if (!fields.isEmpty())
		{
			JsonData children=JsonData.createObject();
			JsonData cProperties=JsonData.createObject();
			children.put("type", "object");
			children.put("properties", cProperties);
			properties.put(parent.key(), children);
			for (AtIndexedField field : fields)
			{
				if (ClassX.isKindOf(field.type(), DataVertex.class))
				{
					this.addDynamicClass(field, cProperties);
				}
				else
				{
					JsonData property=EsPartitionHelper.getPropertyIndexMapping(field);
					cProperties.put(field.key(), property);
				}
			}
		}
	}
}
