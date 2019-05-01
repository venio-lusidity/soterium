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

package com.lusidity.collections;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.lang.reflect.Field;
import java.util.Objects;

public class PropertyAttributes
{
	private Class<? extends DataVertex> labelType=null;
	private Class<? extends Edge> edgeType=null;
	private Class<? extends DataVertex> expectedType=null;
	private DataVertex vertex=null;
	private String key=null;
	private String name=null;
	private String description=null;
	private Common.Direction direction=Common.Direction.OUT;
	private Integer limit=null;
	private boolean singleInstance=false;
	private boolean indexed=true;
	private Class<? extends DataVertex> partitionType=null;
	private String sortKey=null;
	private JsonData filters=null;
	private boolean preload=true;
	private boolean immediate=true;
	private boolean allowDeprecated= false;
	private BaseQueryBuilder.Sort sortdirection = BaseQueryBuilder.Sort.asc;


	// Constructors
	public PropertyAttributes()
	{
		super();
	}

	public PropertyAttributes(DataVertex vertex, Field field, AtSchemaProperty schema)
	{
		super();
		this.vertex=vertex;
		this.name=schema.name();
		this.description=schema.description();
		this.direction=schema.direction();
		this.limit=schema.limit();
		this.immediate=schema.immediate();
		this.expectedType=schema.expectedType();
		this.labelType=((Common.Direction.OUT==this.direction) || (Objects.equals(schema.labelType(), DataVertex.class))) ?
			this.expectedType : schema.labelType();
		this.edgeType=schema.edgeType();
		this.singleInstance=schema.isSingleInstance();
		this.sortKey=schema.sortKey();
		this.partitionType=(Common.Direction.OUT==this.direction) ? vertex.getClass() : null;
		String fieldName=StringX.isBlank(schema.fieldName()) ? field.getName() : schema.fieldName();
		this.key=ClassHelper.getPropertyKey(this.getLabelType(), fieldName);
		this.filters=new JsonData(schema.jsonFilter());
		this.allowDeprecated= schema.allowDeprecated();
		this.sortdirection = schema.sortDirection();
	}

	public boolean allowDeprecated()
	{
		return this.allowDeprecated;
	}

	public Class<? extends DataVertex> getLabelType()
	{
		return this.labelType;
	}

// Methods
	public static PropertyAttributes create()
	{
		return new PropertyAttributes();
	}

	public boolean preLoad()
	{
		return this.preload;
	}

// Getters and setters
	public boolean isImmediate()
	{
		return this.immediate;
	}

	public DataVertex getVertex()
	{
		return this.vertex;
	}

	public BaseQueryBuilder.Sort getSortDirection()
	{
		return this.sortdirection;
	}

	public void setVertex(DataVertex vertex)
	{
		this.vertex=vertex;
	}

	public String getKey()
	{
		return this.key;
	}

	public PropertyAttributes setKey(String key)
	{
		this.key=key;
		return this;
	}

	public Class<? extends DataVertex> getPartitionType()
	{
		return this.partitionType;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description=description;
	}

	public Common.Direction getDirection()
	{
		return this.direction;
	}

	public PropertyAttributes setDirection(Common.Direction direction)
	{
		this.direction=direction;
		return this;
	}

	public Class<? extends Edge> getEdgeType()
	{
		return this.edgeType;
	}

	public Class<? extends DataVertex> getExpectedType()
	{
		return this.expectedType;
	}

	public void setExpectedType(Class<? extends DataVertex> expectedType)
	{
		this.expectedType=expectedType;
	}

	public int getLimit()
	{
		return (null==this.limit) ? Environment.COLLECTIONS_DEFAULT_LIMIT : this.limit;
	}

	public void setLimit(int limit)
	{
		this.limit=limit;
	}

	public boolean isIndexed()
	{
		return this.indexed;
	}

	public boolean isSingleInstance()
	{
		return this.singleInstance;
	}

	public String getSortKey()
	{
		return this.sortKey;
	}

	public JsonData getFilters()
	{
		return this.filters;
	}

	public void setPreload(boolean preload)
	{
		this.preload=preload;
	}

	public PropertyAttributes setDataVertex(DataVertex vertex)
	{
		this.vertex=vertex;
		return this;
	}
}
