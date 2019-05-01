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
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.UserCredentials;

@SuppressWarnings({
	"RedundantFieldInitialization",
	"EqualsAndHashcode"
})
public class EsQueryResult implements IQueryResult
{
	private final Double score;
	private final String phrase;
	private final int aggHits;
	private String id=null;
	private JsonData indexData=null;
	private DataVertex dataVertex=null;
	private boolean deleted=false;
	private Class<? extends DataVertex> storeType=null;
	private Class<? extends DataVertex> vertexType=null;
	private Class<? extends DataVertex> partitionType=null;
	private UserCredentials credentials=null;
	private String key=null;

	// Constructors
	public EsQueryResult(String id, int aggHits, Double score, JsonData indexData, Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType)
	{
		super();
		this.phrase=null;
		this.id=id;
		this.score=score;
		this.aggHits=aggHits;
		this.indexData=indexData;
		this.storeType=storeType;
		this.partitionType=partitionType;
		String vt=this.indexData.getString("_source::vertexType");
		if (!StringX.isBlank(vt))
		{
			this.vertexType=BaseDomain.getDomainType(vt);
		}
	}

	// Overrides
	@Override
	public <T extends DataVertex> T getOtherEnd(Object dataStoreId)
	{
		T result=null;
		if (null!=dataStoreId)
		{
			Edge edge=this.getEdge();
			if (null!=edge)
			{
				Endpoint endpoint=edge.getOther(dataStoreId.toString());
				if ((null!=endpoint) && (null!=endpoint.getVertex()))
				{
					//noinspection unchecked
					result=(T) endpoint.getVertex();
					result.fetchOrdinal().setValue(edge.getEndpoint(dataStoreId.toString()).fetchOrdinal().getValue());
				}
			}
		}
		return result;
	}

	private DataVertex getResultObject()
	{
		DataVertex result=null;
		try
		{
			JsonData dso=this.getIndexData().getFromPath("_source");
			Class<? extends DataVertex> cls=BaseDomain.getDomainType(dso.getString("vertexType"));
			if (Environment.getInstance().getIndexStore().getEngine().isDataStoreEnabled() && (ClassX.isKindOf(cls, Edge.class) || ClassX.isKindOf(cls, this.getStoreType())))
			{
				result=ClassHelper.as(dso, cls);
				if (null!=result)
				{
					result.setIndexId(this.getIndexId());
					try
					{
						Environment.getInstance().getCache().put(result.getClass(), result.getClass(), result.fetchId().getValue(), result);
					}
					catch (Exception ignored)
					{
					}
				}
			}
			else
			{
				// This is an extended property and the parent object needs to be retrieved.
				String dataId=dso.getString(IDataStore.DATA_STORE_ID);
				if (null==this.getStoreType())
				{
					this.storeType=cls;
				}
				Class<? extends DataVertex> partition=(ClassX.isKindOf(this.getStoreType(), Edge.class)) ? this.getPartitionType() : this.getStoreType();
				result=Environment.getInstance().getDataStore().getObjectById(this.getStoreType(), partition, dataId);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return result;
	}

	@Override
	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();

		result.put("score", this.score);
		result.put("id", this.id);
		result.put("phrase", this.phrase);
		result.put("getAggHits", this.aggHits);
		return result;
	}

	@Override
	public JsonData toJson(Class<? extends DataVertex> store, Class<? extends DataVertex> partition)
	{
		JsonData result=this.toJson();

		DataVertex dv=this.getVertex();
		if (null!=dv)
		{
			result.put("element", dv.toJson(false));
		}

		return result;
	}

	@Override
	public boolean delete()
	{
		if (!this.deleted)
		{
			DataVertex vertex=this.getVertex();
			if (null!=vertex)
			{
				try
				{
					this.deleted=vertex.delete();
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
			else
			{
				this.deleted=this.deleteIndex();
			}
		}

		return this.deleted;
	}

	@Override
	public boolean deleteIndex()
	{
		if (!this.deleted)
		{
			this.deleted=Environment.getInstance().getIndexStore().delete(this.getIndexName(), this.getIndexType(), this.getIndexId());
		}
		return this.deleted;
	}

	@Override
	public String getKey()
	{
		return this.key;
	}

	@Override
	public void setKey(String key)
	{
		this.key=key;
	}

	@Override
	public <T extends Edge> T getEdge()
	{
		T result=null;
		DataVertex vertex=this.getResultObject();
		if (ClassX.isKindOf(vertex, Edge.class))
		{
			//noinspection unchecked
			result=(T) vertex;
		}
		return result;
	}

	@Override
	public String getPhrase()
	{
		return this.phrase;
	}

	@Override
	public String getId()
	{
		if (null==this.id)
		{
			this.id=(null!=this.getVertex()) ? this.getVertex().fetchId().getValue() : null;
		}
		//noinspection NestedConditionalExpression
		return this.id;
	}

	@Override
	public Double getScore()
	{
		return this.score;
	}

	@Override
	public int getAggHits()
	{
		return this.aggHits;
	}

	@Override
	public <T extends DataVertex> T getVertex()
	{
		T result=null;
		DataVertex vertex=this.getResultObject();
		if (null!=vertex)
		{
			//noinspection unchecked
			result=(T) vertex;
		}
		return result;
	}

	@Override
	public Class<? extends DataVertex> getStoreType()
	{
		return this.storeType;
	}

	@Override
	public Class<? extends DataVertex> getPartitionType()
	{
		return this.partitionType;
	}

	@Override
	public JsonData getIndexData()
	{
		return this.indexData;
	}

	@Override
	public String getIndexId()
	{
		return this.indexData.getString("_id");
	}

	@Override
	public String getIndexName()
	{
		return this.indexData.getString("_index");
	}

	@Override
	public String getIndexType()
	{
		return this.indexData.getString("_type");
	}

	@Override
	public boolean isDeleted()
	{
		return this.deleted;
	}

	@Override
	public UserCredentials getCredentials()
	{
		return this.credentials;
	}

	@Override
	public void setCredentials(UserCredentials credentials)
	{
		this.credentials=credentials;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if (obj instanceof IQueryResult)
		{
			IQueryResult that=(IQueryResult) obj;
			result=(null!=this.getId()) && (null!=that.getId()) && this.getId().equals(that.getId());
		}
		return result;
	}

	// Getters and setters
	public Class<? extends DataVertex> getVertexType()
	{
		return this.vertexType;
	}
}
