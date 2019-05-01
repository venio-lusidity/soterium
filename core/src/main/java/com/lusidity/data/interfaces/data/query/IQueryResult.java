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

package com.lusidity.data.interfaces.data.query;


import com.lusidity.data.DataVertex;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

public interface IQueryResult
{

	<T extends DataVertex> T getOtherEnd(Object dataStoreId);

	JsonData toJson();

	JsonData toJson(Class<? extends DataVertex> store, Class<? extends DataVertex> partition);

	boolean delete();

	boolean deleteIndex();

	String getKey();
	void setKey(String key);

	// Getters and setters
	<T extends Edge> T getEdge();

	String getPhrase();

	String getId();

	Double getScore();

	int getAggHits();

	<T extends DataVertex> T getVertex();

	Class<? extends DataVertex> getStoreType();

	Class<? extends DataVertex> getPartitionType();

	JsonData getIndexData();

	String getIndexId();

	String getIndexName();

	String getIndexType();

	boolean isDeleted();

	UserCredentials getCredentials();

	void setCredentials(UserCredentials credentials);
}
