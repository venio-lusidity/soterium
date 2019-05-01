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

package com.lusidity.domains.book.record;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@SuppressWarnings("unused")
@AtSchemaClass(name="Delete Record", discoverable=false, description="Records all deletes.")
public class DeleteRecord extends BaseDomain
{

// Fields
	public static final int INIT_ORDINAL=2000;
	private static DeleteRecord INSTANCE=null;
	@AtSchemaProperty(name="Vertices Deleted", expectedType=LogEntry.class,
		description="Recordings of all deletions.", limit=10)
	private ElementEdges<LogEntry> entries=null;

// Constructors
	public DeleteRecord()
	{
		super();
	}

	public DeleteRecord(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		String title="System Delete Record";
		DeleteRecord deleteRecord=VertexFactory.getInstance().getByPropertyIgnoreCase(DeleteRecord.class, "title", title);
		if (null==deleteRecord)
		{
			try
			{
				DeleteRecord dr=new DeleteRecord();
				dr.fetchTitle().setValue(title);
				dr.save();
				DeleteRecord.INSTANCE=dr;
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return DeleteRecord.INIT_ORDINAL;
	}

// Methods
	public static DeleteRecord getInstance()
	{
		return DeleteRecord.INSTANCE;
	}

	public void add(LogEntry logEntry)
	{
		if (null!=logEntry)
		{
			boolean added=this.entries.add(logEntry);
			if (!added)
			{
				Environment.getInstance().getReportHandler().warning("Failed to create LogEntry.");
			}
		}
	}

// Getters and setters
	public ElementEdges<LogEntry> getEntries()
	{
		if (null==this.entries)
		{
			this.buildProperty("entries");
		}
		return this.entries;
	}
}
