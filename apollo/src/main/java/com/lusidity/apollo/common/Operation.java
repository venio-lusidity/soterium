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

package com.lusidity.apollo.common;

import com.lusidity.data.DataVertex;
import com.lusidity.data.bulk.BulkItems;
import com.lusidity.data.interfaces.data.IOperation;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;

public class Operation implements IOperation
{
	private boolean successful=true;
	private Class<? extends DataVertex> partitionType=null;
	private DataVertex vertex=null;
	private Class<? extends DataVertex> storeType=null;
	private IOperation.Type type=IOperation.Type.none;
	private Object result=null;
	private int retries=0;
	private BulkItems<? extends DataVertex> bulkItems=null;

// Constructors
	public Operation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, IOperation.Type type, BulkItems<? extends DataVertex> bulkItems)
	{
		super();
		this.storeType=storeType;
		this.partitionType=partitionType;
		this.type=type;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.bulkItems=bulkItems;
	}

	public Operation(Class<? extends DataVertex> storeType, IOperation.Type type)
	{
		super();
		this.storeType=storeType;
		this.type=type;
	}

	public Operation(IOperation.Type type, DataVertex vertex)
	{
		super();
		this.storeType=vertex.getClass();
		this.partitionType=vertex.getClass();
		this.type=type;
		this.vertex=vertex;
		this.load();
	}

	private void load()
	{
		if ((null!=this.vertex) && ClassX.isKindOf(this.partitionType, Edge.class))
		{
			Edge edge=(Edge) this.vertex;
			Class cls=edge.fetchEndpointFrom().getValue().getRelatedClass();
			if (ClassX.isKindOf(cls, DataVertex.class))
			{
				//noinspection unchecked
				this.partitionType=(Class<? extends DataVertex>) cls;
			}
		}
	}

	public Operation(Class<? extends DataVertex> storeType, IOperation.Type type, DataVertex vertex)
	{
		super();
		this.storeType=storeType;
		this.partitionType=storeType;
		this.type=type;
		this.vertex=vertex;
		this.load();
	}

	public Operation(Class<? extends DataVertex> storeType, Class<? extends DataVertex> partitionType, IOperation.Type type, DataVertex vertex)
	{
		super();
		this.partitionType=partitionType;
		this.type=type;
		this.storeType=storeType;
		this.vertex=vertex;
		this.load();
	}

	private Operation()
	{
		super();
	}

// Overrides
	@Override
	public synchronized void incrementRetries()
	{
		this.retries++;
	}

	@Override
	public IOperation.Type getType()
	{
		return this.type;
	}

	@Override
	public Class<? extends DataVertex> getStoreType()
	{
		return this.storeType;
	}

	@Override
	public synchronized Class<? extends DataVertex> getPartitionType()
	{
		return this.partitionType;
	}

	@Override
	public Object getResult()
	{
		return this.result;
	}

	@Override
	public DataVertex getVertex()
	{
		return this.vertex;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if (obj instanceof Operation)
		{
			Operation other=(Operation) obj;
			result=StringX.equalsIgnoreCase(this.getName(), other.getName());
		}
		return result;
	}	@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
	@Override
	public void setBulkItems(BulkItems bulkItems)
	{
		this.bulkItems=bulkItems;
	}

// Methods
	public static Operation createEmpty()
	{
		return new Operation();
	}	@Override
	public BulkItems<? extends DataVertex> getBulkItems()
	{
		return this.bulkItems;
	}


	@Override
	public synchronized String getName()
	{
		return this.getPartitionType().getName();
	}

	@Override
	public synchronized boolean isSuccessful()
	{
		return this.successful;
	}

	@Override
	public void setSuccessful(boolean successful)
	{
		this.successful=successful;
	}

	@Override
	public synchronized String getDataStoreId()
	{
		return this.getVertex().fetchId().getValue();
	}

	@Override
	public int getRetries()
	{
		return this.retries;
	}




}

