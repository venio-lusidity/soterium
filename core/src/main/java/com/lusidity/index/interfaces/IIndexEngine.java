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

package com.lusidity.index.interfaces;

import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;

import java.net.URL;

public interface IIndexEngine
{
	URL getIndexUrl(Object... parts);

	boolean execute(IOperation operation)
		throws Exception;

	boolean create(Class<? extends DataVertex> store, DataVertex vertex, Object dataStoreId, int attempt);

	boolean delete(Class<? extends DataVertex> store, DataVertex vertex);

	boolean makeAvailable(Class<? extends DataVertex> store);

	boolean flush(Class<? extends DataVertex> store);

	boolean update(Class<? extends DataVertex> store, DataVertex vertex, int attempt);

	boolean isValidResponse(JsonData response);

	JsonData getResponse(URL url, HttpClientX.Methods method, Object content, String... headers);

	String getTypeName(Class<? extends DataVertex> cls);

	JsonData toJson();

// Getters and setters
	boolean isDataStoreEnabled();

	URL getServerUrl();
}
