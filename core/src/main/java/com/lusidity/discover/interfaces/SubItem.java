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

import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

public class SubItem
{
	private final Class<? extends DataVertex> store;
	private final Class<? extends DataVertex> displayType;
	private final BaseQueryBuilder qb;
	private final Class<? extends Edge> edgeType;
	private final Common.Direction direction;
	private String label=null;
	private Integer hits=0;
	private Double relevancy=null;

// Constructors
	@SuppressWarnings("ConstructorWithTooManyParameters")
	public SubItem(Class<? extends DataVertex> displayType, Class<? extends DataVertex> store, Class<? extends Edge> edgeType, BaseQueryBuilder queryBuilder, Common.Direction direction, double
		relevancy, int hits)
	{
		super();
		this.displayType=displayType;
		this.store=store;
		this.qb=queryBuilder;
		this.relevancy=relevancy;
		this.edgeType=edgeType;
		this.direction=direction;
		this.hits=hits;
	}

	@SuppressWarnings("ConstructorWithTooManyParameters")
	public SubItem(Class<? extends DataVertex> displayType, Class<? extends DataVertex> store, Class<? extends Edge> edgeType, String label, BaseQueryBuilder queryBuilder, Common.Direction
		direction, double relevancy, int hits)
	{
		super();
		this.displayType=displayType;
		this.store=store;
		this.qb=queryBuilder;
		this.relevancy=relevancy;
		this.edgeType=edgeType;
		this.direction=direction;
		this.label=label;
		this.hits=hits;
	}

	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();
		result.put("query", this.qb.getQuery());
		result.put("hits", this.getHits());
		String lbl=(StringX.isBlank(this.label) ? this.getTitle() : this.label);
		result.put("name", lbl);
		result.put("displayType", ClassHelper.getClassKey(this.displayType));
		result.put("vertexType", ClassHelper.getClassKey(this.store));
		result.put("direction", this.direction.toString());
		result.put("edgeType", (null==this.edgeType) ? ClassHelper.getClassKey(this.store) : ClassHelper.getClassKey(this.edgeType));
		result.put("relevancy", this.relevancy);
		return result;
	}

	public int getHits()
	{
		return this.hits;
	}

	public String getTitle()
	{
		String result=StringX.insertSpaceAtCapitol(this.displayType.getSimpleName());
		AtSchemaClass sc=this.displayType.getAnnotation(AtSchemaClass.class);
		if ((null!=sc) && !StringX.isBlank(sc.name()))
		{
			result=sc.name();
		}
		return result;
	}
}
