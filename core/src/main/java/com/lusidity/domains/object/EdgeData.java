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

import com.lusidity.data.DataVertex;
import com.lusidity.data.bulk.BulkItems;
import com.lusidity.framework.data.Common;
import com.lusidity.system.security.UserCredentials;

public class EdgeData
{
	private BulkItems<DataVertex> bulkItems = null;
	private String fromLabel=null;
	private String toLabel=null;
	private Edge edge=null;
	private long fromOrdinal=0L;
	private long toOrdinal=0L;
	private String key=null;
	private Common.Direction direction=null;
	private Class<? extends Edge> edgeType=null;
	private UserCredentials credentials=null;
	private boolean immediate = true;

	// Constructors
	public EdgeData()
	{
		super();
	}

	public EdgeData(BulkItems<DataVertex> bulkItems, String fromLabel, long fromOrdinal, String toLabel, long toOrdinal)
	{
		super();
		this.fromLabel=fromLabel;
		this.fromOrdinal=fromOrdinal;
		this.toLabel=toLabel;
		this.toOrdinal=toOrdinal;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.bulkItems = bulkItems;
	}

	public EdgeData(String fromLabel, long fromOrdinal, String toLabel, long toOrdinal)
	{
		super();
		this.fromLabel=fromLabel;
		this.fromOrdinal=fromOrdinal;
		this.toLabel=toLabel;
		this.toOrdinal=toOrdinal;
	}

	public EdgeData(String fromLabel, String toLabel)
	{
		super();
		this.fromLabel=fromLabel;
		this.toLabel=toLabel;
	}

	public EdgeData(long fromOrdinal, long toOrdinal)
	{
		super();
		this.fromOrdinal=fromOrdinal;
		this.toOrdinal=toOrdinal;
	} 	
// Getters and setters
	public String getFromLabel()
	{
		return this.fromLabel;
	}

	public void setFromLabel(String fromLabel)
	{
		this.fromLabel=fromLabel;
	}

	public String getToLabel()
	{
		return this.toLabel;
	}

	public void setToLabel(String toLabel)
	{
		this.toLabel=toLabel;
	}

	public Edge getEdge()
	{
		return this.edge;
	}

	public void setEdge(Edge edge)
	{
		this.edge=edge;
	}

	public UserCredentials getCredentials()
	{
		return this.credentials;
	}

	public void setCredentials(UserCredentials credentials)
	{
		this.credentials=credentials;
	}

	public String getKey()
	{
		return this.key;
	}

	public void setKey(String key)
	{
		this.key=key;
	}

	public Common.Direction getDirection()
	{
		return this.direction;
	}

	public void setDirection(Common.Direction direction)
	{
		this.direction=direction;
	}

	public Class<? extends Edge> getEdgeType()
	{
		return this.edgeType;
	}

	public void setEdgeType(Class<? extends Edge> edgeType)
	{
		this.edgeType=edgeType;
	}

	public long getFromOrdinal()
	{
		return this.fromOrdinal;
	}

	public void setFromOrdinal(Long fromOrdinal)
	{
		this.fromOrdinal=fromOrdinal;
	}

	public long getToOrdinal()
	{
		return this.toOrdinal;
	}

	public void setToOrdinal(Long toOrdinal)
	{
		this.toOrdinal=toOrdinal;
	}

	public boolean isImmediate()
	{
		return this.immediate;
	}

	public void setImmediate(boolean immediate)
	{
		this.immediate=immediate;
	}

	public BulkItems<DataVertex> getBulkItems()
	{
		return this.bulkItems;
	}
}
