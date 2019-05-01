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

package com.lusidity.discover.tasks;

import com.lusidity.discover.DiscoveryItems;
import com.lusidity.discover.DiscoveryProvider;
import com.lusidity.system.security.UserCredentials;

import java.util.concurrent.Callable;

public class ProviderTask implements Callable<DiscoveryItems>
{

	private final DiscoveryProvider provider;
	private final String phrase;
	private final int start;
	private final int limit;
	private final boolean suggest;
	private final UserCredentials credentials;

	// Constructors
	public ProviderTask(UserCredentials credentials, DiscoveryProvider provider, String phrase, boolean suggest, int start, int limit)
	{
		super();
		this.provider = provider;
		this.phrase = phrase;
		this.start = start;
		this.limit = limit;
		this.suggest = suggest;
		this.credentials = credentials;
	}

	// Overrides
	@Override
	public DiscoveryItems call()
		throws Exception
	{
		return this.provider.discover(this.phrase, this.suggest, this.start, this.limit, this.credentials);
	}
}
