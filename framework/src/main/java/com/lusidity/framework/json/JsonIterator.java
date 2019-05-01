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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class JsonIterator
	implements Iterator<Object>
{
	private final JSONArray jsonArray;
	private int index;
	private final int count;

	public JsonIterator(JSONArray jsonArray)
	{
		super();
		this.jsonArray = jsonArray;
		this.index = 0;
		this.count = (null != this.jsonArray) ? this.jsonArray.length() : 0;
	}

	@Override
	public boolean hasNext()
	{
		return (this.jsonArray != null) && (this.index <= (this.count - 1));
	}

	@Override
	public Object next()
	{
		Object result;

		if (! this.hasNext())
		{
			throw new NoSuchElementException("No more items.");
		}

		try
		{
			result = this.jsonArray.get(this.index++);
		} catch (JSONException ignored)
		{
			result = null;
		}

		return result;
	}

	@Override
	public void remove()
	{
		throw new RuntimeException("remove() is not supported.");
	}
}
