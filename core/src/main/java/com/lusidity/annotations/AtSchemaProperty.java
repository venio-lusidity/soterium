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

package com.lusidity.annotations;


import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.data.Common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("UnusedDeclaration")
@Retention(RetentionPolicy.RUNTIME)
public @interface AtSchemaProperty
{
	/**
	 * A friendly name.
	 *
	 * @return A friendly name.
	 */
	String name();

	/**
	 * @return What this property represents.
	 */
	String description() default "";

	/**
	 * The direction of the relationship.
	 *
	 * @return The direction of the relationship.
	 */
	Common.Direction direction() default Common.Direction.OUT;

	/**
	 * The expected type in the collection.
	 *
	 * @return The expected type.
	 */
	Class<? extends DataVertex> expectedType();

	/**
	 * The label type for an edge.
	 * Use if set Direction to IN.
	 *
	 * @return The label type.
	 */
	Class<? extends DataVertex> labelType() default DataVertex.class;

	/**
	 * The max items allowed to return when queried for.
	 *
	 * @return Max items allowed to return when queried for.
	 */
	int limit() default Environment.COLLECTIONS_DEFAULT_LIMIT;

	/**
	 * The type of Edge used to store the relationship.
	 *
	 * @return The type of Edge used to store the relationship.
	 */
	Class<? extends Edge> edgeType() default Edge.class;

	/**
	 * Only use if direction is set to IN.
	 *
	 * @return keyName
	 */
	String fieldName() default "";

	String sortKey() default "";

	BaseQueryBuilder.Sort sortDirection() default BaseQueryBuilder.Sort.asc;

	String jsonFilter() default "";

	boolean immediate() default true;

	/**
	 * Can the discovery engine return this;
	 *
	 * @return true or false, default false
	 */
	boolean discoverable() default true;

// Getters and setters
	/**
	 * Can this collection only store a single instance.
	 *
	 * @return Is this a single instance collection, default false.
	 */
	boolean isSingleInstance() default false;

	/**
	 * Allow deprecated vertices to be retrieved.
	 */
	boolean allowDeprecated() default false;
}
