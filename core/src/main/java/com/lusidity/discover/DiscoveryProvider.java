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


import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.discover.interfaces.SuggestItem;
import com.lusidity.system.security.UserCredentials;

import java.util.Collection;

public interface DiscoveryProvider
{
	DiscoveryItems discover(String phrase, boolean suggest, int start, int limit, UserCredentials credentials);
	DiscoveryItems process(String phrase, ApolloVertex vertex, String key, Object value, int start, int limit, boolean suggest);
	Collection<SuggestItem> suggest(String phrase, int start, int limit, UserCredentials credentials);

	boolean handles(DataVertex vertex);

	boolean isValid(String phrase);

	int getHits(BaseQueryBuilder queryBuilder);

	boolean providerHandles(Class<? extends ApolloVertex> cls);

	// Getters and setters
	Class getVertexType();
}
