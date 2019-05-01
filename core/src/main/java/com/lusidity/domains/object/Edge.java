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

package com.lusidity.domains.object;

import com.lusidity.Environment;
import com.lusidity.Initializer;
import com.lusidity.collections.IEdgeHandler;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.IKeyDataHandler;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import org.joda.time.DateTime;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("BooleanParameter")
/*
 * Edge that have a related EdgeData class must overrided and return an getEdgeDataType.  If this is not done the basic add to ElementEdge will fail.
 * The EdgeData class almost must have a public empty param constructor for use with reflections.
 */
@AtSchemaClass(name="Edge", discoverable=false)
public class Edge extends ApolloVertex implements Initializer
{

	private transient EdgeData edgeData=null;
	private KeyData<Class<? extends DataVertex>> type=null;
	private KeyData<Endpoint> endpointFrom=null;
	private KeyData<Endpoint> endpointTo=null;
	private KeyData<String> label=null;

	// Constructors
	public Edge()
	{
		super();
	}

	public Edge(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public Edge(EdgeData edgeData, Endpoint endpointFrom, Endpoint endpointTo)
	{
		super();
		this.fetchEndpointFrom().setValue(endpointFrom);
		this.fetchEndpointTo().setValue(endpointTo);
		this.fetchLabel().setValue(edgeData.getKey());
		this.edgeData=edgeData;
	}

	public Edge(Class<? extends DataVertex> type, Endpoint fromEndpoint, Endpoint toEndpoint, String label)
	{
		super();
		this.type=new KeyData<>(this, "type", type, false, null);
		this.fetchLabel().setValue(label);
		this.fetchEndpointFrom().setValue(fromEndpoint);
		this.fetchEndpointTo().setValue(toEndpoint);
	}

	public KeyData<String> fetchLabel()
	{
		if (null==this.label)
		{
			this.label=new KeyData<>(this, "label", String.class, false, null);
		}
		return this.label;
	}

	public KeyData<Endpoint> fetchEndpointFrom()
	{
		if (null==this.endpointFrom)
		{
			this.endpointFrom=new KeyData<>(this, "endpointFrom", Endpoint.class, false, null);
		}
		return this.endpointFrom;
	}

	public KeyData<Endpoint> fetchEndpointFrom(IKeyDataHandler... handlers)
	{
		if (null==this.endpointFrom)
		{
			this.endpointFrom=new KeyData<>(this, "endpointFrom", Endpoint.class, false, null, handlers);
		}
		return this.endpointFrom;
	}

	public KeyData<Endpoint> fetchEndpointTo()
	{
		if (null==this.endpointTo)
		{
			this.endpointTo=new KeyData<>(this, "endpointTo", Endpoint.class, false, null);
		}
		return this.endpointTo;
	}

	public KeyData<Endpoint> fetchEndpointTo(IKeyDataHandler... handlers)
	{
		if (null==this.endpointTo)
		{
			this.endpointTo=new KeyData<>(this, "endpointTo", Endpoint.class, false, null, handlers);
		}
		return this.endpointTo;
	}

	public KeyData<Class<? extends DataVertex>> fetchType()
	{
		if (null==this.type)
		{
			this.type=new KeyData<>(this, "type", null, false, null);
		}
		return this.type;
	}

	// Overrides
	@Override
	public boolean save(Class<? extends DataVertex> store)
		throws Exception
	{
		if(this.fetchEndpointFrom().isNullOrEmpty())
		{
			throw new ApplicationException("The from endpoint cannot be null.");
		}
		if (this.fetchEndpointTo().isNullOrEmpty())
		{
			throw new ApplicationException("The to endpoint cannot be null.");
		}
		// todo: this step4 could be slowing us down because of retrieving the vertices during an update, during a create is ok.
		DataVertex from= this.fetchEndpointFrom().getValue().getVertex();
		if (null==from)
		{
			throw new ApplicationException("The from endpoint cannot have a null vertex.");
		}
		DataVertex to= this.fetchEndpointTo().getValue().getVertex();
		if (null==to)
		{
			throw new ApplicationException("The to endpoint cannot have a null vertex.");
		}

		if((null==this.getCredentials()) && (from.getCredentials()!=null)){
			this.setCredentials(from.getCredentials());
		}
		else if((null==this.getCredentials()) && (to.getCredentials()!=null)){
			this.setCredentials(to.getCredentials());
		}

		LogEntry.OperationTypes changeType=this.hasId() ? LogEntry.OperationTypes.update : LogEntry.OperationTypes.create;
		from.beforeEdgeUpdate(changeType, to, this);
		boolean result=super.save(store);
		from.afterEdgeUpdate(changeType, to, this, result);

		return result;
	}

	@Override
	public boolean delete()
	{
		DataVertex from=(this.fetchEndpointFrom().isNotNullOrEmpty()) ? this.fetchEndpointFrom().getVertex() : null;
		DataVertex to=(this.fetchEndpointTo().isNotNullOrEmpty()) ? this.fetchEndpointTo().getVertex() : null;

		LogEntry.OperationTypes changeType=LogEntry.OperationTypes.delete;

		if ((null!=from) && (null!=to))
		{
			from.beforeEdgeUpdate(changeType, to, this);
		}

		boolean result=super.delete();

		if ((null!=from) && (null!=to))
		{
			from.afterEdgeUpdate(changeType, to, this, result);
		}

		return result;
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
						if (success)
						{
							Environment.getInstance().getCache().put(this.getClass(),
								this.fetchEndpointFrom().getVertex().getClass(), this.fetchId().getValue(), this
							);
						}
						break;
					case delete:
						break;
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		super.afterUpdate(operationType, success);
	}

	@Override
	public void beforeUpdate(LogEntry.OperationTypes operationType)
	{
		this.touch();
		try
		{
			if (null!=Environment.getInstance().getCache())
			{
				//noinspection SwitchStatementWithTooFewBranches,SwitchStatementWithoutDefaultBranch,EnumSwitchStatementWhichMissesCases
				switch (operationType)
				{
					case delete:
						Environment.getInstance().getCache().remove(this.getClass(),
							this.fetchEndpointFrom().getVertex().getClass(), this
						);
						break;
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	// Methods
	public static <T extends Edge> T create(EdgeData edgeData, Endpoint fromEndpoint, Endpoint toEndpoint)
	{
		T result=null;
		try
		{
			Constructor<? extends Edge>
				constructor=edgeData.getEdgeType().getConstructor(EdgeData.class, Endpoint.class, Endpoint.class);
			result=(T) constructor.newInstance(edgeData, fromEndpoint, toEndpoint);
			result.setCredentials(edgeData.getCredentials());
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	public static <T extends Edge> T create(Class<? extends Edge> edgeType,
	                                        Class<? extends DataVertex> type,
	                                        Endpoint from, Endpoint to, String label)
	{
		T result=null;
		try
		{
			Constructor<? extends Edge>
				constructor=edgeType.getConstructor(Class.class, Endpoint.class, Endpoint.class, String.class);
			result=(T) constructor.newInstance(type, from, to, label);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	public void nullifyEndpoints()
	{
		// this.getFromEndpoint().nullifyVertex();
		// this.getToEndpoint().nullifyVertex();
	}

	public Endpoint getOther(String id)
	{
		return Objects.equals(this.fetchEndpointFrom().getValue().fetchRelatedId().getValue(), id) ?
			this.fetchEndpointTo().getValue() : this.fetchEndpointFrom().getValue();
	}

	public Endpoint getEndpoint(String id)
	{
		return Objects.equals(this.fetchEndpointFrom().getValue().fetchRelatedId().getValue(), id) ?
			this.fetchEndpointFrom().getValue() : this.fetchEndpointTo().getValue();
	}

	public boolean isFrom(Object id)
	{
		return this.fetchEndpointFrom().getValue().fetchRelatedId().getValue().equals(id);
	}

	@Override
	public void initialize()
		throws Exception
	{
		try
		{
			if (Environment.getInstance().getConfig().isInitializeDomains())
			{
				IDataStore dataStore = Environment.getInstance().getDataStore();
				// Create new Indices on startup.
				int count=dataStore.count(this.getClass());
				if (count==0)
				{
					Constructor constructor=this.getClass().getConstructor();
					Edge test=(Edge) constructor.newInstance();

					ApolloVertex v1 = new ApolloVertex();
					v1.fetchId().setValue(UUID.randomUUID().toString());

					ApolloVertex v2 = new ApolloVertex();
					v2.fetchId().setValue(UUID.randomUUID().toString());

					Endpoint epFrom = new Endpoint(v1, "text", 0);
					Endpoint epTo = new Endpoint(v2, "text", 0);

					test.fetchEndpointFrom().setValue(epFrom);
					test.fetchEndpointTo().setValue(epTo);

					String label=String.format("init_test_%d", DateTime.now().getMillis());
					test.fetchLabel().setValue(label);

					boolean saved=dataStore.execute(dataStore.getOperation(test.getClass(), IOperation.Type.create, test));

					if (!saved)
					{
						throw new ApplicationException("%s index is not properly configured.", this.getClass());
					}
					else
					{
						test.delete();
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return 0;
	}

	// Getters and setters
	public IEdgeHandler getHandler()
	{
		return null;
	}


	public EdgeData getEdgeData()
	{
		return this.edgeData;
	}

	public Class<? extends EdgeData> getEdgeDataType()
	{
		return EdgeData.class;
	}
}
