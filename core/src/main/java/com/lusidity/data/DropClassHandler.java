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
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.process.BaseProgressHandler;

import java.io.File;
import java.util.Set;

public class DropClassHandler extends BaseProgressHandler
{

	private final Class<? extends DataVertex> cls;

// Constructors
	public DropClassHandler(Class<? extends DataVertex> cls, int maxThreads, int maxItems)
	{
		super(maxThreads, maxItems);
		this.cls=cls;
	}

// Overrides
	@Override
	protected void done()
	{

	}

	@Override
	public String getStatusText()
	{
		return null;
	}

	@Override
	public File writeExceptionReport()
	{
		return null;
	}

	@Override
	public void start()
	{
		try
		{
			this.deleteAll(100);
			if (this.getProcessStatus().fetchSkipped().getValue().getCount()==0)
			{
				boolean dropped=Environment.getInstance().getDataStore().drop(this.cls);
				if (dropped)
				{
					dropped=Environment.getInstance().getIndexStore().drop(this.cls, null);
					if (!dropped)
					{
						Environment.getInstance().getReportHandler().severe("The index at /%s was not dropped.", ClassHelper.getIndexKey(this.cls));
					}
					else
					{
						Environment.getInstance().getIndexStore().drop(Edge.class, this.cls);
						Set<Class<? extends Edge>> subTypesOf=Environment.getInstance().getReflections().getSubTypesOf(Edge.class);
						for (Class<? extends Edge> subType : subTypesOf)
						{
							Environment.getInstance().getIndexStore().drop(subType, this.cls);
						}
					}
				}
				else
				{
					Environment.getInstance().getReportHandler().severe("The table %s was not dropped.", this.cls.getSimpleName());
				}
			}
		}
		catch (ApplicationException e)
		{
			Environment.getInstance().getReportHandler().severe(e);
		}
	}

	private void deleteAll(int limit)
		throws ApplicationException
	{
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(this.cls, this.cls, BaseQueryBuilder.API._search, 0, limit);
		qb.filter(BaseQueryBuilder.Operators.matchAll, null, BaseQueryBuilder.StringTypes.na, null);
		QueryResults queryResults=qb.execute();
		if ((this.getProcessStatus().fetchTotal().getValue().getCount()==0) && (null!=queryResults.getHits()))
		{
			this.getProcessStatus().fetchTotal().getValue().add(queryResults.getHits());
		}
		if (!queryResults.isEmpty())
		{
			for (IQueryResult queryResult : queryResults)
			{
				DataVertex dataVertex=queryResult.getVertex();
				if (null!=dataVertex)
				{
					boolean deleted=dataVertex.delete();
					if (deleted)
					{
						this.getProcessStatus().fetchProcessed().getValue().increment();
					}
					else
					{
						this.getProcessStatus().fetchSkipped().getValue().increment();
					}
				}
			}
			//recursive until empty.
			this.deleteAll(limit);
		}
	}
}
