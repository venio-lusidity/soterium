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
import com.lusidity.collections.ElementEdges;
import com.lusidity.core.ElementAction;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.BaseVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.system.primitives.Primitive;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

@AtWebResource(pathTemplate = "/svc/domains", methods = "get", description = "A list of all available domains and their properties.", optionalParams = "type")
@AtAuthorization(required = true)
public class DomainsServerResource extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Get
    public Representation retrieve(){
        JsonData result = JsonData.createObject();
        JsonData items = JsonData.createArray();
        List<Class<? extends BaseVertex>> list = new ArrayList<>();

        Set<Class<? extends BaseVertex>> types = Environment.getInstance().getReflections().getSubTypesOf(BaseVertex.class);
        for(Class<? extends BaseVertex> type: types){
            list.add(type);
        }

	    Collection<Class> exclusions = new ArrayList<>();
	    exclusions.add(Identity.class);
	    exclusions.add(ElementAction.class);

        Set<Class<? extends Primitive>> primitives = Environment.getInstance().getReflections().getSubTypesOf(Primitive.class);
	    for(Class<? extends Primitive> prim: primitives){
	    	exclusions.add(prim);
	    }

        //noinspection Convert2Lambda
        list.sort(new Comparator<Class<? extends BaseVertex>>() {
            @Override
            public int compare(Class<? extends BaseVertex> o1, Class<? extends BaseVertex> o2) {
                int result = 0;
                try{
                    result = o1.getSimpleName().compareTo(o2.getSimpleName());
                }
                catch (Exception ingored){
                    result = -1;
                }
                return result;
            }
        });
        Collection<Class<? extends DataVertex>> cache = new ArrayList<>();
        Map<Class, Collection<Class>> classes = new HashMap<>();
        for(Class<? extends BaseVertex> vertexCls: list){
        	if(ClassX.isKindOf(vertexCls, ApolloVertex.class) && !ClassX.isKindOf(vertexCls, ElementAction.class))
	        {
	        	@SuppressWarnings("unchecked")
		        Class<? extends ApolloVertex> domain = (Class<? extends ApolloVertex>) vertexCls;
		        if (cache.contains(domain))
		        {
			        continue;
		        }
		        cache.add(domain);
		        AtSchemaClass sca=ClassHelper.getSchema(domain);
		        if (null!=sca)
		        {
			        String domainKey=ClassHelper.getClassKey(domain);
			        JsonData item=JsonData.createObject();
			        item.put("clsName", domain.getName());
			        item.put("simpleName", domain.getSimpleName());
			        item.put("name", sca.name());
			        item.put("key", domainKey);
			        item.put("description", sca.description());
			        item.put("discoverable", sca.discoverable());
			        item.put("writable", sca.writable());
			        item.put("url", String.format("%s%s", "/svc/domain", domainKey));
			        item.put("indexKey", ClassHelper.getIndexKey(domain));
			        item.put("parent", this.getParent(domain, classes));
			        item.put("queryable", !ClassX.isAbstract(domain) && !ClassX.isInterface(domain) && !exclusions.contains(domain));

			        DataVertex vertex = null;
			        try
			        {
				        Constructor constructor=domain.getConstructor();
				        vertex =(DataVertex) constructor.newInstance();
			        }
			        catch (Exception ignored){}

			        JsonData properties=JsonData.createArray();
			        Collection<Field> fields=PropertyHelper.getAllFields(domain);
			        for (Field field : fields)
			        {
				        if (PropertyHelper.isUsable(field))
				        {
					        try
					        {
						        Class cls=field.getType();
						        JsonData property=JsonData.createObject();
						        property.put("name", field.getName());
						        property.put("type", cls);

						        if (ClassX.isKindOf(cls, KeyData.class) && (null!=vertex))
						        {
						        	try
							        {
							        	/*
								        field.setAccessible(true);
								        Method method=cls.getMethod(field.getName(), cls);
								        KeyData keyData=(KeyData) method.invoke(vertex);
								        property.put("discoverable", keyData.isDiscoverable());
								        */
							        }
							        catch (Exception ex){}
						        }
						        else if (ClassX.isKindOf(cls, KeyDataCollection.class))
						        {
							        // TODO: make this WriteConsoleJob the information about the KeyData object (not high importance)
						        }
						        else if (ClassX.isKindOf(cls, ElementEdges.class))
						        {
							        AtSchemaProperty sp=field.getAnnotation(AtSchemaProperty.class);
							        if (null!=sp)
							        {
								        boolean isOut=(Common.Direction.OUT==sp.direction());
								        Class<? extends DataVertex> c=(isOut) ? sp.expectedType() : domain;
								        String fieldName=isOut ? field.getName() : sp.fieldName();
								        String propertyKey=ClassHelper.getPropertyKey(c, fieldName);
								        JsonData schemaProperty=JsonData.createObject();
								        schemaProperty.put("description", sp.description());
								        schemaProperty.put("key", propertyKey);
								        schemaProperty.put("indexKey", ClassHelper.getIndexKey(sp.expectedType()));
								        schemaProperty.put("name", sp.name());
								        schemaProperty.put("direction", sp.direction());
								        schemaProperty.put("edgeType", ClassHelper.getClassKey(sp.edgeType()));
								        schemaProperty.put("edgeTypeClass", sp.edgeType());
								        schemaProperty.put("expectedType", ClassHelper.getClassKey(sp.expectedType()));
								        schemaProperty.put("expectedTypeClass", sp.expectedType());
								        schemaProperty.put("discoverable", sp.discoverable());
								        property.put("schemaProperty", schemaProperty);
							        }
						        }
						        properties.put(property);
					        }
					        catch (Exception ignored)
					        {
					        }
				        }
			        }
			        item.put("properties", properties);
			        items.put(item);
		        }
	        }
        }

        Class cls = DataVertex.class;
	    JsonData item=JsonData.createObject();
	    item.put("clsName", cls.getName());
	    item.put("simpleName", cls.getSimpleName());
	    items.put(item);

	    cls = BaseVertex.class;
	    item=JsonData.createObject();
	    item.put("clsName", cls.getName());
	    item.put("simpleName", cls.getSimpleName());
	    items.put(item);

        result.put("total", items.length());
        result.put("results", items);
        result.put("structure", this.getChildren(BaseVertex.class, classes));

        return this.getRepresentation(result);
    }

	private JsonData getChildren(Class cls, Map<Class, Collection<Class>> classes)
	{
		JsonData result = JsonData.createObject();
		result.put("clsName", cls.getName());
		result.put("simpleName", cls.getSimpleName());
		JsonData results = JsonData.createArray();
		result.put("results", results);

		Collection<Class> items = classes.get(cls);
		if(null!=items){
			for(Class item: items){
				JsonData child = this.getChildren(item, classes);
				results.put(child);
			}
		}

		return result;
	}

	private JsonData getParent(Class cls, Map<Class, Collection<Class>> classes)
	{
		JsonData result = null;
		Class superclass = cls.getSuperclass();
		if(null!=superclass){
			if(!classes.containsKey(superclass)){
				classes.put(superclass, new ArrayList<>());
			}
			Collection<Class> children = classes.get(superclass);
			if(!children.contains(cls))
			{
				classes.get(superclass).add(cls);
			}
			result = JsonData.createObject();
			result.put("clsName", cls.getName());
			JsonData parent = this.getParent(superclass, classes);
			if(null!=parent){
				result.put("parent", parent);
			}
		}
		return result;
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
