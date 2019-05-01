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

import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.representation.Representation;

import java.lang.reflect.Field;
import java.util.Collection;

@AtWebResource(pathTemplate = "/svc/domain/{*}", matchingMode = AtWebResource.MODE_BEST_MATCH,
        methods = "get", description = "A specific domain type and all of its properties.")
@AtAuthorization(required = true)
public class DomainServerResource extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve(){
        JsonData result = null;
        String key = StringX.replace(this.getRequest().getOriginalRef().getPath().toLowerCase(), "/svc/domain", "");
        Class<? extends DataVertex> domain = BaseDomain.getDomainType(key);
        if(null!=domain) {
            AtSchemaClass sca = ClassHelper.getSchema(domain);
            if (null != sca) {
                String domainKey = ClassHelper.getClassKey(domain);
                result = JsonData.createObject();
                result.put("clsName", domain.getName());
                result.put("name", sca.name());
                result.put("key", domainKey);
                result.put("description", sca.description());
                result.put("writable", sca.writable());
                result.put("url", String.format("%s%s", "/svc/domain", domainKey));
                result.put("indexKey", ClassHelper.getIndexKey(domain));

                JsonData properties = JsonData.createArray();
                Collection<Field> fields = PropertyHelper.getAllFields(domain);
                for (Field field : fields) {
                    if (PropertyHelper.isUsable(field)) {
                        try {
                            Class cls = field.getType();
                            JsonData property = JsonData.createObject();
                            property.put("name", field.getName());
                            property.put("type", cls);

                            if (ClassX.isKindOf(cls, KeyDataCollection.class)){
                                // TODO: make this WriteConsoleJob the information about the KeyData object (not high importance)
                            }
                            else if(ClassX.isKindOf(cls,ElementEdges.class)){
                                AtSchemaProperty sp = field.getAnnotation(AtSchemaProperty.class);
                                if (null!=sp) {
                                    boolean isOut = (Common.Direction.OUT==sp.direction());
                                    Class<? extends DataVertex> c = (isOut) ? sp.expectedType() : domain;
                                    String fieldName =  isOut ? field.getName() : sp.fieldName();
                                    String propertyKey = ClassHelper.getPropertyKey(c, fieldName);
                                    JsonData schemaProperty = JsonData.createObject();
                                    schemaProperty.put("description", sp.description());
                                    schemaProperty.put("key", propertyKey);
                                    schemaProperty.put("indexKey", ClassHelper.getIndexKey(sp.expectedType()));
                                    schemaProperty.put("name", sp.name());
                                    schemaProperty.put("direction", sp.direction());
                                    schemaProperty.put("edgeType", ClassHelper.getClassKey(sp.edgeType()));
                                    schemaProperty.put("expectedType", ClassHelper.getClassKey(sp.expectedType()));
                                    property.put("schemaProperty", schemaProperty);
                                }
                            }
                            properties.put(property);
                        } catch (Exception ignored) {
                        }
                    }
                }
                result.put("properties", properties);
            }
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public Representation update(Representation representation)
    {
        return null;
    }

}
