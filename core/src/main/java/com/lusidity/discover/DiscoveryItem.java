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

package com.lusidity.discover;


import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.json.JsonData;

import java.net.URI;

public interface DiscoveryItem
{

	void build(String title, String description, URI internalUri, URI externalUri, Double relevancy, Class<? extends DataVertex> cls, int hits);

	void build(String title, RawString description, URI internalUri, URI externalUri, Double relevancy, Class<? extends DataVertex> type, int hits);

	void addRelated(Class<? extends DataVertex> displayType, Class<? extends DataVertex> store, Class<? extends Edge> edgeType, BaseQueryBuilder qb, Double score, Common.Direction direction, int
		hits);

	void addRelated(Class<? extends DataVertex> displayType, Class<? extends DataVertex> store, Class<? extends Edge> edgeType, String label, BaseQueryBuilder qb, Double score, Common.Direction
		direction, int hits);

	JsonData toJson();

	// Getters and setters
	DataVertex getVertex();

	URI getUri();

	URI getExternalUri();

	String getTitle();

	void setTitle(String title);

	String getDescription();

	void setDescription(String desc);

	Double getRelevancy();

	void setRelevancy(double relevancy);

	String getName();

	String getVertexType();

	String getKey();
	String getValue();
	String getValueHighlighted();

	int getHits();
}
