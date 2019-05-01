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

import com.lusidity.data.DataVertex;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.index.interfaces.IReIndexer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class EsReIndexer implements IReIndexer
{
	public class IndexAlias
	{
		private final int version;
		private final URL url;
		private final boolean exists;

		// Constructors
		public IndexAlias(URL url, int version, boolean exists)
		{
			super();
			this.url=url;
			this.exists=exists;
			this.version=version;
		}

		// Getters and setters
		public boolean isExists()
		{
			return this.exists;
		}

		public URL getUrl()
		{
			return this.url;
		}

		public int getVersion()
		{
			return this.version;
		}
	}

	private final Class<? extends DataVertex> store;
	private final Class<? extends DataVertex> partitionType;
	private final JsonData map;
	private final EsIndexEngine engine;
	private boolean stopped=true;
	private boolean canStop=true;
	private int total=0;
	private int processed=0;

	// Constructors
	public EsReIndexer(Class<? extends DataVertex> indexStore, Class<? extends DataVertex> partionType, JsonData map, EsIndexEngine engine)
		throws ApplicationException

	{
		super();
		if (null==indexStore)
		{
			throw new ApplicationException("The store is not in the expected format.");
		}
		if (null==partionType)
		{
			throw new ApplicationException("The partitionType is not in the expected format.");
		}
		if (!map.isJSONObject())
		{
			throw new ApplicationException("The step1 is not in the expected format.");
		}
		this.store=indexStore;
		this.partitionType=partionType;
		this.map=map;
		this.engine=engine;
	}

	// Overrides
	@Override
	public void process()
		throws ApplicationException
	{
		this.stopped=false;
		Collection<EsReIndexer.IndexAlias> aliases=new ArrayList<>();
		for (int i=0; i<2; i++)
		{
			EsReIndexer.IndexAlias alias=this.getAlias(i+1);
			if (null!=alias)
			{
				aliases.add(alias);
			}
		}
		for (EsReIndexer.IndexAlias alias : aliases)
		{
			if (!alias.exists)
			{

			}
		}

		String partitionTypeName=this.engine.getTypeName(this.partitionType);

		JsonData content=JsonData.createObject();
		JsonData actions=JsonData.createArray();
		for (EsReIndexer.IndexAlias alias : aliases)
		{
			JsonData action=JsonData.createObject();
			JsonData remove=JsonData.createObject();
			remove.put("alias", partitionTypeName);
			remove.put("index", String.format("%s_v%d", partitionTypeName, alias.version));
			action.put(alias.exists ? "remove" : "add", remove);
			actions.put(action);
		}
		content.put("actions", actions);

		//TODO post the actions

	}

	private EsReIndexer.IndexAlias getAlias(int version)
	{
		EsReIndexer.IndexAlias result=null;
		try
		{
			URL url=this.engine.getIndexUrl(this.store, this.partitionType);
			url=new URL(String.format("%s_v%d", url.toString(), version));
			URL test=new URL(String.format("%s/%s", url.toString(), "_status"));
			JsonData response=this.engine.getResponse(test, HttpClientX.Methods.GET, null);
			boolean exists=this.engine.isValidResponse(response) && response.getBoolean("found");
			result=new EsReIndexer.IndexAlias(url, version, exists);
		}
		catch (Exception ignored)
		{
		}
		return result;
	}

	@Override
	public void stop()
	{
		this.stopped=true;
	}

	@Override
	public boolean isStarted()
	{
		return !this.stopped;
	}

	@Override
	public void close()
		throws IOException
	{
		while (!this.canStop)
		{
		}
	}

	@Override
	public String getProgressStatusText()
	{
		return null;
	}

	@Override
	public int getCurrentProgressCount()
	{
		return 0;
	}

	@Override
	public int getWholeProcessCount()
	{
		return 0;
	}
}
