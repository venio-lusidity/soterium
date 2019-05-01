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

package com.lusidity.workers.importer;


import com.lusidity.process.BaseProgressHandler;
import com.lusidity.process.IPreprocessor;
import org.joda.time.DateTime;

import java.io.File;

public abstract class BaseImporter extends BaseProgressHandler
{
	private DateTime started = DateTime.now();

	// Constructors
	public BaseImporter(int maxThreads, int maxItems)
	{
		super(maxThreads, maxItems);
	}

	public BaseImporter(int maxThreads, int maxItems, int maxWait)
	{
		super(maxThreads, maxItems, maxWait);
	}

	public BaseImporter(int maxThreads, int maxItems, int maxBatch, int maxWait)
	{
		super(maxThreads, maxItems, maxBatch, maxWait);
	}

// Overrides
	@Override
	public File writeExceptionReport()
	{
		return null;
	}

// Getters and setters
	public abstract IPreprocessor getPreprocessor();

	public abstract boolean isRecycle();

	public DateTime getStarted()
	{
		return this.started;
	}
}
