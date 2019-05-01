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
import com.lusidity.collections.PropertyAttributes;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.data.handler.KeyDataHandlerDeprecated;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.helper.EdgeHelper;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.office.ExcelSchema;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.joda.time.DateTime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings({
	"EqualsAndHashcode",
	"AbstractClassNamingConvention"
	,
	"OverlyComplexClass"
	,
	"OverlyCoupledClass"
})
@AtSchemaClass(name="Data Vertex", discoverable=false)
public
abstract class DataVertex extends BaseVertex
{
	public enum VertexState
	{
		aged,
		created,
		deleted,
		loaded,
		none,
		suspect
	}

	// Fields
	public static final String URI_FORMAT="/domains/%s/%s";
	public static final String VERTEX_URI="/vertex/uri";
	protected transient UserCredentials credentials=null;
	private transient DataVertex.VertexState state=DataVertex.VertexState.none;
	private transient volatile JsonData vertexData=JsonData.createObject();
	private transient volatile JsonData deltas=JsonData.createObject();
	private transient volatile JsonData beforeData=null;
	private transient volatile Object indexId=null;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private transient boolean dirty=false;
	private transient boolean immediate=true;
	private transient boolean deprecatedChanged=false;
	private KeyData<Boolean> deprecated=null;
	/**
	 * // The edge between this vertex and another.
	 */
	@SuppressWarnings("ClassReferencesSubclass")
	private Edge edge=null;

	// Constructors
	public DataVertex(JsonData dso, Object indexId)
	{
		super();
		this.vertexData=dso;
		this.indexId=indexId;
		this.state=DataVertex.VertexState.loaded;
	}

	public DataVertex()
	{
		super();
		this.state=DataVertex.VertexState.created;
	}

