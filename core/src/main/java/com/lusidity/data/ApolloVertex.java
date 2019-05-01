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

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementAttributes;
import com.lusidity.collections.ElementEdges;
import com.lusidity.collections.PropertyAttributes;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDataHandlerIdCallBack;
import com.lusidity.data.handler.KeyDataVertexTypeHandler;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.data.interfaces.data.query.IQueryResultHandler;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.generic.GenericItem;
import com.lusidity.discover.interfaces.SuggestItem;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.office.ExcelSchema;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.cbac.ISecurityPolicy;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

@SuppressWarnings({
	"UnusedDeclaration",
	"ThrowCaughtLocally",
	"BooleanParameter",
	"ClassWithTooManyFields",
	"ComparableImplementedButEqualsNotOverridden",
	"OverlyComplexClass",
	"OverlyCoupledClass",
	"FieldMayBeFinal"
})
@AtSchemaClass(name="Apollo Vertex", discoverable=false)
public class ApolloVertex extends DataVertex implements Comparable<DataVertex>
{
	private boolean deleting = false;

	public static final String KEY_URI = "/vertex/uri";
	@SuppressWarnings("EnumeratedConstantNamingConvention")
	public enum SharedAccessLevels
	{
		Shared,
		Public,
		Private
	}

	private static String KEY_CHANGE_LOG=null;
	private static String KEY_PRINCIPALS=null;
	private static String KEY_DESCRIPTIONS=null;

	private transient String descriptionCached= null;
	private transient boolean cachedDescTried = false;

