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

package com.lusidity.collections;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.domains.object.Edge;
import com.lusidity.system.security.UserCredentials;

import java.util.Iterator;

public class ElementEntries<T extends IQueryResult> implements Iterator<T>
{
	private static final int LIMIT=1;
	private final BaseQueryBuilder queryBuilder;
	private final int limit;
	private final DataVertex vertex;
	private int start=0;
	private Integer size=0;
	private Iterator<IQueryResult> iterator=null;
	private UserCredentials credentials=null;

// Constructors
	public ElementEntries(DataVertex vertex, BaseQueryBuilder queryBuilder, int start, int limit, int size)
	{
		super();
		this.queryBuilder=queryBuilder;
		this.start=start;
		this.size=size;
		this.limit=limit;
		this.vertex=vertex;
		this.credentials=vertex.getCredentials();
		this.getNext();
		if (0==this.size)
		{
			this.setSize();
		}
	}

	public void getNext()
	{
		this.queryBuilder.setLimit(this.limit);
		this.queryBuilder.setStart(this.start);
		this.iterator=this.queryBuilder.execute().iterator();
	}

	private void setSize()
	{
		this.queryBuilder.setApi(BaseQueryBuilder.API._count);
		this.size=this.queryBuilder.execute().getCount();
		this.queryBuilder.setApi(BaseQueryBuilder.API._search);
	}

// Overrides
	@Override
	public boolean hasNext()
	{
		return this.start<this.size;
	}

	@Override
	public T next()
	{
		T result=null;
		if (this.start<this.size)
		{
			if (!this.iterator.hasNext())
			{
				this.getNext();
			}
			// It is possible for the size of the collection to get out of sync because of CRUD operations
			// during lazy loading we are just going to have to ignore them.  Throwing errors on normal behavior
			// is just not the right answer.
			//noinspection unchecked
			result=((null!=this.iterator) && this.iterator.hasNext()) ? (T) this.iterator.next() : null;
		}
		if (null!=result)
		{
			result.setCredentials(this.vertex.getCredentials());
		}
		this.start++;
		return result;
	}

	public boolean remove(int idx, Class<? extends Edge> edgCls)
	{
		boolean result=false;
		IQueryResult qr=this.getAt(idx);
		if (null!=qr)
		{
			try
			{
				Edge edge=qr.getEdge();
				if (null!=edge)
				{
					result=edge.delete();
				}
				if (!result)
				{
					Environment.getInstance().getReportHandler().warning("Could not delete edge at %d.", idx);
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		return result;
	}

	public T getAt(int idx)
	{
		this.start=idx;
		return this.next();
	}
}
