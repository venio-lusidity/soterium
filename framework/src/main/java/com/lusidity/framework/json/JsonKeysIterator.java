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

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public
class JsonKeysIterator
	implements Iterator<String>
{
	private final Collection<String> keys;
    private final JsonData jsonData;
	private int index;
	private final int count;

	public JsonKeysIterator(JsonData jsonData)
	{
		super();
        this.jsonData = jsonData;
        this.keys = this.jsonData.keys();
		this.index = 0;
		this.count = this.keys.size();
	}

	@Override
	public
	boolean hasNext()
	{
		return (this.index <= (this.count-1));
	}

	@Override
	public
	String next()
	{
		String result;

		if (!this.hasNext())
		{
			throw new NoSuchElementException("No more items.");
		}

		try
		{
            result= (String) CollectionUtils.get(this.keys, this.index++);
		}
		catch (Exception ignored)
		{
			result=null;
		}

		return result;
	}

	@Override
	public
	void remove()
	{
		throw new RuntimeException("remove() is not supported.");
	}
}