	static
	{
		try
		{
			//noinspection ClassReferencesSubclass
			ApolloVertex.KEY_CHANGE_LOG=ClassHelper.getPropertyKey(LogEntry.class, "changeLogs");
			//noinspection ClassReferencesSubclass
			ApolloVertex.KEY_PRINCIPALS=ClassHelper.getPropertyKey(BasePrincipal.class, "principals");
			//noinspection ClassReferencesSubclass
			ApolloVertex.KEY_DESCRIPTIONS=ClassHelper.getPropertyKey(RawString.class, "descriptions");
		}
		catch (Exception ex)
		{
			if (null!=Environment.getInstance().getReportHandler())
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
	}

	private transient volatile Boolean deleted=false;
	private KeyData<String> id=null;
	private KeyData<Long> ordinal=null;
	protected KeyData<String> title=null;
	private KeyData<ApolloVertex.SharedAccessLevels> sharedAccessLevel=null;
	private KeyData<DateTime> modifiedWhen=null;
	private KeyData<DateTime> createdWhen=null;
	private KeyData<String> appVersion=null;
	private KeyData<String> vertexType=null;

	@SuppressWarnings("ClassReferencesSubclass")
	@AtSchemaProperty(name="Change Log", expectedType=LogEntry.class,
		description="Recordings of changes to this vertex.", limit=10)
	private ElementEdges<LogEntry> changeLogs=null;
	@SuppressWarnings("ClassReferencesSubclass")
	@AtSchemaProperty(name="Description", expectedType=RawString.class,
		description="The descriptive text describing the object in the localized language of the client browser (eg. if"+
		            " the browser is localized for /lang/en the article should be in English).", isSingleInstance=true)
	private ElementEdges<RawString> descriptions=null;

	// Constructors
	public ApolloVertex()
	{
		super();
		if(this.fetchId().isNullOrEmpty()){
			this.build();
		}
	}

	public ApolloVertex(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public void touch()
	{
		DateTime current = DateTime.now();
		if(this.fetchCreatedWhen().isNullOrEmpty()){
			this.fetchCreatedWhen().setValue(current);
		}
		this.fetchModifiedWhen().setValue(current);
	}

	// Overrides
	@Override
	public KeyData<String> fetchAppVersion()
	{
		if (null==this.appVersion)
		{
			this.appVersion=new KeyData<>(this, "appVersion", String.class, false, null);
		}
		return this.appVersion;
	}

	@Override
	public KeyData<String> fetchId()
	{
		if (null==this.id)
		{
			this.id=new KeyData<>(this, IDataStore.DATA_STORE_ID, String.class, false, null, new KeyDataHandlerIdCallBack());
		}
		return this.id;
	}

	@Override
	public URI getUri()
	{
		return (this.hasId()) ? URI.create(
			String.format(DataVertex.URI_FORMAT, ClassHelper.getIndexKey(this.getClass()), StringX.replace(this.fetchId().getValue(), "#", ""))) : null;
	}

	@Override
	public KeyData<DateTime> fetchCreatedWhen()
	{
		if (null==this.createdWhen)
		{
			this.createdWhen=new KeyData<>(this, "createdWhen", DateTime.class, false, DateTime.now());
		}
		return this.createdWhen;
	}

	@Override
	public KeyData<DateTime> fetchModifiedWhen()
	{
		if (null==this.modifiedWhen)
		{
			this.modifiedWhen=new KeyData<>(this, "modifiedWhen", DateTime.class, false, DateTime.now());
		}
		return this.modifiedWhen;
	}

	@Override
	public KeyData<Long> fetchOrdinal()
	{
		if(null==this.ordinal){
			this.ordinal = new KeyData<>(this, "ordinal", Long.class, false, 0);
		}
		return this.ordinal;
	}

	public KeyData<String> fetchTitle()
	{
		if (null==this.title)
		{
			this.title=new KeyData<>(this, "title", String.class, true, null);
		}
		return this.title;
	}

	@Override
	public KeyData<String> fetchVertexType()
	{
		if (null==this.vertexType)
		{
			this.vertexType=new KeyData<>(this, "vertexType", String.class, false, null, new KeyDataVertexTypeHandler());
		}
		return this.vertexType;
	}

	@Override
	public String formatProperty(String name, String value)
	{
		return value;
	}

	@Override
	public boolean hasId()
	{
		return this.fetchId().isNotNullOrEmpty();
	}

	@SuppressWarnings({
		"SpellCheckingInspection",
		"MethodWithTooManyParameters"
	})
	@Override
	public boolean enforcePolicy()
	{
		return false;
	}

	@Override
	public JsonData toJson(boolean storing, Collection<? extends DataVertex> items)
	{
		JsonData results=JsonData.createArray();
		if (null!=items)
		{
			for (DataVertex vertex : items)
			{
				JsonData result=vertex.toJson(storing);
				if (null!=result)
				{
					results.put(result);
				}
			}
		}
		return results;
	}

	@Override
	public JsonData toJson(boolean storing, String... languages)
	{
		JsonData result = null;
		if (storing)
		{
			result = this.getVertexData();
			result.remove("vertexLabel");
			result.remove("writable");
			result.remove(ApolloVertex.KEY_URI);
		}
		else{
			try
			{
				result = this.getVertexData().clone();
				result.update("vertexLabel", ClassHelper.getSchema(this.getClass()).name());
				result.update("writable", this.isWritable());
				if(null!=this.getEdge()){
					result.put("_edge", this.getEdge().toJson(false), true);
				}
				if (this.hasId())
				{
					result.update(ApolloVertex.KEY_URI, this.getUri());
				}
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	@SuppressWarnings("OverlyNestedMethod")
	@Override
	public JsonData toIndex()
	{
		return this.toJson(true);
	}

	@Override
	public void buildProperty(String fieldName)
	{
		try
		{
			if (StringX.isBlank(fieldName))
			{
				throw new ApplicationException(
					"The keyName cannot be empty."
				);
			}
			Field field=ClassX.getField(this.getClass(), fieldName);
			if (null!=field)
			{
				if (field.isAnnotationPresent(AtSchemaProperty.class))
				{
					AtSchemaProperty schemaProperty=field.getAnnotation(AtSchemaProperty.class);
					Object attributes=this.build(this, field, schemaProperty);
					if (null!=attributes)
					{
						try
						{
							field.setAccessible(true);
							field.set(this, attributes);
						}
						catch (IllegalAccessException e)
						{
							throw new ApplicationException(e);
						}
					}
					else
					{
						Environment.getInstance().getReportHandler().severe("The property should not be null.");
					}
				}
			}
			else
			{
				Environment.getInstance().getReportHandler().severe("The field should not be null.");
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	@Override
	public ElementAttributes build(DataVertex dataVertex, Field field, AtSchemaProperty schemaProperty)
		throws ApplicationException
	{
		PropertyAttributes config=new PropertyAttributes(dataVertex, field, schemaProperty);
		Class<? extends DataVertex> valueCls=schemaProperty.expectedType();
		//noinspection unchecked
		return this.attributes((Class<? extends ElementAttributes>) field.getType(), config);
	}

	@SuppressWarnings("ClassReferencesSubclass")
	@Override
	public PolicyDecisionPoint getSecurityPolicy(BasePrincipal principal, ISecurityPolicy... policies)
	{
		return new PolicyDecisionPoint(this, principal, policies);
	}

	@Override
	public boolean save()
		throws Exception
	{
		return this.save(this.getClass());
	}

	@Override
	public boolean save(IDataStore dataStore)
		throws Exception
	{
		return this.save(this.getClass(), dataStore);
	}

	@Override
	public boolean save(Class<? extends DataVertex> store)
		throws Exception
	{
		return this.save(store, Environment.getInstance().getDataStore());
	}

	@Override
	public synchronized boolean save(Class<? extends DataVertex> store, IDataStore dataStore) throws Exception
	{
		boolean result = false;

		LogEntry.OperationTypes operationType=this.hasId() ? LogEntry.OperationTypes.update : LogEntry.OperationTypes.create;
		if(Environment.getInstance().getConfig().isDebug()){
			Environment.getInstance().getReportHandler().info(
				"title: %s id %s trying to %s. isDirty: %s hasId %s isDeleting %s isDeleted %s"
				, this.fetchTitle().isNotNullOrEmpty() ? this.fetchTitle().getValue() : "no title", this.fetchId().getValue(),
				operationType.toString(),
				this.isDirty(),
				this.hasId(),
				this.isDeleting(),
				this.isDirty()
			);
		}
		if((this.isDirty() || !this.hasId()) && (!this.isDeleting() || !this.isDeleted()))
		{
			this.beforeUpdate(operationType);
			if (operationType==LogEntry.OperationTypes.create)
			{
				result=dataStore.execute(dataStore.getOperation(store, IOperation.Type.create, this));
			}
			else
			{
				IOperation operation = dataStore.getOperation(store, IOperation.Type.update, this);
				result=dataStore.execute(operation);
			}
			this.afterUpdate(operationType, result);
			this.setDirty(false);
			this.setImmediate(true);
			this.setState(result ? DataVertex.VertexState.loaded : DataVertex.VertexState.suspect);

			//noinspection ClassReferencesSubclass
			if (this.isDeprecatedChanged() && !ClassX.isKindOf(this, Edge.class))
			{
				this.getEdgeHelper().deprecateEdges(this.fetchDeprecated().getValue());
			}
			this.setDeprecatedChanged(false);
		}

		return result;
	}

	@Override
	public boolean delete()
	{
		return this.delete(Environment.getInstance().getDataStore());
	}

	@Override
	public synchronized boolean delete(IDataStore dataStore)
	{
		if(!this.isDeleting() && !this.isDeleted())
		{
			try
			{
				this.setDeleting(true);
				LogEntry.OperationTypes operationType=LogEntry.OperationTypes.delete;
				this.beforeUpdate(operationType);
				this.deleteAllEdges();
				this.deleted=dataStore.execute(dataStore.getOperation(IOperation.Type.delete, this));
				this.afterUpdate(LogEntry.OperationTypes.delete, this.deleted);
			}
			catch (Exception x)
			{
				Environment.getInstance().getReportHandler().severe(x);
			}
			finally
			{
				this.setDeleting(false);
			}
		}
		this.setState(this.deleted ? DataVertex.VertexState.deleted : DataVertex.VertexState.suspect);
		return this.deleted;
	}

	@SuppressWarnings("ClassReferencesSubclass")
	@Override
	public boolean deleteAllEdges()
	{
		boolean result=false;
		try
		{
			Set<Class<? extends Edge>> subTypesOf=Environment.getInstance().getReflections().getSubTypesOf(Edge.class);
			subTypesOf.add(Edge.class);

			for (Class<? extends Edge> subType : subTypesOf)
			{
				try
				{
					result=this.getEdgeHelper().removeAllEdges(subType, this.isImmediate(), 0);
				}
				catch (Exception ex){
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}

		return result;
	}

	@Override
	public <T extends DataVertex> T move(Class<? extends DataVertex> toClass)
	{
		T result=null;
		if (null!=toClass)
		{
			try
			{
				Constructor constructor=toClass.getConstructor(JsonData.class, Object.class);
				JsonData data=this.getVertexData();
				data.remove(IDataStore.DATA_STORE_ID);
				Environment.getInstance().getDataStore().removeVertexId(data);
				//noinspection unchecked
				result=(T) constructor.newInstance(data, this.getIndexId());
				result.save();
				@SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
				MergeVertices merger=new MergeVertices(this, result, 1, 0, true);
				merger.start();
				this.delete();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}

		}
		return result;
	}

	public ElementEdges<RawString> getDescriptions()
	{
		if (null==this.descriptions)
		{
			this.buildProperty("descriptions");
		}
		return this.descriptions;
	}

	@Override
	public <T extends DataVertex> T mergeTo(DataVertex vertex, boolean delete)
	{
		@SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
		MergeVertices merger=new MergeVertices(this, vertex, 1, 0, delete);
		merger.start();
		return merger.getMerged();
	}

	@Override
	public void afterUpdate(LogEntry.OperationTypes operationType, boolean success)
	{
		try
		{
			if (null!=Environment.getInstance().getCache())
			{
				//noinspection SwitchStatementWithoutDefaultBranch,EnumSwitchStatementWhichMissesCases
				switch (operationType)
				{
					case update:
					case create:
						Environment.getInstance().getCache().put(this.getClass(), this.getClass(), this.fetchId().getValue(), this);
						break;
					case delete:
						break;
				}
			}

			//noinspection ClassReferencesSubclass
			if((null!=this.getCredentials()) && !Objects.equals(this.getCredentials(), SystemCredentials.getInstance())
			   && !ClassX.isKindOf(this, LogEntry.class)
			   && !ClassX.isKindOf(this, Edge.class))
			{
				String comment = null;
				if(null!=this.getBeforeData()){
					List<String> exclusions = new ArrayList<>();
					exclusions.add("createdWhen");
					exclusions.add("modifiedWhen");
					exclusions.add("lid");
					exclusions.add("/vertex/uri");
					exclusions.add("vertexLabel");
					exclusions.add("writable");
					JsonData dif = this.getBeforeData().changes(this.toJson(false), exclusions);
					if((null!=dif) && !dif.isEmpty()){
						dif.put("_action", operationType, true);
						dif.put("_class", this.getClass().getSimpleName(), true);
						dif.put(ApolloVertex.KEY_URI, this.getUri());
						comment = dif.toString();
					}
				}
				UserActivity.logActivity(this, operationType, comment, success);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		this.setBeforeData(null);
	}

	@Override
	public void beforeUpdate(LogEntry.OperationTypes operationType)
	{
		this.touch();
		try
		{
			if (null!=Environment.getInstance().getCache())
			{
				//noinspection EnumSwitchStatementWhichMissesCases,SwitchStatementWithTooFewBranches,SwitchStatementWithoutDefaultBranch
				switch (operationType)
				{
					case update:
						if ((null!=this.getCredentials()) && !Objects.equals(this.getCredentials(), SystemCredentials.getInstance()))
						{
							ApolloVertex vertex = Environment.getInstance().getDataStore().getObjectById(this.getClass(), this.getClass(), this.fetchId().getValue(), true);
							this.setBeforeData(vertex.getVertexData().clone());
						}
						break;
					case delete:
						Environment.getInstance().getCache().remove(this.getClass(), this.getClass(), this);
						break;
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	@SuppressWarnings("ClassReferencesSubclass")
	@Override
	public void beforeEdgeUpdate(LogEntry.OperationTypes operationType, DataVertex other, Edge edge)
	{
		try
		{
			if (null!=Environment.getInstance().getCache())
			{
				//noinspection EnumSwitchStatementWhichMissesCases,SwitchStatementWithTooFewBranches
				switch (operationType)
				{
					case delete:
						this.getEdgeHelper().cacheRemoveEdge(edge.getClass(), other, edge.fetchLabel().getValue(), Common.Direction.OUT);
						break;
					default:
						break;
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	@SuppressWarnings("ClassReferencesSubclass")
	@Override
	public void afterEdgeUpdate(LogEntry.OperationTypes operationType, DataVertex other, Edge edge, boolean success)
	{
		try
		{
			if (null!=Environment.getInstance().getCache())
			{
				//noinspection SwitchStatementWithoutDefaultBranch,EnumSwitchStatementWhichMissesCases
				switch (operationType)
				{
					case update:
					case create:
						this.getEdgeHelper().cachePutEdge(edge);
						break;
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		this.setBeforeData(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DataVertex> ElementAttributes<T> attributes(Class<? extends ElementAttributes> cls, PropertyAttributes propertyAttributes)
	{
		ElementAttributes<T> attributes=null;

		try
		{
			Constructor constructor=cls.getConstructor(PropertyAttributes.class);
			attributes=(ElementAttributes<T>) constructor.newInstance(propertyAttributes);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return attributes;
	}

	@Override
	public DiscoveryItem getDiscoveryItem(String phrase, UserCredentials userCredentials, String key, Object value, boolean suggest)
	{
		DiscoveryItem result;
		if(suggest){
			result =SuggestItem.getSuggestion(this, userCredentials, phrase);
		}
		else{
			result = new GenericItem(phrase, this, SystemCredentials.getInstance(), key, value, 0);
		}
		return result;
	}

	@Override
	public synchronized boolean isDeleting()
	{
		return this.deleting;
	}

	@Override
	public synchronized void setDeleting(boolean deleting)
	{
		this.deleting = deleting;
	}

	@Override
	public boolean isDeleted()
	{
		return this.deleted;
	}

	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted=deleted;
	}

	@Override
	public Duration getAge()
	{
		DateTime now=DateTime.now(DateTimeZone.UTC);
		return new Duration(this.fetchModifiedWhen().getValue(), now);
	}

	@Override
	public boolean isWritable()
	{
		return ClassHelper.getSchema(this.getClass()).writable();
	}

	@Override
	public List<IQueryResultHandler> getQueryResultHandlers()
	{
		return new ArrayList<>();
	}

	@Override
	public Class<? extends DataVertex> getActualClass()
	{
		return Environment.getInstance().getApolloVertexType(this.getVertexData().getString("vertexType"));
	}

	@Override
	public ExcelSchema getExcelSchema(int index)
	{
		return PropertyHelper.getExcelSchema(this);
	}

	// Methods
	public static String getKeyChangeLog()
	{
		return ApolloVertex.KEY_CHANGE_LOG;
	}

	public static String getKeyDescriptions()
	{
		return ApolloVertex.KEY_DESCRIPTIONS;
	}

	public static String getKeyPrincipals()
	{
		return ApolloVertex.KEY_PRINCIPALS;
	}

	public static <T extends ApolloVertex> List<T> sortTitle(Collection<? extends ApolloVertex> vertices, final boolean ascending)
	{
		List<T> results=new ArrayList<>();

		if (null!=vertices)
		{
			for (ApolloVertex vertex : vertices)
			{
				//noinspection unchecked
				results.add((T) vertex);
			}

			//noinspection Java8ListSort
			Collections.sort(results, new Comparator<T>()
			{
				// Overrides
				@Override
				public int compare(T o1, T o2)
				{
					int result=o1.compareTo(o2);
					if (!ascending)
					{
						result*=-1;
					}
					return result;
				}
			});
		}

		return results;
	}

	@Override
	public int compareTo(DataVertex o)
	{
		int result=0;
		try
		{
			if (ClassX.isKindOf(o.getClass(), ApolloVertex.class))
			{
				ApolloVertex vertex=(ApolloVertex) o;
				String thisStr=this.fetchTitle().getValue();
				String thatStr=vertex.fetchTitle().getValue();
				String oThis=StringX.isBlank(thisStr) ? this.fetchId().getValue() : thisStr;
				String oThat=StringX.isBlank(thatStr) ? vertex.fetchId().getValue() : thatStr;
				result=StringX.compare(oThis, oThat);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	public Long getMillis(DateTime value)
	{
		return (null!=value) ? value.getMillis() : null;
	}

	private boolean isExcluded(String key, String... excludeKeys)
	{
		boolean result=false;
		for (String exclude : excludeKeys)
		{
			result=StringX.equalsIgnoreCase(key, exclude);
			if (result)
			{
				break;
			}
		}
		return result;
	}

	// Getters and setters
	public KeyData<ApolloVertex.SharedAccessLevels> getSharedAccessLevel()
	{
		if (null==this.sharedAccessLevel)
		{
			this.sharedAccessLevel=new KeyData<>(this, "sharedAccessLevel", ApolloVertex.SharedAccessLevels.class, true, null);
		}
		return this.sharedAccessLevel;
	}

	public ElementEdges<LogEntry> getChangeLogs()
	{
		if (null==this.changeLogs)
		{
			this.buildProperty("changeLogs");
		}
		return this.changeLogs;
	}

	public String getDescription()
	{
		this.descriptionCached = null;
		try{
			@SuppressWarnings("ClassReferencesSubclass")
			RawString desc = this.getDescriptions().get();
			if(null!=desc){
				this.descriptionCached = desc.fetchValue().getValue();
			}
		}
		catch (Exception ignored) { }

		return this.descriptionCached;
	}

	public String getDescriptionCached(){
		String result = this.descriptionCached;
		if(!this.cachedDescTried){
			result = this.getDescription();
			if(null!=result)
			{
				this.cachedDescTried=true;
			}
		}
		return result;
	}

	@Override
	public boolean isUnusable()
	{
		return this.isDeleted() || this.isDeleting() || this.fetchDeprecated().getValue();
	}
}
