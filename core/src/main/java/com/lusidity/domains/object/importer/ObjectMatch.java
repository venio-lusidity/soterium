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

package com.lusidity.domains.object.importer;

import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import org.joda.time.DateTime;

@AtSchemaClass(name="Fetched Item", description = "Shows how two objects were matched using some sort of disambiguation.", discoverable = false)
public class ObjectMatch extends BaseDomain
{
	private KeyData<String> key = null;
	private KeyData<String> value = null;
	private KeyData<DateTime> lastSeen = null;
	private KeyDataCollection<DisambiguatedItem> disambiguatedItem = null;

	// Constructors
	//add last seen value
	public ObjectMatch(){super();}
	public ObjectMatch(JsonData dso, Object indexId){super(dso, indexId);}

	public KeyData<String> fetchKey()
	{
		if(null==this.key){
			this.key =new KeyData<>(this, "key", String.class, false, null);
		}
		return this.key;
	}

	public KeyData<String> fetchValue()
	{
		if(null==this.value){
			this.value =new KeyData<>(this, "value", String.class, false, null);
		}
		return this.value;
	}

	public KeyData<DateTime> fetchLastSeen()
	{
		if (null==this.lastSeen)
		{
			this.lastSeen=new KeyData<>(this, "lastSeen", DateTime.class, false, null);
		}
		return this.lastSeen;
	}

	public KeyDataCollection<DisambiguatedItem> fetchMatchedItems()
	{
		if(null==this.disambiguatedItem){
			this.disambiguatedItem = new KeyDataCollection<DisambiguatedItem>(this, "disambiguatedItem", DisambiguatedItem.class, false, false, false, null);
		}
		return this.disambiguatedItem;
	}
}
