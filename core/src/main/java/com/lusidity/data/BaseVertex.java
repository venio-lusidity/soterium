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

package com.lusidity.data;

import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementAttributes;
import com.lusidity.collections.PropertyAttributes;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.IQueryResultHandler;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.cbac.ISecurityPolicy;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({
	"ClassReferencesSubclass",
	"ClassMayBeInterface"
})
public abstract class BaseVertex
{
	@SuppressWarnings("unused")
	public abstract KeyData<String> fetchAppVersion();

	public abstract KeyData<String>  fetchId();

	@SuppressWarnings("unused")
	public abstract KeyData<DateTime> fetchCreatedWhen();

	public abstract KeyData<DateTime> fetchModifiedWhen();

	public abstract KeyData<Long> fetchOrdinal();

	public abstract KeyData<String> fetchVertexType();

	public abstract String formatProperty(String name, String value);

	public abstract boolean hasId();

	public abstract boolean enforcePolicy();

	public abstract JsonData toJson(boolean storing, Collection<? extends DataVertex> items);

	public abstract JsonData toJson(boolean storing, String... languages);

	public abstract JsonData toIndex();

	public abstract void buildProperty(String fieldName);

	public abstract ElementAttributes build(DataVertex dataVertex, Field field, AtSchemaProperty schemaProperty)
		throws ApplicationException;

	public abstract PolicyDecisionPoint getSecurityPolicy(BasePrincipal principal, ISecurityPolicy... policies);

	public abstract boolean save()
		throws Exception;

	public abstract boolean save(Class<? extends DataVertex> store)
		throws Exception;

	public abstract boolean save(Class<? extends DataVertex> store, IDataStore dataStore)
		throws Exception;

	public abstract boolean save(IDataStore dataStore)
		throws Exception;

	public abstract boolean delete();

	public abstract boolean delete(IDataStore dataStore);

	public abstract boolean deleteAllEdges();

	public abstract <T extends DataVertex> T move(Class<? extends DataVertex> toClass);

	public abstract <T extends DataVertex> T mergeTo(DataVertex vertex, boolean delete);

	public abstract void afterUpdate(LogEntry.OperationTypes operationType, boolean success);

	public abstract void beforeUpdate(LogEntry.OperationTypes operationType);

	public abstract void beforeEdgeUpdate(LogEntry.OperationTypes operationType, DataVertex other, Edge edge);

	public abstract void afterEdgeUpdate(LogEntry.OperationTypes operationType, DataVertex other, Edge edge, boolean success);

	public abstract <T extends DataVertex> ElementAttributes<T> attributes(Class<? extends ElementAttributes> cls, PropertyAttributes propertyAttributes);

	public abstract DiscoveryItem getDiscoveryItem(String phrase, UserCredentials userCredentials, String key, Object value, boolean suggest);

	// Getters and setters
	public abstract URI getUri();

	public abstract boolean isDeleting();

	public abstract void setDeleting(boolean deleting);

	public abstract boolean isUnusable();

	public abstract boolean isDeleted();

	public abstract void setDeleted(boolean deleted);

	@SuppressWarnings("unused")
	public abstract Duration getAge();

	public abstract boolean isWritable();

	public abstract List<IQueryResultHandler> getQueryResultHandlers();

	public abstract Class<? extends DataVertex> getActualClass();
}
