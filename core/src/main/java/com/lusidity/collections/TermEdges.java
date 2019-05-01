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


import com.lusidity.data.DataVertex;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.edge.TermEdge;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;

/**
 * A fixed time period that this relationship should last.  Requires some other process to be utilized in the intended context.
 *
 * @param <T> A DataVertex
 */
public class TermEdges<T extends DataVertex> extends ElementEdges<T>
{

// Constructors
	@SuppressWarnings("unused")
	public TermEdges(PropertyAttributes propertyAttributes)
		throws ApplicationException
	{
		super(propertyAttributes);
		this.validateType();
	}

	private void validateType()
		throws ApplicationException
	{
		if (!ClassX.isKindOf(this.getEdgeType(), TermEdge.class))
		{
			throw new ApplicationException("The property \"edgeType\" in AtSchemaProperty must be a type of TermEdge.class.");
		}
	}

	@Override
	public Class<? extends Edge> getEdgeType()
	{
		return this.propertyAttributes.getEdgeType();
	}

	@SuppressWarnings("unused")
	public TermEdges()
	{
		super(null);
	}
}
