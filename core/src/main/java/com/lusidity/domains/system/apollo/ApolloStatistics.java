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

package com.lusidity.domains.system.apollo;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.system.primitives.SynchronizedLong;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

@AtSchemaClass(name = "Apollo Statistics", description = "Information related to the operations of a datastore.", discoverable = false)
public class ApolloStatistics extends BaseDomain
{
	public static class Queries
	{
		// Methods
		public static synchronized ApolloStatistics get()
		{
			return ApolloStatistics.Queries.get(Environment.getInstance().getConfig().getServerName());
		}

		private static synchronized ApolloStatistics get(String serverName)
		{
			return VertexFactory.getInstance().getByTitle(ApolloStatistics.class, serverName);
		}
	}
	private KeyData<SynchronizedLong> create = null;
	private KeyData<SynchronizedLong> deleted = null;
	private KeyData<SynchronizedLong> updated = null;
	private KeyData<SynchronizedLong> queried = null;

	// Constructors
	public ApolloStatistics(){
		super();
	}

	public ApolloStatistics(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	// Overrides
	@Override
	public JsonData toJson(boolean storing, String... languages)
	{
		JsonData result=super.toJson(storing, languages);
		if (!storing)
		{
			result.put("available", true);
		}
		return result;
	}

	@Override
	public boolean save()
		throws Exception
	{
		if(StringX.isBlank(this.fetchTitle().getValue())){
			this.fetchTitle().setValue(Environment.getInstance().getConfig().getServerName());
		}
		return super.save();
	}

	@Override
	public boolean save(IDataStore dataStore)
		throws Exception
	{
		if(StringX.isBlank(this.fetchTitle().getValue())){
			this.fetchTitle().setValue(Environment.getInstance().getConfig().getServerName());
		}
		return super.save(dataStore);
	}

	@Override
	public boolean save(Class<? extends DataVertex> store, IDataStore dataStore)
		throws Exception
	{
		if(StringX.isBlank(this.fetchTitle().getValue())){
			this.fetchTitle().setValue(Environment.getInstance().getConfig().getServerName());
		}
		return super.save(store, dataStore);
	}

	public KeyData<SynchronizedLong> fetchCreated()
	{
		if (null==this.create)
		{
			this.create=new KeyData<>(this, "create", SynchronizedLong.class, false, new SynchronizedLong());
		}
		return this.create;
	}

	public KeyData<SynchronizedLong> fetchDeleted()
	{
		if (null==this.deleted)
		{
			this.deleted=new KeyData<>(this, "deleted", SynchronizedLong.class, false, new SynchronizedLong());
		}
		return this.deleted;
	}

	public KeyData<SynchronizedLong> fetchUpdated()
	{
		if (null==this.updated)
		{
			this.updated=new KeyData<>(this, "updated", SynchronizedLong.class, false, new SynchronizedLong());
		}
		return this.updated;
	}

	public KeyData<SynchronizedLong> fetchQueried()
	{
		if (null==this.queried)
		{
			this.queried=new KeyData<>(this, "queried", SynchronizedLong.class, false, new SynchronizedLong());
		}
		return this.queried;
	}
}
