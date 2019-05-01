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
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtIndexedField(key = "/event/event_fact/facts", type = EventFact.class)
@AtSchemaClass(name = "Asset Fact", description = "Facts about a related asset.", discoverable = false)
public class Itinerary extends BaseDomain
{
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private KeyDataCollection<EventFact> facts;

    public Itinerary(){
        super();
    }

    public Itinerary(JsonData dso, Object indexId){
        super(dso, indexId);
    }

    public KeyDataCollection<EventFact> fetchFacts() {
        if(null==this.facts){
            this.facts = new KeyDataCollection<>(this, "facts", EventFact.class, false, false, false, null);
        }
        return this.facts;
    }
}
