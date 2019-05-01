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

package com.lusidity.domains.event;

import com.lusidity.annotations.AtIndexedField;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDataObjectEncoderHandler;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtIndexedField(key = "label", type = String.class)
@AtIndexedField(key = "fact", type = Object.class, indexable = false)
@AtIndexedField(key = "key", type = String.class)
@AtIndexedField(key = "clsType", type = String.class)
@AtSchemaClass(name = "Event Fact", description = "A fact about an event.", discoverable = false)
public class EventFact extends BaseDomain {

    private KeyData<String> key = null;
    private KeyData<String> label = null;
    private KeyData<Class> clsType = null;
    private KeyData<Object> fact = null;

    public EventFact(){
        super();
    }

    public EventFact(JsonData dso, Object indexId){
        super(dso, indexId);
    }

    public KeyData<String> fetchKey() {
        if(null==this.key){
            this.key = new KeyData<>(this, "key", String.class, false, null);
        }
        return this.key;
    }

    public KeyData<String> fetchLabel() {
        if(null==this.label){
            this.label = new KeyData<>(this, "label", String.class, false, null);
        }
        return this.label;
    }

    public KeyData<Object> fetchFact() {
        if(null==this.fact){
            this.fact = new KeyData<>(this, "fact", Object.class, true, null, new KeyDataObjectEncoderHandler());
        }
        return this.fact;
    }

    public KeyData<Class> fetchClsType() {
        if(null==this.clsType){
            this.clsType = new KeyData<>(this, "clsType", Class.class, true, null);
        }
        return this.clsType;
    }
}
