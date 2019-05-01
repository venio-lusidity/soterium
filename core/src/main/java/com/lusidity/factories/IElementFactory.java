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

package com.lusidity.factories;

import com.lusidity.collections.ElementAttributes;
import com.lusidity.collections.PropertyAttributes;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.exceptions.ApplicationException;

import java.net.URI;
import java.util.Collection;

@SuppressWarnings("unused")
public interface IElementFactory
{

	/**
	 * Get and Element by its URI.
	 *
	 * @param <T>   The type.
	 * @param store The store class.
	 * @param uri   The Uri of the Element.  @return An Element of type T.
	 */
	<T extends DataVertex> T get(Class<? extends DataVertex> store, URI uri);


	<T extends DataVertex> T get(String dataStoreId)
		throws Exception;

	/**
	 * Get and Element by its URI.
	 *
	 * @param <T>         The type.
	 * @param dataStoreId The id of the Element as represented in the data store.  @return An Element of type T.
	 */
	<T extends DataVertex> T get(Class<? extends DataVertex> store, Object dataStoreId);

	/**
	 * Delete an existing Element.
	 *
	 * @param dataVertex The Element to delete.
	 * @param store      The store class.
	 * @return true if the Element was successfully deleted.
	 */
	boolean delete(DataVertex dataVertex, Class<? extends DataVertex> store);

	<T extends DataVertex> ElementAttributes<T> attributes(Class<? extends ElementAttributes> cls, PropertyAttributes config);

	DataVertex merge(DataVertex fromVertex, DataVertex toVertex)
		throws ApplicationException;

	<T extends DataVertex> T getByIdentifier(Class<? extends DataVertex> store, UriValue uriValue);

	<T extends DataVertex> T getByVolatileIdentifier(Class<? extends DataVertex> store, UriValue uriValue);

	<T extends DataVertex> Collection<T> getByVolatileIdentifier(Class<? extends DataVertex> store, UriValue uriValue, int start, int limit);

	<T extends DataVertex> T getByIdentifier(Class<? extends DataVertex> store, URI uri);

	<T extends DataVertex> T getByIdentifiers(Class<? extends DataVertex> store, Collection<UriValue> uriValues);

	<T extends DataVertex> Collection<T> getAllByPropertyExact(Class<? extends DataVertex> store, String propertyName,
	                                                           Object value, int start, int limit);

	<T extends DataVertex> T getByPropertyExact(Class<? extends DataVertex> store, String propertyName, Object value);

	<T extends DataVertex> Collection<T> getAllByPropertyIgnoreCase(Class<? extends DataVertex> store, String propertyName, Object value, int start, int limit);

	<T extends DataVertex> T getByPropertyIgnoreCase(Class<? extends DataVertex> store, String propertyName, Object value);

	<T extends DataVertex> Collection<T> getByPropertiesIgnoreCase(Class<? extends DataVertex> store, int start, int limit, Object... keyValuePairs);

	<T extends DataVertex> T getByTitle(Class<? extends DataVertex> store, String phrase);

	<T extends DataVertex> Collection<T> getAll(Class<? extends DataVertex> store, int start, int limit);

	int count(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, int start, int limit);

	<T extends DataVertex> Collection<T> startsWith(Class<? extends DataVertex> store, String query, Integer start, Integer limit);

	<T extends DataVertex> Collection<T> startsWith(Class<? extends DataVertex> store, String property, String phrase, int start, int limit);
}
