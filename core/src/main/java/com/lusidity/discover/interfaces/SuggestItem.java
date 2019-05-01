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

package com.lusidity.discover.interfaces;

import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.UserCredentials;

import java.net.URI;

public class SuggestItem extends BaseItem
{
	private String otherId=null;
	private int occurrences=0;

// Constructors
	public SuggestItem(String phrase, DataVertex vertex, UserCredentials credentials, String key, Object value, int hits)
	{
		super(phrase, vertex, credentials, key, value, hits);
	}

// Overrides
	@Override
	public JsonData toJson()
	{
		JsonData result=super.toJson();
		result.put("otherId", this.getOtherId());
		result.put("occurrences", this.getOccurrences());
		return result;
	}

	public String getOtherId()
	{
		return this.otherId;
	}

	public int getOccurrences()
	{
		return this.occurrences;
	}

	// Methods
	public static SuggestItem getSuggestion(ApolloVertex vertex, UserCredentials credentials, String phrase, String childKey, Class childType, String parentKey)
	{
		SuggestItem result = null;
		String title = vertex.fetchTitle().getValue();
		if(!StringX.isBlank(title))
		{
			if (StringX.isBlank(childKey) || (null==childType))
			{
				String value=vertex.getVertexData().getString(parentKey);
				if (!StringX.isBlank(value))
				{
					title=value;
				}
			}

			if (!StringX.equalsIgnoreCase(title, vertex.fetchTitle().getValue()))
			{
				title=String.format("%s :[%s]", title, vertex.fetchTitle().getValue());
			}
			result=new SuggestItem(phrase, vertex, credentials, null, null, 0);
			result.build(title, vertex.getUri(), vertex.fetchId().getValue(), vertex.getClass());
		}
		return result;
	}

	public void build(String title, URI uri, String otherId, Class<? extends DataVertex> vertexType)
	{
		super.build(title, "", uri, null, 1.0, vertexType, this.getHits());
	}

	public static SuggestItem getSuggestion(ApolloVertex vertex, UserCredentials credentials, String phrase)
	{
		SuggestItem result = null;
		String title = vertex.fetchTitle().getValue();
		if(!StringX.isBlank(title))
		{
			if (!StringX.equalsIgnoreCase(title, vertex.fetchTitle().getValue()))
			{
				title=String.format("%s :[%s]", title, vertex.fetchTitle().getValue());
			}
			result=new SuggestItem(phrase, vertex, credentials, null, null, 0);
			result.build(title, vertex.getUri(), vertex.fetchId().getValue(), vertex.getClass());
		}
		return result;
	}

	public synchronized void addOccurrence()
	{
		this.occurrences+=1;
	}
}
