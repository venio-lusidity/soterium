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

package com.lusidity.discover.providers.wikipedia;

import com.lusidity.Environment;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class WikipediaInfoBox {

    private static Pattern PROPERTY_PATTERN = Pattern.compile("([^\\s].*)(\\s)=.*");
    private final String content;

    private String type = null;
    private String infoBox = null;
    private JsonData data = JsonData.createObject();
    private String title = null;
    private String description = null;
    private Collection<URI> images = new ArrayList<>();
    private URI homepage = null;

    public WikipediaInfoBox(String content) {
        super();
        this.content = content;
        this.transform(this.getInfoBox(), 0);
    }

    public void load(){
        if(!StringX.isBlank(this.content)){
            String[] lines = StringX.split(this.content, "\\\n");
            if(null!=lines){
                int zeroed = 0;
                int group = 0;
                StringBuilder sb = new StringBuilder();
                for(String line: lines){
                    try {
                        if (StringX.startsWith(line, "{") && !StringX.endsWith(line, "}")) {
                            zeroed++;
                        } else if (StringX.startsWith(line, "}") && !StringX.startsWith(line, "{")) {
                            zeroed--;
                        }
                        sb.append(line);
                        if (zeroed == 0) {
                            group++;
                            this.transform(sb.toString(), group);
                            sb = new StringBuilder();
                        }
                    }
                    catch (Exception ex){
                        Environment.getInstance().getReportHandler().warning(ex);
                    }
                }
            }
        }
    }

    public void transform(String box, int group){
        if(!StringX.isBlank(box)){
            String key = null;
            JsonData result = JsonData.createObject();
            String[] lines = StringX.split(box, "\\\n");
            if(null!=lines){
                int on = 0;
                int zeroed = 0;
                String inner = null;
                for(String line: lines){
                    try {
                        if (on == 0) {
                            String[] parts = StringX.split(line, ' ');
                            String t = this.clean(parts[parts.length - 1]);
                            result.put("groupType", t);
                            key = StringUtils.replace(parts[0], "{{", "");
                            if(StringX.contains(key, "|")){
                                parts = StringX.split(key, "|");
                                key = parts[0];
                            }
                        } else if (StringX.startsWith(line, "{") && !StringX.endsWith(line, "}")) {
                            zeroed++;
                            inner = line;
                        } else if (StringX.startsWith(line, "}") && !StringX.startsWith(line, "{")) {
                            zeroed--;
                            inner += line;
                            this.parseInner(inner);
                            inner = null;
                        }
                        if (zeroed == 0) {
                            line = StringX.removeStart(line, "|").trim();
                            int count = StringX.countMatches(line, "|");

                            if (count > 1) {
                                String[] parts = StringX.split(line, "|");
                                for (String part : parts) {
                                    this.parseObject(line, result);
                                }
                            } else {
                                this.parseObject(line, result);
                            }
                        }
                    }
                    catch (Exception ex){
                        Environment.getInstance().getReportHandler().warning(ex);
                    }
                    on++;
                }
                if(!result.keys().isEmpty()){
                    key = StringX.isBlank(key) ? String.format("group_%s", group) : key;
                    this.data.put(key, result);
                }
            }
        }
    }

    private void parseObject(String line, JsonData working) {
        if(!StringUtils.isBlank(line) && !StringUtils.equals(line, "|")
                && !StringUtils.equals(line, "{") && !StringUtils.equals(line, "}")
                && !StringUtils.equals(line, "[") && !StringUtils.equals(line, "]")) {
            try {
                int sum = StringX.countMatches(line, "|");
                if (sum > 0 && !WikipediaInfoBox.PROPERTY_PATTERN.matcher(line).matches()) {
                    String[] parts = StringX.split(line, "|");
                    for (String part : parts) {
                        this.parseObject(part.trim(), working);
                    }
                } else {
                    String[] parts = StringX.split(line, "=");
                    if (parts.length == 2 && !StringX.isBlank(parts[0]) && !StringX.isBlank(parts[1])) {
                        working.put(parts[0].trim(), this.parseInner(parts[1].trim()));
                    } else if (WikipediaInfoBox.PROPERTY_PATTERN.matcher(line).matches()) {
                        String str = StringX.replace(line, String.format("%s =", parts[0].trim()), "").trim();
                        working.put(parts[0].trim(), this.parseInner(str));
                    } else {
                        working.put("unknown", this.parseInner(line));
                    }
                }
            } catch (Exception ex) {
                Environment.getInstance().getReportHandler().severe(ex);
            }
        }
    }

    private Object parseInner(String inner) {
        Object result = inner;
        if(StringX.startsWith(inner, "{") || StringX.startsWith(inner, "]")){
            result = this.getJson(inner);
        }
        else if(StringX.contains(inner, "<ref")){
            String[] parts = StringX.split(inner, "<");
            if(parts.length>1){
                for(int i=0;i<parts.length;i++){
                    if(!StringX.isBlank(parts[i]) && !StringUtils.startsWith(parts[i], "ref ")){
                        result = parts[i];
                        break;
                    }
                }
            }
        }
        if(result instanceof String){
           result = this.clean(result.toString());
        }
        return result;
    }

    private String clean(String str) {
        String result = StringX.replace(str, "[[", "");
        result = StringX.replace(result, "]]", "");
        result = StringX.replace(result, "{{", "");
        result = StringX.replace(result, "}}", "");
        return result;
    }

    private Object getJson(String inner) {
        JsonData results = StringX.startsWith(inner, "{") ? JsonData.createObject() : JsonData.createArray();
        if(!StringX.isBlank(inner)){
            if(StringX.startsWith(inner, "{")){
                String[] parts = StringX.split(inner, "=");
                if(parts.length==2 && !StringX.isBlank(parts[0]) && !StringX.isBlank(parts[1])){
                    results.put(parts[0].trim(), this.parseInner(parts[1].trim()));
                }
            }
            else{
                String[] parts = StringX.split(inner, "|");
                if(null!=parts && parts.length>0){
                    for(String part: parts){
                        part = this.removeBrackets(part);
                        if(!StringX.isBlank(part)&&!StringX.equalsIgnoreCase(part, "nowrap")){
                            results.put(part);
                        }
                    }
                }
            }
        }
        return results;
    }

    private String removeBrackets(String str) {
        String result = str.trim();
        if(!StringX.isBlank(result)) {
            result = StringX.replace(result, "{", "");
            result = StringX.replace(result, "}", "");
            result = StringX.replace(result, "[", "");
            result = StringX.replace(result, "]", "");
        }
        return result;
    }

    public String getContent() {
        return this.content;
    }

    public JsonData getData() {
        return this.data;
    }

    public String getDescription() {
        return this.description;
    }

    public URI getHomepage() {
        return this.homepage;
    }

    public Collection<URI> getImages() {
        return this.images;
    }

    public String getTitle() {
        String result = null;
        if(null!=this.data){
            result = this.data.getString("conventional_long_name");
            if(StringX.isBlank(result)){
                result = this.data.getString("common_name");
            }
        }
        return result ;
    }

    public String getType(){
        return this.type;
    }

    public String getInfoBox() {
        if(StringX.isBlank(this.infoBox)){
            int len = this.content.length();
            int start = this.content.indexOf("{{Infobox");
            int end = 0;
            if(start>0){
                int zeroed = 0;
                for(int i=start;i<len;i++){
                    String c = StringX.substring(this.content, i, i+1);
                    if(StringX.equals(c, "{")){
                        zeroed++;
                    }
                    else if(StringX.equals(c, "}")){
                        zeroed--;
                    }
                    if(zeroed<=0){
                        end = i;
                        break;
                    }
                }
                this.infoBox = StringX.substring(this.content, start, end+1);
            }
        }
        return this.infoBox;
    }
}
