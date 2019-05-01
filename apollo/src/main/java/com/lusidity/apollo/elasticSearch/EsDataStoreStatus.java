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
import com.lusidity.components.Module;
import com.lusidity.data.ApolloVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;
import org.joda.time.Duration;
import org.json.JSONObject;

import java.net.URL;
import java.util.Map;

public class EsDataStoreStatus implements Module
{
	private static EsDataStoreStatus INSTANCE=null;
	private static long AVERAGE_SEARCH_TIME=0;
	private static long SEARCHES=0;
	private boolean opened=false;

	// Constructors
	public EsDataStoreStatus()
	{
		super();
		try
		{
			this.open();
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public void open(Object... params)
		throws ApplicationException
	{
		this.opened=true;
	}

	@Override
	public void close()
	{
		// nothing to close
	}

	@Override
	public boolean isOpened()
	{
		return this.opened;
	}

	@Override
	public boolean isEnabled()
	{
		return this.opened;
	}

	// Methods
	public static void start()
	{
		EsDataStoreStatus.getInstance();
	}

	public static synchronized EsDataStoreStatus getInstance()
	{
		if (null==EsDataStoreStatus.INSTANCE)
		{
			EsDataStoreStatus.INSTANCE=new EsDataStoreStatus();
		}
		return EsDataStoreStatus.INSTANCE;
	}

	public synchronized void updateSearchStatus(Duration duration)
	{
		EsDataStoreStatus.SEARCHES++;
		EsDataStoreStatus.AVERAGE_SEARCH_TIME=(EsDataStoreStatus.AVERAGE_SEARCH_TIME+duration.getMillis())/2;
	}

	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();
		JsonData search=JsonData.createArray();
		search.put(JsonData.createObject().put("key", "averageSearchTime").put("label", "Average Seek Time").put("value", EsDataStoreStatus.AVERAGE_SEARCH_TIME));
		search.put(JsonData.createObject().put("key", "SEARCHES").put("label", "Total Searches").put("value", EsDataStoreStatus.SEARCHES));
		result.put("search", search);
		result.put("config", "");
		JsonData buckets=JsonData.createArray();

		Map<String, Class<? extends ApolloVertex>> domains=Environment.getInstance().getApolloVertexTypes();

		if (null!=domains)
		{

			try
			{
				URL url=new URL(String.format("%s://%s:%d/_cat/indices?format=json", EsConfiguration.getInstance().getProtocol().getSchemeName(),
					EsConfiguration.getInstance().getHttpHost(), EsConfiguration.getInstance().getHttpPort()
				));
				JsonData response=HttpClientX.getResponse(url, HttpClientX.Methods.GET, null, Environment.getInstance().getConfig().getReferer());
				if ((null!=response) && response.isJSONArray())
				{
					response.sort("index");
					for (Object o : response)
					{
						if (o instanceof JSONObject)
						{
							try
							{
								JsonData node=new JsonData(o);
								String index=node.getString("index");
								url=new URL(String.format("%s://%s:%d/%s/_mapping?format=json", EsConfiguration.getInstance().getProtocol().getSchemeName(),
									EsConfiguration.getInstance().getHttpHost(), EsConfiguration.getInstance().getHttpPort(), index
								));

								JsonData mapping=HttpClientX.getResponse(url, HttpClientX.Methods.GET, null, Environment.getInstance().getConfig().getReferer());
								if ((null!=mapping) && mapping.isJSONObject())
								{
									JsonData bucket=JsonData.createObject();
									JsonData items=JsonData.createArray();
									JsonData mappings=mapping.getFromPath(index, "mappings");
									int total=0;
									for (String key : mappings.keys())
									{
										try
										{
											url=new URL(String.format("%s://%s:%d/%s/%s/_count?format=json", EsConfiguration.getInstance().getProtocol().getSchemeName(),
												EsConfiguration.getInstance().getHttpHost(), EsConfiguration.getInstance().getHttpPort(), index, key
											));
											JsonData count=HttpClientX.getResponse(url, HttpClientX.Methods.GET, null, Environment.getInstance().getConfig().getReferer());
											if (null!=count)
											{
												Integer value=count.getInteger("count");
												if (null==value)
												{
													value=0;
												}
												total+=value;
												JsonData item=JsonData.createObject();
												item.put("label", key);
												item.put("value", value);
												item.put("cls", BaseDomain.getDomainType(key).getName());
												items.put(item);
											}
										}
										catch (Exception ignored)
										{
										}
									}
									bucket.put("title", index);
									bucket.put("cls", BaseDomain.getDomainType(index).getName());
									bucket.put("count", total);
									bucket.put("types", items);
									buckets.put(bucket);
								}
							}
							catch (Exception ignored)
							{
							}
						}
					}
				}

			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		result.put("buckets", buckets);
		return result;
	}
}
