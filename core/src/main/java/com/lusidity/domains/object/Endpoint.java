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

package com.lusidity.domains.object;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

@AtSchemaClass(name = "Endpoint", discoverable = false)
public class Endpoint extends BaseDomain
{
	// Fields
	public static final String KEY_FROM_EP_ID="/object/endpoint/endpointFrom.relatedId";
	public static final String KEY_TO_EP_ID="/object/endpoint/endpointTo.relatedId";

	public static final String KEY_FROM_EP_ORDINAL="/object/endpoint/endpointFrom.ordinal";
	public static final String KEY_FROM_EP_LABEL="/object/endpoint/endpointFrom.label";
	public static final String KEY_FROM_EP_RELATED_TYPE="/object/endpoint/endpointFrom.relatedType";
	public static final String KEY_TO_EP_ORDINAL="/object/endpoint/endpointTo.ordinal";
	public static final String KEY_TO_EP_LABEL="/object/endpoint/endpointTo.label";
	public static final String KEY_TO_EP_RELATED_TYPE="/object/endpoint/endpointTo.relatedType";
	private KeyData<String> label=null;
	private KeyData<String> relatedId=null;
	private KeyData<String> relatedType=null;

	private transient DataVertex vertex=null;

// Constructors
	public Endpoint(){
		super();
	}

	public Endpoint(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public Endpoint(DataVertex vertex, String label, long ordinal)
	{
		super();
		this.setVertex(vertex);
		this.fetchLabel().setValue(
			(StringX.isBlank(label) ? ClassHelper.getClassKey(vertex.getClass()) : label)
		);
		this.fetchOrdinal().setValue(ordinal);
	}

	public KeyData<String> fetchLabel()
	{
		if (null==this.label)
		{
			this.label=new KeyData<>(this, "label", String.class, false, null);
		}
		return this.label;
	}

	public KeyData<String> fetchRelatedType()
	{
		if (null==this.relatedType)
		{
			this.relatedType=new KeyData<>(this, "relatedType", String.class, false, null);
		}
		return this.relatedType;
	}

	public KeyData<String> fetchRelatedId()
	{
		if (null==this.relatedId)
		{
			this.relatedId=new KeyData<>(this, "relatedId", String.class, false, null);
		}
		return this.relatedId;
	}

	// Methods
	public static String getDirectionalKey(Common.Direction direction)
	{
		return (direction==Common.Direction.OUT) ? Endpoint.KEY_FROM_EP_ID : Endpoint.KEY_TO_EP_ID;
	}

	public static String getDirectionalOrdinal(Common.Direction direction)
	{
		return (direction==Common.Direction.OUT) ? Endpoint.KEY_FROM_EP_ORDINAL : Endpoint.KEY_TO_EP_ORDINAL;
	}

	private void load()
	{
		if ((null!=this.vertex) && this.vertex.fetchId().isNullOrEmpty())
		{
			try
			{
				this.vertex.save();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
	}

// Getters and setters
	public DataVertex getVertex()
	{
		if ((null==this.vertex) && this.fetchRelatedId().isNotNullOrEmpty() && this.fetchRelatedType().isNotNullOrEmpty())
		{
			try
			{
				Class<? extends DataVertex> cls=this.getRelatedClass();
				if ((null!=cls) && ClassX.isKindOf(cls, DataVertex.class))
				{
					//noinspection unchecked
					this.vertex=VertexFactory.getInstance().get(cls, this.fetchRelatedId().getValue());
					if(null!=this.vertex)
					{
						this.vertex.setCredentials(this.getCredentials());
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return this.vertex;
	}

	public void setVertex(DataVertex vertex)
	{
		this.vertex=vertex;
		this.fetchRelatedType().setValue(ClassHelper.getClassKey(vertex.getClass()));
		this.fetchRelatedId().setValue(this.vertex.fetchId().getValue());
	}

	public Class<? extends DataVertex> getRelatedClass()
	{
		Class<? extends DataVertex> result = null;
		try
		{
			result=BaseDomain.getDomainType(this.fetchRelatedType().getValue());
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}
}
