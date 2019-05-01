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

package com.lusidity.domains.object.edge;

import com.lusidity.domains.object.EdgeData;
import org.joda.time.DateTime;

public class TermEdgeData extends EdgeData
{
	private DateTime expiresOn=null;
	private boolean expires=false;

// Constructors
	public TermEdgeData()
	{
		super();
	}

	public TermEdgeData(String fromLabel, long fromOrdinal, String toLabel, long toOrdinal, boolean expires, DateTime expiresOn)
	{
		super(fromLabel, fromOrdinal, toLabel, toOrdinal);
		this.expires=expires;
		this.expiresOn=expiresOn;
	}

	public TermEdgeData(String fromLabel, String toLabel, boolean expires, DateTime expiresOn)
	{
		super(fromLabel, toLabel);
		this.expires=expires;
		this.expiresOn=expiresOn;
	}

	public TermEdgeData(long fromOrdinal, long toOrdinal, boolean expires, DateTime expiresOn)
	{
		super(fromOrdinal, toOrdinal);
		this.expires=expires;
		this.expiresOn=expiresOn;
	}

// Getters and setters
	public DateTime getExpiresOn()
	{
		return this.expiresOn;
	}

	public void setExpiresOn(DateTime expiresOn)
	{
		this.expiresOn=expiresOn;
	}

	public boolean isExpires()
	{
		return this.expires;
	}

	public void setExpires(boolean expires)
	{
		this.expires=expires;
	}
}
