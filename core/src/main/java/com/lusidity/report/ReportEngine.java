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

package com.lusidity.report;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.report.BaseReport;
import com.lusidity.framework.exceptions.ApplicationException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

public class ReportEngine
{
	private final IDataStore dataStore;

	// Constructors
	public ReportEngine(IDataStore dataStore){
		super();
		this.dataStore = dataStore;
	}

	public boolean save(DataVertex parent, BaseReport report)
		throws Exception
	{
		report.fetchRelatedId().setValue(parent.getUri().toString());
		report.fetchId().setValue(parent.fetchId().getValue());
		return report.save(this.dataStore);
	}

	public boolean update(BaseReport report)
		throws ApplicationException
	{
		return this.dataStore.update(report, 0);
	}

	public boolean deleteAll(DataVertex vertex, Class<? extends BaseReport> cls)
	{
		boolean result=true;
		Collection<BaseReport> reports=this.get(vertex, cls);
		for (BaseReport report : reports)
		{
			boolean deleted=this.delete(report);
			if (!deleted)
			{
				result=false;
			}
		}
		return result;
	}

	public Collection<BaseReport> get(DataVertex vertex, Class<? extends BaseReport> cls){
		Collection<BaseReport> results = new ArrayList<>();

		try
		{
			BaseReport report = this.dataStore.getObjectById(cls, cls, vertex.fetchId().getValue());

			if(null!=report)
			{
				results.add(report);
			}

			if(results.isEmpty())
			{
				Constructor constructor = cls.getConstructor();
				report = (BaseReport)constructor.newInstance();

				BaseQueryBuilder qb = report.getQuery(vertex);

				if(null==qb)
				{
					qb = this.dataStore.getIndexStore().getQueryBuilder(cls, cls, 0, 0);
					qb.filter(BaseQueryBuilder.Operators.must, "relatedId", BaseQueryBuilder.StringTypes.raw, vertex.getUri().toString());
					//qb.filter(BaseQueryBuilder.Operators.must, "vertexType", BaseQueryBuilder.StringTypes.raw, ClassHelper.getClassKey(cls));
					qb.sort("createdWhen", BaseQueryBuilder.Sort.desc);
				}

				qb.setApi(BaseQueryBuilder.API._count);

				int hits = qb.execute().getHits();
				qb.setApi(BaseQueryBuilder.API._search);

				int next = 100;

				int on=0;
				boolean stop=false;

				for (int i=0; i<hits; i+=next)
				{
					qb.setStart(i);
					qb.setLimit(next);
					QueryResults queryResults=qb.execute();
					if (queryResults.isEmpty())
					{
						break;
					}

					for (IQueryResult result : queryResults)
					{
						report=result.getVertex();
						results.add(report);
					}
				}
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return results;
	}

	public boolean delete(BaseReport report)
	{
		return report.delete(this.dataStore);
	}

	// Getters and setters
	public IDataStore getDataStore(){
		return this.dataStore;
	}
}
