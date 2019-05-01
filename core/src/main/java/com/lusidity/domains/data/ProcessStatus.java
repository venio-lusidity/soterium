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

package com.lusidity.domains.data;

import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.data.importer.ImporterHistory;
import com.lusidity.domains.system.primitives.SynchronizedInteger;
import com.lusidity.domains.system.primitives.SynchronizedLong;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.math.MathX;

import java.util.Collection;

@AtSchemaClass(name="Process Status", discoverable = false)
public class ProcessStatus extends BaseDomain
{
	private KeyData<SynchronizedInteger> matches=null;
	private KeyData<SynchronizedInteger> skipped=null;
	private KeyData<SynchronizedInteger> errors=null;

	private KeyData<SynchronizedInteger> total=null;
	private KeyData<SynchronizedInteger> processed=null;

	private KeyData<SynchronizedInteger> queries=null;
	private KeyData<SynchronizedInteger> created=null;
	// Could be used for a targeted object.
	private KeyData<SynchronizedInteger> primary=null;
	private KeyData<SynchronizedInteger> updated=null;
	private KeyData<SynchronizedInteger> deleted=null;
	// Child objects being processed within a processed object.
	private KeyData<SynchronizedInteger> innerProcessed=null;
	private KeyData<SynchronizedLong> fileSize=null;
	
	private String message="processing";
	private transient ImporterHistory history = null;

	// Constructors
	public ProcessStatus()
	{
		super();
	}