	// Overrides
	@Override
	public int hashCode()
	{
		final int prime=31;
		int result=1;
		result=(prime*result)
		       +((this.fetchId().getValue()==null) ? 0 : this.fetchId().getValue().hashCode());
		return result;
	}

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object o)
	{
		boolean result=false;
		if ((null!=o) && ClassX.isKindOf(o, DataVertex.class))
		{
			DataVertex that=(DataVertex) o;
			if (this.fetchId().isNotNullOrEmpty() && that.fetchId().isNotNullOrEmpty())
			{
				String thisId=this.fetchId().getValue();
				String thatId=that.fetchId().getValue();
				result=thisId.equals(thatId);
			}
		}
		return result;
	}

	// Methods
	public static void warmUp()
	{
		if (Environment.getInstance().getConfig().isWarmUpEnabled()
		    && Environment.getInstance().getCache().isOpened())
		{
			for (Map.Entry<Class<? extends DataVertex>, Long> entry : Environment.getInstance().getCache().getCacheableObjects().entrySet())
			{
				DataVertex.handle(entry.getKey());
			}
		}
	}

	static void handle(Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends DataVertex> cls=(Class<? extends DataVertex>) o;
			if (null!=cls)
			{
				int limit=1000;
				BaseQueryBuilder queryBuilder=
					Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, 0, limit);

				queryBuilder.matchAll();
				int hits=Environment.getInstance().getIndexStore().getQueryFactory().count(queryBuilder);
				if (hits>0)
				{
					Stopwatch stopwatch=new Stopwatch();
					stopwatch.start();
					Environment.getInstance().getReportHandler().say("Warming up %d %s.", hits, cls.getSimpleName());
					int total=0;
					for (int i=0; i<hits; i+=limit)
					{
						queryBuilder.setStart(i);
						queryBuilder.setLimit(limit);
						QueryResults queryResults=queryBuilder.execute();
						if (queryResults.isEmpty())
						{
							break;
						}

						for (IQueryResult result : queryResults)
						{
							DataVertex vertex=result.getVertex();
							Environment.getInstance().getCache().put(vertex);
						}
						total+=queryResults.size();
						Environment.getInstance().getReportHandler()
						           .say("%d/%d %s loaded, %s", total, hits, cls.getSimpleName(),
							           stopwatch.toString()
						           );

					}
					stopwatch.stop();
					Environment.getInstance().getReportHandler()
					           .say("%s warmed up, took %s.", cls.getSimpleName(),
						           stopwatch.elapsed().toString()
					           );
					Environment.getInstance().getReportHandler().say("The data store cache has %d items.",
						Environment.getInstance().getCache().getTotal()
					);
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	public KeyData<Boolean> fetchDeprecated()
	{
		if (null==this.deprecated)
		{
			this.deprecated=new KeyData<>(this, "deprecated", Boolean.class, false, false, new KeyDataHandlerDeprecated());
		}
		return this.deprecated;
	}

	/**
	 * Make a copy of this object. only fields and ElementAtributes will be copied.
	 *
	 * @param cls Must either be the same as this class or a subclass.
	 * @return A clone of this object.
	 */
	@SuppressWarnings({
		"ThrowCaughtLocally",
		"unused"
	})
	public <T extends DataVertex> T makeClone(Class<? extends DataVertex> cls)
	{
		T result=null;

		try
		{
			if (!ClassX.isKindOf(cls, this.getClass()))
			{
				throw new ApplicationException("%s is not the same as or is not a subclass of, %s.", cls.getName(), this.getClass().getName());
			}

			Collection<Field> fields1=PropertyHelper.getAllFields(this.getClass());
			Collection<Field> fields2=PropertyHelper.getAllFields(cls);

			Constructor constructor=cls.getConstructor();
			//noinspection unchecked
			result=(T) constructor.newInstance();

			for (Field actual : fields1)
			{
				actual.setAccessible(true);

				if (!Modifier.isAbstract(actual.getModifiers()) && !Modifier.isInterface(actual.getModifiers()))
				{
					try
					{
						Field expected=cls.getField(actual.getName());
						if ((null!=expected) && expected.getType().equals(actual.getType()))
						{
							Object o=actual.get(this);
							Environment.getInstance().getReportHandler().notImplemented();
							// TODO: finish this.
						}
					}
					catch (Exception ignored)
					{
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return result;
	}

	public abstract ExcelSchema getExcelSchema(int index);

	public void build()
	{
		try
		{
			if (null==this.getVertexData())
			{
				this.setVertexData(JsonData.createObject());
			}

			Map<String, Method> methods=PropertyHelper.getAllMethods(this.getClass());
			Collection<String> processed=new ArrayList<>();
			for (Map.Entry<String, Method> entry : methods.entrySet())
			{
				Method method=entry.getValue();
				if (ClassX.isKindOf(method.getReturnType(), KeyData.class) || ClassX.isKindOf(method.getReturnType(), KeyDataCollection.class))
				{
					try
					{
						String name=method.getName();
						if ((method.getGenericParameterTypes().length==0) && !processed.contains(name))
						{
							processed.add(name);
							method.setAccessible(true);
							method.invoke(this);
						}
					}
					catch (Exception ex)
					{
						if (this.getState()!=DataVertex.VertexState.created)
						{
							Environment.getInstance().getReportHandler().warning(ex);
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		this.setDirty(false);
	}

	public synchronized JsonData getVertexData()
	{
		if (null==this.vertexData)
		{
			this.vertexData=JsonData.createObject();
		}
		return this.vertexData;
	}

	public DataVertex.VertexState getState()
	{
		return this.state;
	}

	public void setState(DataVertex.VertexState state)
	{
		this.state=state;
	}

	public void setVertexData(JsonData vertexData)
	{
		this.vertexData=vertexData;
	}

	public boolean isAuthorized(UserCredentials userCredentials, Permission.Types permission)
	{
		this.setCredentials(userCredentials);
		boolean result=!ScopedConfiguration.getInstance().isEnabled() || this.isInScope(this);
		if (result)
		{
			switch (permission)
			{
				case scope:
				case read:
					if (!ScopedConfiguration.getInstance().isEnabled())
					{
						result=this.getSecurityPolicy(userCredentials.getPrincipal()).canRead();
					}
					break;
				case write:
					result=this.getSecurityPolicy(userCredentials.getPrincipal()).canWrite();
					break;
				case delete:
					result=this.getSecurityPolicy(userCredentials.getPrincipal()).canDelete();
					break;
				case denied:
					result=this.getSecurityPolicy(userCredentials.getPrincipal()).isDenied();
					break;
				default:
					result=false;
					break;
			}
		}
		return result;
	}

	protected boolean isInScope(DataVertex vertex)
	{
		boolean result=true;
		if (ScopedConfiguration.getInstance().isEnabled())
		{
			if (vertex.enforcePolicy() && !PolicyDecisionPoint.isInScope(vertex, this.getCredentials()))
			{
				result=false;
			}
		}
		return result;
	}

	/**
	 * @param data The JsonData that contains key value pairs.
	 *             The key is the filed name and the value is a collection of JSON objects.
	 */
	protected void transformCachedEdges(JsonData data)
	{
		JsonData edges=data.getFromPath("element_edges");
		if ((null!=edges) && edges.isJSONObject())
		{
			Class cls=this.getClass();
			Collection<String> keys=edges.keys();
			for (String key : keys)
			{
				try
				{
					JsonData jd=edges.getFromPath(key);
					Field field=cls.getField(key);
					if ((null!=field) && jd.isJSONArray())
					{
						AtSchemaProperty sp=field.getAnnotation(AtSchemaProperty.class);
						if (null!=sp)
						{
							ElementAttributes attributes=this.getElementAttribute(field, sp);
							field.set(this, attributes);
						}
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}
		}
	}

	/**
	 * @param field          The field that extends ElementAttributes.
	 * @param schemaProperty The AtSchemaProperty associated with the field.
	 * @return An ElementAttributes.
	 * @throws ApplicationException throws ApplicationException
	 */
	public ElementAttributes getElementAttribute(Field field, AtSchemaProperty schemaProperty)
		throws ApplicationException
	{
		PropertyAttributes config=new PropertyAttributes(this, field, schemaProperty);
		config.setPreload(false);
		//noinspection unchecked
		return this.attributes((Class<? extends ElementAttributes>) field.getType(), config);
	}

	// Getters and setters
	public ExcelSchema getExcelSchema()
	{
		return PropertyHelper.getExcelSchema(this);
	}

	public EdgeHelper getEdgeHelper()
	{
		return new EdgeHelper(this);
	}

	/**
	 * Should this vertex be immediately available for search.
	 *
	 * @return true or false, default true
	 */
	public boolean isImmediate()
	{
		return this.immediate;
	}

	/**
	 * Set the value of whether or not this vertex should be immediately available for search.
	 * If set to false set back to true after done as this object may reside in cache and other processes may need it to be available immediately.
	 *
	 * @param immediate true or false
	 */
	public void setImmediate(boolean immediate)
	{
		this.immediate=immediate;
	}

	@SuppressWarnings("unused")
	public boolean isDirty()
	{
		return this.dirty;
	}

	public synchronized void setDirty(boolean dirty)
	{
		this.dirty=dirty;
	}

	public Object getIndexId()
	{
		return this.indexId;
	}

	public synchronized void setIndexId(Object indexId)
	{
		this.indexId=indexId;
	}

	/**
	 * The current principal working with this object.
	 * Only works if caching for this object is not enabled.
	 *
	 * @return The current principal object.
	 */
	@SuppressWarnings("ClassReferencesSubclass")
	public BasePrincipal getPrincipal()
	{
		return ((null!=this.getCredentials()) && (null!=this.getCredentials().getPrincipal())) ?
			this.getCredentials().getPrincipal() : null;
	}

	public UserCredentials getCredentials()
	{
		return this.credentials;
	}

	public void setCredentials(UserCredentials credentials)
	{
		this.credentials=credentials;
	}

	/**
	 * @return Returns at a minimum the modifiedWhen property as of the time this method is called.
	 */
	public synchronized JsonData getDeltas()
	{
		if (null==this.deltas)
		{
			this.deltas=JsonData.createObject();
			this.setDirty(true);
		}
		this.deltas.update("modifiedWhen", DateTime.now());
		return this.deltas;
	}

	protected JsonData getBeforeData()
	{
		return this.beforeData;
	}

	protected void setBeforeData(JsonData beforeData)
	{
		this.beforeData=beforeData;
	}

	public boolean isDeprecatedChanged()
	{
		return this.deprecatedChanged;
	}

	public void setDeprecatedChanged(boolean deprecatedChanged)
	{
		this.deprecatedChanged=deprecatedChanged;
	}

	/**
	 * // The edge between this vertex and another.
	 */
	@SuppressWarnings("ClassReferencesSubclass")
	public Edge getEdge()
	{
		return this.edge;
	}

	/**
	 * @param edge The edge between this vertex and another.
	 */
	@SuppressWarnings("ClassReferencesSubclass")
	public void setEdge(Edge edge)
	{
		this.edge=edge;
	}
}
