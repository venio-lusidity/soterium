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
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryFactory;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.system.security.UserCredentials;

import java.util.Iterator;

public class NodeIterator<T extends DataVertex> implements Iterator<T>
{
	private final int limit;
	private final DataVertex vertex;
	private final Class<? extends Edge> store;
	private final Class<? extends DataVertex> partition;
	private final String label;
	private final Common.Direction direction;
	private int start=0;
	private Integer size=0;
	private Iterator<IQueryResult> iterator=null;
	private UserCredentials credentials=null;

// Constructors
	public NodeIterator(DataVertex vertex, Class<? extends Edge> store, Class<? extends DataVertex> partition, String label, Common.Direction direction, int start, int limit, int size)
	{
		super();
		this.store = store;
		this.partition = partition;
		this.label = label;
		this.direction = direction;
		this.start=start;
		this.size=size;
		this.limit=limit;
		this.vertex=vertex;
		this.credentials=vertex.getCredentials();
		this.getNext();
	}

	public void getNext()
	{
		IQueryFactory factory = Environment.getInstance().getQueryFactory();

		JsonData content = JsonData.createObject();
		content.put("store", ClassHelper.getIndexKey(this.store));
		content.put("partition", (((null!=this.partition) && (this.direction==Common.Direction.OUT)) ? ClassHelper.getIndexKey(this.partition) : null));
		content.put("label", this.label);
		content.put("lid", this.vertex.fetchId().getValue());
		content.put("direction", this.direction);
		content.put("start", this.start);
		content.put("limit", this.limit);

		Stopwatch sw = new Stopwatch();
		sw.start();
		QueryResults qrs = factory.request(HttpClientX.Methods.POST, BaseQueryBuilder.API._other_end.toString(), content, 0, this.limit);
		sw.stop();
		System.out.println(String.format("NodeIterator took %d and retrieved %d vertices.", sw.elapsed().getMillis(), qrs.size()));
		this.iterator=qrs.iterator();
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
			result=((null!=this.iterator) && this.iterator.hasNext()) ? this.getNext(this.iterator.next()) : null;
		}
		if (null!=result)
		{
			result.setCredentials(this.vertex.getCredentials());
		}
		this.start++;
		return result;
	}

	public T remove(int idx, Class<? extends Edge> edgCls, DataVertex dataVertex, String key, Common.Direction direction)
	{
		T result=this.getAt(idx);
		if (null!=result)
		{
			try
			{
				DataVertex from=(Common.Direction.OUT==direction) ? dataVertex : result;
				DataVertex to=(Common.Direction.OUT==direction) ? result : dataVertex;

				Edge edge=from.getEdgeHelper().getEdge(edgCls, to, key, direction);
				if (null!=edge)
				{
					edge.delete();
				}
				else
				{
					Environment.getInstance().getReportHandler().warning("Could not delete edge from %s to %s.", from.fetchId().getValue(), to.fetchId().getValue());
					result=null;
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
				result=null;
			}
		}
		return result;
	}

	public T getAt(int idx)
	{
		this.start=idx;
		return this.next();
	}

	private T getNext(IQueryResult queryResult)
	{
		T result=null;
		if (null!=queryResult)
		{
			try
			{
				DataVertex other=queryResult.getVertex();
				if (null!=other)
				{
					other.setCredentials(this.credentials);
					//noinspection unchecked
					result=((T) other);
				}
				// The credentials are being cleared out somewhere in "getOtherEnd".
				this.vertex.setCredentials(this.credentials);
			}
			catch (Exception e){
				Environment.getInstance().getReportHandler().severe(e);
			}
		}
		return result;
	}
}
