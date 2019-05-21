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

package com.lusidity.services.server.resources.helper;


import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementAttributes;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtRequiredField;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Map;

public class VertexHelper {
    private final UserCredentials credentials;

    public VertexHelper(UserCredentials credentials) {
        super();
        this.credentials = credentials;
    }


    @SuppressWarnings("OverlyComplexMethod")
    public DataVertex process(String key, JsonData data)
        throws Exception
    {
        String id = data.getString(DataVertex.VERTEX_URI);
        Class<? extends DataVertex> cls = Environment.getInstance().getApolloVertexType(key);
        if(null==cls){
            cls = Environment.getInstance().getApolloVertexType(key);
        }
        DataVertex result=null;
        if(null!=cls) {
            AtSchemaClass sca = ClassHelper.getSchema(cls);
            if((null!=sca) && sca.writable()){
                boolean validated = this.isValidated(cls,data);
                if(validated){
                    DataVertex vertex = StringX.isBlank(id) ? null : VertexFactory.getInstance().get(id);

                    if((null==vertex) && StringX.isBlank(id)){
                        Constructor constructor = cls.getConstructor();
                        vertex =(DataVertex) constructor.newInstance();
                    }

                    if ((null!=vertex))
                    {
                        vertex.setCredentials(this.credentials);
                        boolean write=VertexHelper.getSecurityPolicy(vertex).canWrite();
                        if (write)
                        {
                            Map<String, Method> methods = PropertyHelper.getAllMethods(vertex.getClass());
                            for(Map.Entry<String, Method> entry: methods.entrySet()){
                                Method method = entry.getValue();
                                if(ClassX.isKindOf(method.getReturnType(), KeyData.class)){
                                    try
                                    {
                                        method.setAccessible(true);
                                        Object invoked = method.invoke(vertex);
                                        if(invoked instanceof KeyData){
                                            KeyData keyData = (KeyData)invoked;
                                            if(data.hasKey(keyData.getKeyName()))
                                            {
                                                Object value=data.getObjectFromPath(keyData.getKeyName());
                                                if(null!=value){
                                                    value = Common.getTypeFor(value, keyData.getFieldType());
                                                }
                                                //noinspection unchecked
                                                keyData.setValue(value);
                                            }
                                        }
                                    }
                                    catch (Exception ex){
                                        Environment.getInstance().getReportHandler().warning(ex);
                                    }
                                }
                            }
                            if(vertex.isDirty())
                            {
                                vertex.save();
                            }
                        }
                        if (write && vertex.hasId())
                        {
                            result=vertex;
                            this.processEdges(vertex, data);
                        }
                        else
                        {
                            throw new ApplicationException("The vertex failed to step4.");
                        }
                    }
                    else
                    {
                        throw new ApplicationException("The vertex failed to step4.");
                    }
                }
                else{
                    throw new ApplicationException("The data received is missing required fields.");
                }
            }
            else{
                Environment.getInstance().getReportHandler().info("The vertexType, %s, is not writable.", key);
            }
        }
        else{
            Environment.getInstance().getReportHandler().info("The vertexType, %s, is an unknown Class.", key);
        }
        return result;
    }

    public static PolicyDecisionPoint getSecurityPolicy(DataVertex vertex)
    {
        PolicyDecisionPoint result = null;
        BasePrincipal principal = ((null!=vertex) && (null!=vertex.getCredentials())) ? vertex.getCredentials().getPrincipal() : null;
        if(null!=vertex)
        {
            vertex.setCredentials(vertex.getCredentials());
            result=vertex.getSecurityPolicy(principal);
        }
        else{
            result= new PolicyDecisionPoint(null, null);
        }
        return result;
    }

    private void processEdges(DataVertex vertex, JsonData data) throws ApplicationException {
        for(String key: data.keys()){
            if(StringX.startsWith(key, "/") && !StringX.equals(key, DataVertex.VERTEX_URI)){
                Object o = data.getObjectFromPath(key);
                if(o instanceof JsonData){
                    JsonData items = new JsonData(o);
                    if(items.isJSONArray()){
                        for(Object obj: items){
                            if(obj instanceof String){
                                this.processEdge(vertex, key, obj.toString());
                            }
                            else if(obj instanceof JSONObject){
                                this.handleJsonObject(vertex, key, new JsonData(obj));
                            }
                            else{
                                throw new InvalidParameterException(String.format("The key, %s, should either be a string or an array of strings.", key));
                            }
                        }
                    }
                    else if(items.isJSONObject()){
                        this.handleJsonObject(vertex, key, items);
                    }
                    else{
                        throw new InvalidParameterException(String.format("The key, %s, should either be a string or an array of strings.", key));
                    }
                }
                else if(o instanceof String){
                    this.processEdge(vertex, key, o.toString());
                }
                else{
                    Environment.getInstance().getReportHandler().severe("Unknown type not handled, %s.", (null!=o)? o.getClass().getName() : "undefined object");
                }
            }
        }
    }

    private void handleJsonObject(DataVertex vertex, String key, JsonData data) {
        try {
            if(data.hasKey("vertexType")) {
                DataVertex other = this.process(data.getString("vertexType"), data);
                if(null!=other) {
                    this.processEdge(vertex, key, other);
                }
            }
            else{
                throw new InvalidParameterException("The inner json object does not have the property vertexType");
            }
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().severe(ex);
        }
    }

    private DataVertex processEdge(DataVertex vertex, String key, Object value) throws ApplicationException {
        DataVertex result = null;
        Collection<Field> fields = PropertyHelper.getAllFields(vertex.getClass());
        for(Field field: fields){
            AtSchemaProperty schemaProperty = field.getAnnotation(AtSchemaProperty.class);
            if(null!=schemaProperty){
                String propertyKey = ClassHelper.getPropertyKey(schemaProperty.expectedType(), field);
                if(StringX.equals(propertyKey, key)){
                    result = (ClassX.isKindOf(value, DataVertex.class)) ? (DataVertex) value :
                        VertexFactory.getInstance().get(schemaProperty.expectedType(), value);

                    if((null==result)){
                       if((value instanceof String) && ClassX.isKindOf(schemaProperty.expectedType(), RawString.class)){
                           RawString rawString = new RawString(value.toString());
                           result = rawString;
                       }
                       else if((value instanceof String) && StringX.startsWith(value.toString(), "/domains")){
                           try
                           {
                               result = VertexFactory.getInstance().get(value.toString());
                           }
                           catch (Exception e)
                           {
                               Environment.getInstance().getReportHandler().severe(e);
                           }
                       }
                    }

                    if(null!=result){
                        if((schemaProperty.direction() == Common.Direction.OUT)){
                            ElementAttributes attributes = vertex.build(vertex, field, schemaProperty);
                            //noinspection unchecked
                            attributes.add(result);
                            break;
                        }
                        else if(StringX.equals(vertex.fetchId().getValue(), result.fetchId().getValue())) {
                            throw new ApplicationException("You cannot link a vertex to itself.");
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean isValidated(Class<? extends DataVertex> cls, JsonData data) {
        boolean result = true;

        Collection<Field> fields = PropertyHelper.getAllFields(cls);
        if((fields!=null) && !fields.isEmpty()){
            for (Field field : fields) {
                String title = field.getName();
                AtRequiredField rfa = field.getAnnotation(AtRequiredField.class);
                if (rfa != null) {
                    result = data.hasValue(title);
                    //noinspection StatementWithEmptyBody
                    if (result && rfa.isUnique()) {
                        //TODO implements unique field property
                    }
                }
                if (!result) {
                    break;
                }
            }
        }

        return result;
    }
}
