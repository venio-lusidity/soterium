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

package com.lusidity.cache;

import com.lusidity.data.DataVertex;
import com.lusidity.framework.exceptions.ApplicationException;

import java.net.URI;

/**
 * This cache will never do anything and prevents checks from having to be made if real cache is used.
 */
public class DummyCache extends BaseCache
{

// Overrides
	@Override
	public void init()
	{

	}

	@Override
	public void stop()
	{

	}

	@Override
	protected long recount()
	{
		return 0;
	}

	@Override
	public void put(DataVertex entry)
		throws ApplicationException
	{

	}

	@Override
	public void checkAndLoad()
	{

	}

	@Override
	public <T extends DataVertex> T get(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key)
		throws ApplicationException
	{
		return null;
	}

	@Override
	public <T extends DataVertex> T get(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, URI key)
		throws ApplicationException
	{
		return null;
	}

	@Override
	public void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, String entry)
		throws ApplicationException
	{

	}

	@Override
	public void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key, DataVertex entry)
		throws ApplicationException
	{

	}

	@Override
	public void put(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException
	{

	}

	@Override
	public void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, DataVertex entry)
		throws ApplicationException
	{

	}

	@Override
	public void remove(Class<? extends DataVertex> store, Class<? extends DataVertex> partitionType, String key)
		throws ApplicationException
	{

	}

	@Override
	public void resetCache()
	{

	}
}
