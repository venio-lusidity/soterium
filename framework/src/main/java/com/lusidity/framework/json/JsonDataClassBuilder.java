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

package com.lusidity.framework.json;

import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Collection;

public class JsonDataClassBuilder {
    private JsonData data = null;
    private StringBuilder template = new StringBuilder();

    public JsonDataClassBuilder(JsonData data) {
        super();
        this.data = data;
    }

    public void build(String className, File directory) {
        this.template.append("import java.util.Collection;");
        this.template.append("\nimport com.lusidity.framework.json.JsonData;");
        this.template.append("\nimport java.util.ArrayList;");
        this.template.append("\n\npublic class ").append(className).append("{");
        this.template.append("\n\nprivate JsonData data = null;");
        this.template.append("\n\n\tpublic ").append(className).append("(JsonData data){");
        this.template.append("\n\t\t").append("super();");
        this.template.append("\n\t\t").append("this.data = data;");
        this.template.append("\n\t\t").append("if(null==this.data){");
        this.template.append("\n\t\t\t").append("this.data = JsonData.createObject();");
        this.template.append("\n\t\t").append("}");
        this.template.append("\n\t}");

        if(null!=this.data) {
            if (this.data.isJSONArray()) {
                int last = 0;
                JsonData finalData = null;
                for (Object o : this.data) {
                    if (null != o) {
                        if (o instanceof JSONObject) {
                            JsonData item = new JsonData(o);
                            Collection<String> k = item.keys();
                            if (k.size() > last) {
                                finalData = item;
                                last = k.size();
                            }
                        } else {
                            break;
                        }
                    }
                    if (null != finalData) {
                        this.data = finalData;
                    }
                }
            }

            Collection<String> keys = this.data.keys();
            for (String key : keys) {
                Object value = this.data.getObjectFromPath(key);
                if (value instanceof JsonData) {
                    JsonData internal = new JsonData(value);

                    String innerClassName = String.format("%s%s", className, StringX.toPascalCase(key));
                    String propertyName = StringX.toCamelCase(innerClassName);

                    if (internal.isJSONArray()) {
                        boolean isData = false;
                        for (Object o : internal) {
                            if (null != o) {
                                isData = (o instanceof JSONObject);
                                break;
                            }
                        }

                        if (!isData) {
                            this.appendProperty(key, value, 1);
                        } else {
                            internal.buildClass(innerClassName, directory);
                            this.template.append(String.format("\n\n\tprivate Collection<%s> %s = null;", innerClassName, propertyName));
                            this.template.append("\n\n\t").append(String.format("public Collection<%s> get%s()", innerClassName, innerClassName)).append("{");

                            this.template.append(String.format("\n\t\tif(null==this.%s){", propertyName));

                            this.template.append(String.format("\n\t\t\tthis.%s = new ArrayList<>();", propertyName));

                            this.template.append("\n\t\t}");

                            this.template.append("\n\t\tif(null != this.data){");
                            this.template.append(String.format("\n\t\t\tJsonData items = this.data.getFromPath(\"%s\");", key));

                            this.template.append("\n\t\t\tif(items.isJSONArray()){");

                            this.template.append("\n\t\t\t\tfor(Object o: items){");
                            this.template.append("\n\t\t\t\t\tJsonData item = new JsonData(o);");
                            this.template.append(String.format("\n\t\t\t\t\t%s c = new %s(item);", innerClassName, innerClassName));
                            this.template.append(String.format("\n\t\t\t\t\tthis.%s.add(c);", propertyName));
                            this.template.append("\n\t\t\t\t}");

                            this.template.append("\n\t\t\t}");
                            this.template.append("\n\t\t\telse if(items.isJSONObject()){");

                            this.template.append("\n\t\t\t\tJsonData item = new JsonData(items);");
                            this.template.append(String.format("\n\t\t\t\t%s c = new %s(item);", innerClassName, innerClassName));
                            this.template.append(String.format("\n\t\t\t\tthis.%s.add(c);", propertyName));
                            this.template.append("\n\t\t\t}");

                            this.template.append("\n\t\t}");
                            this.template.append("\n\t\t").append(String.format("return this.%s;", propertyName));
                            this.template.append("\n\t").append("}");
                        }
                    } else if (internal.isJSONObject()) {
                        internal.buildClass(innerClassName, directory);
                        this.template.append("\n\n\t").append(String.format("public %s get%s()", innerClassName, innerClassName)).append("{");
                        this.template.append(String.format("\n\t\tJsonData item = this.data.getFromPath(\"%s\");", key));
                        this.template.append(String.format("\n\t\treturn (null!=item) ? new %s(item) : null;", innerClassName));
                        this.template.append("\n\n\t").append("}");
                    }
                } else {
                    this.appendProperty(key, value, 1);
                }
            }
        }

        this.template.append("}");

        FileX.write(new File(directory, String.format("%s.java", className)), this.template.toString());
    }

    private void appendProperty(String key, Object value, int tabs) {
        String typeName = null;
        String name = StringX.toPascalCase(key);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tabs; i++) {
            sb.append("\t");
        }
        String t =(sb.length()==0) ? "" : sb.toString();

        if(value instanceof JsonData){
            JsonData items = (JsonData) value;
            for(Object o: items){
               if(null!=o){
                   if(o instanceof JSONObject){
                       typeName = JsonData.class.getSimpleName();
                   }
                   else if(o instanceof JSONArray){
                       typeName = JsonData.class.getSimpleName();
                   }
                   else {
                       typeName = o.getClass().getSimpleName();
                   }
                   break;
               }
            }
            if(StringX.isBlank(typeName)){
                typeName = Object.class.getSimpleName();
            }
            this.template.append("\n\n").append(t).append(String.format("public Collection<%s> get%s()", typeName, name)).append("{");

            this.template.append("\n\t").append(t).append(String.format("Collection<%s> results = new ArrayList<>();", typeName));
            this.template.append("\n\t").append(t).append(String.format("JsonData items = this.data.getFromPath(\"%s\");", key));
            this.template.append("\n\t").append(t).append("if(null!=items && items.isJSONArray()){");
            this.template.append("\n\t\t").append(t).append("for(Object o: items){");
            this.template.append("\n\t\t\t").append(t).append("if(null!=o){");
            this.template.append("\n\t\t\t\t").append(t).append(String.format("results.add((%s)o);", typeName));
            this.template.append("\n\t\t\t").append(t).append("}");
            this.template.append("\n\t\t").append(t).append("}");
            this.template.append("\n\t").append(t).append("}");

            this.template.append("\n\t").append(t).append("return results;");
            this.template.append("\n").append(t).append("}");
        }
        else  {
            typeName = value.getClass().getSimpleName();
            this.template.append("\n\n").append(t).append(String.format("public %s %s%s()", typeName, (value instanceof Boolean) ? "is" : "get", name)).append("{");
            this.template.append("\n\t").append(t).append(String.format("return this.data.get%s(\"%s\");", StringX.equals(typeName, JsonData.class.getSimpleName()) ? "FromPath" : typeName, key));
            this.template.append("\n").append(t).append("}");
        }
    }
}
