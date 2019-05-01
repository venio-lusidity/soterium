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
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.helper.VertexHelper;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}", methods = "delete,get,post", description = "Delete, Get, " +
"Create or Update a vertex.", matchingMode = AtWebResource.MODE_FIRST_MATCH)
@AtAuthorization
public class VertexServerResource
	extends BaseServerResource {

	@Override
	public Representation retrieve() {
		JsonData result = JsonData.createObject();
		String url = null;
		try {
			url = this.getRequest().getOriginalRef().toString();
			DataVertex vertex = this.getVertex();
			if (null != vertex)
			{
				if(this.authorized(vertex, Permission.Types.read)) {
					vertex.fetchOrdinal().setValue(null);
					result = vertex.toJson(false);
					this.handleWebResource(vertex, null, result, null);
					if(ClassX.isKindOf(vertex, ApolloVertex.class)){
						ApolloVertex av = (ApolloVertex)vertex;
						String key = av.getDescriptions().getKey();
						if(!result.hasKey(key) && !av.getDescriptions().isEmpty()){
							result.put(key, av.getDescriptions().toJson(false));
						}
					}
				}
				else {
					this.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					result.put("_response_status", "unauthorized", true);
					result.put("_response_code", 403, true);
				}
			}
			else
			{
				this.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().severe(
				"Exception processing GET %s: %s", StringX.isBlank(url) ? "unknown URI" : url, e.toString()
			);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}

		return this.getRepresentation(result);
	}

	@Override
	public Representation store(Representation representation)
	{
		return null;
	}

	@Override
	public Representation remove() {
		JsonData result = JsonData.createObject();

		String url = null;
		try {
			url = this.getRequest().getOriginalRef().toString();
			String webId = this.getAttribute("webId");

			DataVertex vertex = this.getVertex(webId);

			if ((null!=vertex) && this.getUserCredentials().getPrincipal().isAdmin(true))
			{
				boolean deleted = vertex.delete();
				result.put("deleted", deleted);
			}
			else
			{
				this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			}
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().severe(
				"Exception processing DELETE %s: %s", StringX.isBlank(url) ? "unknown URI" : url, e.toString()
			);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}

		return this.getRepresentation(result);
	}

	@SuppressWarnings({
		"ThrowCaughtLocally",
		"OverlyComplexMethod",
		"OverlyNestedMethod"        ,
		"OverlyLongMethod"
	})
	@Override
	public Representation update(Representation representation) {
		String webId = this.getAttribute("webId");
		String mode = this.getParameter("mode");
		boolean isNew = ((StringX.endsWithIgnoreCase(webId, "new")) || StringX.equalsIgnoreCase(mode, "add"));
		JsonData result = JsonData.createObject();
		try {
			JsonData data = this.getJsonContent();
			if(data.isValid() && data.hasKey("vertexType")) {
				String key = data.getString("vertexType");
				if(!StringX.isBlank(key))
				{
					VertexHelper vh = new VertexHelper(this.getUserCredentials());
					DataVertex item = vh.process(key, data);
					if ((null!=item) && item.fetchId().isNotNullOrEmpty())
					{
						if (data.hasKey("edgeKey") && data.hasKey("edgeDirection"))
						{
							if (isNew)
							{
								Long ordinal = data.getLong("ordinal");
								@SuppressWarnings("StandardVariableNames")
								String k = data.getString("edgeKey");
								String t = data.getString("edgeType");
								String p = (data.getString(data.hasKey("fromId") ? "fromId" : "toId"));

								Class<? extends Edge> edgeCls = Edge.class;
								if(!StringX.isBlank(t)){
									Class<? extends DataVertex> cls = BaseDomain.getDomainType(t);
									if(ClassX.isKindOf(cls, Edge.class)){
										//noinspection unchecked
										edgeCls =(Class<? extends Edge>) cls;
									}
								}
								Common.Direction direction = data.getEnum(Common.Direction.class, "edgeDirection");
								DataVertex parent = this.getVertex(StringX.isBlank(p) ? webId : p);
								if ((null!=parent) && !parent.getEdgeHelper().hasRelationshipWith(edgeCls, item, k, direction))
								{
									if (!StringX.isBlank(k) && (null!=direction))
									{
										EdgeData edgeData = new EdgeData();
										edgeData.setDirection(direction);
										edgeData.setKey(k);
										edgeData.setEdgeType(edgeCls);
										if(null!=ordinal){
											if (direction==Common.Direction.OUT)
											{
												edgeData.setFromOrdinal(ordinal);
											}
											else{
												edgeData.setToOrdinal(ordinal);
											}
										}

										parent.getEdgeHelper().addEdge(parent, item, edgeData, true);
									}
									else
									{
										throw new ApplicationException("Failed to link vertex.");
									}
								}
							}
						}
						JsonData jd = item.toJson(false);
						this.handleWebResource(item, null, jd, null);
						result.put("result", jd);
					}
					else
					{
						throw new ApplicationException("Failed to create vertex.");
					}
				}
				else{
					throw new ApplicationException("The vertexType cannot be empty.");
				}
			}
			else{
				throw new ApplicationException("The data is missing property vertexType.");
			}
		}
		catch (Exception ex){
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}

		return this.getRepresentation(result);
	}
}