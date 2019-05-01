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
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementAttributes;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.*;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePosition;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.object.Edge;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@SuppressWarnings("ThrowCaughtLocally")
@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/properties/{*}", matchingMode = AtWebResource.MODE_BEST_MATCH,
methods = "get, post", description = "Get and/or update vertices related to the vertex by the property key.")
@AtAuthorization()
public class LinkedServerResource extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
        JsonData result = JsonData.createArray();
        try {
            String sDirection = this.getParameter("direction");

            int start = this.getStart();
            int limit = this.getLimit();
            Common.Direction direction = null;
            if (!StringX.isBlank(sDirection)) {
                direction = Common.Direction.valueOf(sDirection.toUpperCase());
            }

            result = this.getResults(start, limit, direction);
        } catch (Exception e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    private JsonData getResults(int start, int limit, Common.Direction direction) {
        JsonData result = JsonData.createArray();
        String webId = this.getAttribute("webId");
        Class<? extends DataVertex> store=BaseDomain.getDomainType(this.getAttribute("domain"));
        DataVertex vertex = this.getVertex(store, webId);
        if (null != vertex) {
            Class<? extends DataVertex> cls = vertex.getActualClass();
            String key = this.getKey();
            if ((null!=cls) && !StringX.isBlank(key)) {
                AtSchemaProperty sp = ClassHelper.getPropertySchema(cls, key);
                if (null != sp)
                {
                    String sortON=this.getParameter("sortOn");
                    SortObjects sortObjects=null;
                    if (!StringX.isBlank(sortON))
                    {
                        BaseQueryBuilder.Sort sortOnDir=BaseQueryBuilder.Sort.asc;
                        String sod=this.getParameter("sortOnDir");
                        if (StringX.equals("asc", sod) || StringX.equals("desc", sod))
                        {
                            sortOnDir=Enum.valueOf(BaseQueryBuilder.Sort.class, sod);
                        }
                        sortObjects=new SortObjects();
                        sortObjects.add(new SortObject(sortON, sortOnDir));
                    }
                    boolean edges=this.getBoolean("edges");
                    QueryResults qrs=Environment.getInstance().getQueryFactory().getEdges(sp.edgeType(), vertex, key, null, sortObjects,(null!=direction) ? direction : sp.direction(), start, limit);
                    qrs.setScopingEnabled(false);
                    if (edges)
                    {
                        result=JsonData.createObject();
                        result.put("hits", qrs.getHits());
                        result.put("next", qrs.getNext());
                        result.put("limit", this.getLimit());
                        result.put("start", this.getStart());
                        JsonData results=JsonData.createArray();
                        for (IQueryResult qr : qrs)
                        {
                            Edge edge=qr.getEdge();
                            if (null!=edge)
                            {
                                results.put(edge.toJson(false));
                            }
                        }
                        result.put("results", results);
                    }
                    else if (!qrs.isEmpty())
                    {
                        result=qrs.toJson(vertex.fetchId().getValue(), BaseQueryBuilder.API._search, this.getUserCredentials(), QueryResults.Format._default, null);
                    }
                }
                else {
                    //TODO: validate this.
                    Collection<Class<? extends Edge>> edgeTypes = new ArrayList<>();
                    edgeTypes.add(Edge.class);
                    Set<Class<? extends Edge>> subTypesOf = Environment.getInstance().getReflections().getSubTypesOf(Edge.class);
                    for(Class<? extends Edge> subType: subTypesOf){
                        edgeTypes.add(subType);
                    }

                    QueryResults results = new QueryResults(null);

                    int hits = 0;
                    boolean hasNext = false;
                    for(Class<? extends Edge> edgeType: edgeTypes){
                        QueryResults queryResults = Environment.getInstance().getQueryFactory().getEdges(edgeType, vertex, key, null, null, direction, start, limit);
                        CollectionX.addAllIfUnique(results, queryResults);
                        hits += (null!=queryResults.getHits()) ? queryResults.getHits() : 0;
                        if(queryResults.getNext()<queryResults.getHits()){
                            hasNext = true;
                        }
                    }

                    if (!results.isEmpty()) {
                        results.setHits(hits);
                        results.setNext(hasNext? (start+limit) : hits);
                        result = results.toJson(vertex.fetchId().getValue(), BaseQueryBuilder.API._search, this.getUserCredentials(), QueryResults.Format._default, null);
                    }
                }
            }
        }
        return result;
    }

    /**
     * The webId must be the from an the posted object is the to.
     * Furthermore the vertexType must have a SchemaProperty with a direction of OUT.
     * @param representation Json object.
     * @return  Linked object.
     */
    @Override
    public Representation update(Representation representation) {
        JsonData result = JsonData.createObject();

        try {
            JsonData data = this.getJsonContent();
            String key = this.getKey();
            String webId = this.getAttribute("webId");
            boolean isDeleting = data.getBoolean("delete");

            if(data.isValid()) {
                if(!data.hasKey("from")){
                    throw new ApplicationException("The data is missing property \"from\".");
                }
                if(!data.hasKey("to")){
                    throw new ApplicationException("The data is missing property \"to\".");
                }
                String fvt = data.getString("from::vertexType");
                String fid =data.getString(data.hasKey("from::uri") ? "from::uri" : "from::/vertex/uri");
                Class<? extends DataVertex> fcls = Environment.getInstance().getApolloVertexType(fvt);
                DataVertex parent = VertexFactory.getInstance().get(fid);
                if(null==parent){
                    throw new ApplicationException("Invalid parameters could not find parents vertex.");
                }
                String tvt = data.getString("to::vertexType");
                String tid =data.getString(data.hasKey("to::uri") ? "to::uri" : "to::/vertex/uri");
                Class<? extends DataVertex> tcls = Environment.getInstance().getApolloVertexType(tvt);
                DataVertex child = VertexFactory.getInstance().get(tid);
                if(null==child){
                    throw new ApplicationException("Invalid parameters could not find children vertex.");
                }

                parent.setCredentials(this.getUserCredentials());
                child.setCredentials(this.getUserCredentials());
                if (StringX.equals(parent.fetchId().getValue(), child.fetchId().getValue()))
                {
                    throw new ApplicationException("You cannot link a vertex to itself.");
                }
                //noinspection DuplicateBooleanBranch
                boolean isAccountManager = false;
                if(ClassX.isKindOf(child, BasePrincipal.class) || (ClassX.isKindOf(child, BasePosition.class))){
                    isAccountManager = Group.isInGroup(this.getUserCredentials().getPrincipal(), "account managers");
                }
                if(isDeleting && (isAccountManager || (this.getSecurityPolicy(parent).canDelete() && this.getSecurityPolicy(child).canDelete()))){
                    boolean deleted = this.removeEdge(parent, child, key);
                    result.put("deleted", deleted);
                }
                else if(isAccountManager || (this.getSecurityPolicy(parent).canWrite() && this.getSecurityPolicy(child).canWrite())) {
                    Edge edge  = this.processEdge(parent, child, key);
                    if (null != edge) {
                        result.put("from", parent.toJson(false));
                        result.put("to", child.toJson(false));
                        result.put("edge", edge.toJson(false));
                    } else {
                        throw new ApplicationException("Could not create the link.");
                    }
                }
            }
            else{
                throw new ApplicationException("The data is missing property vertexType.");
            }
        }
        catch (Exception ex){
            result.put("failed", true);
            result.put("error", ex.getMessage());
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        return this.getRepresentation(result);
    }

    private Edge processEdge(DataVertex parent, DataVertex child, String key) throws ApplicationException {
        Edge result = null;
        Collection<Field> fields = PropertyHelper.getAllFields(parent.getClass());
        boolean added = false;
        for (Field field : fields)
        {
            AtSchemaProperty schemaProperty = field.getAnnotation(AtSchemaProperty.class);
            if (null != schemaProperty)
            {
                String propertyKey = ClassHelper.getPropertyKey(schemaProperty.expectedType(), field);
                if (StringX.equals(propertyKey, key))
                {
                    if ((null!=child) && ClassX.isKindOf(child.getClass(), schemaProperty.expectedType()))
                    {
                        ElementAttributes attributes = parent.build(parent, field, schemaProperty);
                        //noinspection unchecked
                        added = attributes.add(child);
                        if(added){
                            //noinspection unchecked
                            result = parent.getEdgeHelper().getEdge(attributes.getEdgeType(), child, key, Common.Direction.OUT);
                            break;
                        }
                        else{
                            throw new ApplicationException("The vertices are already linked.");
                        }
                    }
                    else if(null!=child){
                        throw new ApplicationException("The vertex being linked is not the right type.");
                    }
                }
            }
        }
        if(!added){
            throw new ApplicationException("The vertex does not have a defined property for the property key given.");
        }
        return result;
    }

    private boolean removeEdge(DataVertex parent, DataVertex child, String key) throws ApplicationException {
        boolean result = false;
        Collection<Field> fields = PropertyHelper.getAllFields(parent.getClass());
        for (Field field : fields)
        {
            AtSchemaProperty schemaProperty = field.getAnnotation(AtSchemaProperty.class);
            if (null != schemaProperty)
            {
                String propertyKey = ClassHelper.getPropertyKey(schemaProperty.expectedType(), field);
                if (StringX.equals(propertyKey, key))
                {
                    if ((null!=child) && ClassX.isKindOf(child.getClass(), schemaProperty.expectedType()))
                    {
                        ElementAttributes attributes = parent.build(parent, field, schemaProperty);
                        //noinspection unchecked
                        result = attributes.remove(child);
                    }
                }
            }
        }
        return result;
    }

    public String getKey() {
        return PropertyHelper.getFromUrl(this.getRequest().getOriginalRef().toUrl());
    }
}
