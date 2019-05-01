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

import com.lusidity.Environment;
import com.lusidity.annotations.AtIndexedField;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtIndexedField(key = "/event/itinerary/itineraries", type = Itinerary.class)
@AtSchemaClass(name = "Event", description = "An occurrence of something happening.", discoverable = true)
public class Event extends BaseDomain
{
	private KeyDataCollection<Itinerary> itineraries = null;

	public Event(){
		super();
	}

	public Event(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	public KeyDataCollection<Itinerary> fetchItineraries() {
		if(null==this.itineraries){
			this.itineraries = new KeyDataCollection<>(this, "itineraries", EventFact.class, false, false, false, null);
		}
		return this.itineraries;
	}

	@Override
	public void initialize()
		throws Exception
	{
		Environment.getInstance().getIndexStore().drop(Event.class, Event.class);
		super.initialize();
	}
}