	public ProcessStatus(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Methods
	@SuppressWarnings("unused")
	public static ProcessStatus getRandom()
	{
		ProcessStatus result = new ProcessStatus();
		result.fetchTotal().getValue().add(MathX.getRandomNumber());
		result.fetchCreated().getValue().add(MathX.getRandomNumber());
		result.fetchInnerProcessed().getValue().add(MathX.getRandomNumber());
		result.fetchPrimary().getValue().add(MathX.getRandomNumber());
		result.fetchDeleted().getValue().add(MathX.getRandomNumber());
		result.fetchMatches().getValue().add(MathX.getRandomNumber());
		result.fetchProcessed().getValue().add(MathX.getRandomNumber());
		result.fetchQueries().getValue().add(MathX.getRandomNumber());
		result.fetchSkipped().getValue().add(MathX.getRandomNumber());
		result.fetchErrors().getValue().add(MathX.getRandomNumber());
		result.fetchUpdated().getValue().add(MathX.getRandomNumber());
		return result;
	}

	public KeyData<SynchronizedInteger> fetchTotal()
	{
		if (null==this.total)
		{
			this.total=new KeyData<>(this, "total", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.total;
	}

	public KeyData<SynchronizedInteger> fetchCreated()
	{
		if (null==this.created)
		{
			this.created=new KeyData<>(this, "created", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.created;
	}

	public KeyData<SynchronizedInteger> fetchInnerProcessed()
	{
		if (null==this.innerProcessed)
		{
			this.innerProcessed=new KeyData<>(this, "innerProcessed", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.innerProcessed;
	}

	public KeyData<SynchronizedInteger> fetchPrimary()
	{
		if (null==this.primary)
		{
			this.primary=new KeyData<>(this, "primary", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.primary;
	}

	public KeyData<SynchronizedInteger> fetchDeleted()
	{
		if (null==this.deleted)
		{
			this.deleted=new KeyData<>(this, "deleted", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.deleted;
	}

	public KeyData<SynchronizedInteger> fetchMatches()
	{
		if (null==this.matches)
		{
			this.matches=new KeyData<>(this, "matches", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.matches;
	}

	public KeyData<SynchronizedInteger> fetchProcessed()
	{
		if (null==this.processed)
		{
			this.processed=new KeyData<>(this, "processed", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.processed;
	}

	public KeyData<SynchronizedInteger> fetchQueries()
	{
		if (null==this.queries)
		{
			this.queries=new KeyData<>(this, "queries", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.queries;
	}

	public KeyData<SynchronizedInteger> fetchSkipped()
	{
		if (null==this.skipped)
		{
			this.skipped=new KeyData<>(this, "skipped", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.skipped;
	}

	public KeyData<SynchronizedInteger> fetchErrors()
	{
		if (null==this.errors)
		{
			this.errors=new KeyData<>(this, "errors", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.errors;
	}

	public KeyData<SynchronizedInteger> fetchUpdated()
	{
		if (null==this.updated)
		{
			this.updated=new KeyData<>(this, "updated", SynchronizedInteger.class, false, new SynchronizedInteger());
		}
		return this.updated;
	}

	public synchronized void combine(ProcessStatus processStatus)
	{
		if(null!=processStatus)
		{
			this.combine(this.fetchTotal().getValue(), processStatus.fetchTotal().getValue());
			this.combine(this.fetchCreated().getValue(), processStatus.fetchCreated().getValue());
			this.combine(this.fetchInnerProcessed().getValue(), processStatus.fetchInnerProcessed().getValue());
			this.combine(this.fetchPrimary().getValue(), processStatus.fetchPrimary().getValue());
			this.combine(this.fetchDeleted().getValue(), processStatus.fetchDeleted().getValue());
			this.combine(this.fetchMatches().getValue(), processStatus.fetchMatches().getValue());
			this.combine(this.fetchProcessed().getValue(), processStatus.fetchProcessed().getValue());
			this.combine(this.fetchQueries().getValue(), processStatus.fetchQueries().getValue());
			this.combine(this.fetchSkipped().getValue(), processStatus.fetchSkipped().getValue());
			this.combine(this.fetchErrors().getValue(), processStatus.fetchErrors().getValue());
			this.combine(this.fetchUpdated().getValue(), processStatus.fetchUpdated().getValue());
			this.combine(this.fetchFileSize().getValue(), processStatus.fetchFileSize().getValue());
		}
	}

	/**
	 * Reset all counters to zero.
	 */
	@SuppressWarnings("unused")
	public synchronized void resetAll(){
		this.fetchTotal().getValue().reset();
		this.fetchCreated().getValue().reset();
		this.fetchInnerProcessed().getValue().reset();
		this.fetchPrimary().getValue().reset();
		this.fetchDeleted().getValue().reset();
		this.fetchMatches().getValue().reset();
		this.fetchProcessed().getValue().reset();
		this.fetchQueries().getValue().reset();
		this.fetchSkipped().getValue().reset();
		this.fetchErrors().getValue().reset();
		this.fetchUpdated().getValue().reset();
		this.fetchFileSize().getValue().reset();
	}

	private void combine(SynchronizedInteger a, SynchronizedInteger b)
	{
		try
		{
			if ((null!=a) && (null!=b))
			{
				a.add(b.getCount());
			}
		}
		catch (Exception ignored){}
	}

	private void combine(SynchronizedLong a, SynchronizedLong b)
	{
		try
		{
			if ((null!=a) && (null!=b))
			{
				a.add(b.getCount());
			}
		}
		catch (Exception ignored){}
	}

	public KeyData<SynchronizedLong> fetchFileSize()
	{
		if (null==this.fileSize)
		{
			this.fileSize=new KeyData<>(this, "fileSize", SynchronizedLong.class, false, new SynchronizedLong());
		}
		return this.fileSize;
	}

	public synchronized void setMessage(String format, Object... values)
	{
		this.message=String.format(format, values);
	}

	public void createdOrUpdated(boolean isNew)
	{
		if (isNew)
		{
			this.fetchCreated().getValue().increment();
		}
		else
		{
			this.fetchUpdated().getValue().increment();
		}
	}

	public JsonData toJson()
	{
		JsonData result=new JsonData();

		result.put("processed", JsonData.makeLabel("Processed", this.fetchProcessed().getValue().fetchCount().getValue()));
		result.put("innerProcessed", JsonData.makeLabel("Children Processed", this.fetchInnerProcessed().getValue().fetchCount().getValue()));
		result.put("matches", JsonData.makeLabel("Matches", this.fetchMatches().getValue().fetchCount().getValue()));
		result.put("skipped", JsonData.makeLabel("Skipped", this.fetchSkipped().getValue().fetchCount().getValue()));
		result.put("errors", JsonData.makeLabel("Errors", this.fetchErrors().getValue().fetchCount().getValue()));
		result.put("created", JsonData.makeLabel("Created", this.fetchCreated().getValue().fetchCount().getValue()));
		result.put("primary", JsonData.makeLabel("Primary", this.fetchPrimary().getValue().fetchCount().getValue()));
		result.put("updated", JsonData.makeLabel("Updated", this.fetchUpdated().getValue().fetchCount().getValue()));
		result.put("deleted", JsonData.makeLabel("Deleted", this.fetchDeleted().getValue().fetchCount().getValue()));
		result.put("queries", JsonData.makeLabel("Queries", this.fetchQueries().getValue().fetchCount().getValue()));
		result.put("message", JsonData.makeLabel("Message", this.getMessage()));
		result.put("total", JsonData.makeLabel("Total", this.fetchTotal().getValue().fetchCount().getValue()));
		JsonData ext = this.getVertexData().getFromPath("ext");
		if(null!=ext){
			Collection<String> keys = ext.keys();
			for(String key: keys){
				JsonData data = ext.getFromPath(key);
				String label = data.getString("label");
				String desc = data.getString("desc");
				int value = data.getInteger("value");
				result.put(key, JsonData.makeLabel(label, value, desc));
			}
		}
		JsonData results=JsonData.createArray();
		result.put("results", results);
		return result;
	}

	public synchronized String getMessage()
	{
		return this.message;
	}

	public synchronized void setMessage(String message)
	{
		this.message=message;
	}

	public synchronized void increment(String key, String label, String description)
	{
		JsonData data = this.getVertexData().getFromPath("ext", key);
		if(null==data){
			data = JsonData.createObject().put("value", 0).put("label", label).put("desc", description);
			this.getVertexData().put("ext", JsonData.createObject().put(key, data));
		}
		int value = data.getInteger(0, "value");
		data.update("value", (value+1));
	}

	// Getters and setters
	public int getCombinedTotal()
	{
		return this.fetchProcessed().getValue().fetchCount().getValue()
		           +this.fetchCreated().getValue().fetchCount().getValue()
		           +this.fetchQueries().getValue().fetchCount().getValue()
		           +this.fetchUpdated().getValue().fetchCount().getValue()
		           +this.fetchDeleted().getValue().fetchCount().getValue()
			       +this.fetchInnerProcessed().getValue().fetchCount().getValue();
	}

	public ImporterHistory getHistory(){
		return this.history;
	}

	public void setHistory(ImporterHistory history)
	{
		this.history=history;
	}
}
