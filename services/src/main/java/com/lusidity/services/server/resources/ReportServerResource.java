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

package com.lusidity.services.server.resources;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.report.BaseReport;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.report.ReportEngine;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.Collection;

@AtWebResource(pathTemplate="/svc/domains/{domain}/{webId}/report", methods="get", description="Get detailed data on an element of infrastructure.")
@AtAuthorization
public
class ReportServerResource extends BaseServerResource
{
	// Overrides
	@Override
	public Representation remove()
	{
		JsonData result=JsonData.createObject();
		try
		{
			String webId=this.getAttribute("webId");
			boolean debug=this.getBoolean("debug");
			if(debug)
			{
				result.put("debug", true);
				result.put("method", "delete");
				result.put("webId", webId);
				result.put("resource", this.getClass().getName());
			}
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().severe(e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return this.getRepresentation(result);
	}

	@Override
	public
	Representation retrieve()
	{
		JsonData result=JsonData.createObject();
		try
		{
			String webId=this.getAttribute("webId");
			boolean debug=this.getBoolean("debug");
			if(debug)
			{
				result.put("debug", true);
				result.put("method", "get");
				result.put("webId", webId);
				result.put("resource", this.getClass().getName());
			}
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().severe(e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return this.getRepresentation(result);
	}

	@Override
	public
	Representation store(Representation representation)
	{
		JsonData result=JsonData.createObject();
		try
		{
			String webId=this.getAttribute("webId");
			boolean debug=this.getBoolean("debug");
			if(debug)
			{
				result.put("debug", true);
				result.put("method", "put");
				result.put("webId", webId);
				result.put("resource", this.getClass().getName());
			}
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().severe(e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return this.getRepresentation(result);
	}

	@Override
	public
	Representation update(Representation representation)
	{
		JsonData result=JsonData.createObject();
		try
		{
			String webId=this.getAttribute("webId");
			boolean debug=this.getBoolean("debug");
			if(debug)
			{
				result.put("debug", true);
				result.put("method", "post");
				result.put("webId", webId);
				result.put("resource", this.getClass().getName());
			}
			else{
				Class<? extends DataVertex> store=BaseDomain.getDomainType(this.getAttribute("domain"));
				DataVertex vertex=this.getVertex(store, webId);
				if ((null!=vertex) && this.authorized(vertex, Permission.Types.read))
				{
					JsonData origin=vertex.toJson(false);
					this.handleWebResource(vertex, null, origin, vertex.getQueryResultHandlers());
					result.put("origin", origin);
					//noinspection unchecked
					JsonData results = this.getReport(vertex);
					result.put("reports", results);
				}
				else
				{
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}
			}
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().severe(e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return this.getRepresentation(result);
	}

	private JsonData getReport(DataVertex vertex)
	{
		JsonData results=JsonData.createArray();
		JsonData content=this.getJsonContent();
		boolean verbose=content.getBoolean("verbose");
		String type=content.getString("type");
		String dsType=content.getString("datastoreType");
		Class<? extends DataVertex> cls=BaseDomain.getDomainType(type);

		if (ClassX.isKindOf(cls, BaseReport.class))
		{
			IDataStore ds=StringX.equals(dsType, "data") ? Environment.getInstance().getDataStore() : Environment.getInstance().getReportStore();
			if (null!=ds)
			{
				ReportEngine engine=new ReportEngine(ds);
				//noinspection unchecked
				Collection<BaseReport> reports=engine.get(vertex, (Class<? extends BaseReport>) cls);
				for (BaseReport report : reports)
				{
					JsonData item=report.toJson(false);
					if (verbose)
					{
						JsonData extendedData=report.getExtendedData();
						if (null!=extendedData)
						{
							Collection<String> keys=extendedData.keys();
							for (String key : keys)
							{
								item.put(key, extendedData.getFromPath(key));
							}
						}
					}
					results.put(item);
				}
			}
		}
		return results;
	}
}
