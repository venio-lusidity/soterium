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

package com.lusidity.data.table;


import com.lusidity.Environment;
import com.lusidity.configuration.TableViewConfiguration;
import com.lusidity.framework.java.ObjectX;
import com.lusidity.framework.text.StringX;

import java.io.File;
import java.util.Map;

public abstract class TableView
{
	private boolean running=false;

// Constructors
	public TableView()
	{
		super();
		this.makeDirectory();
	}

	private void makeDirectory()
	{
		try
		{
			File file=new File(this.getDirectory());
			if (!file.exists())
			{
				file.mkdirs();
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	protected String getDirectory()
	{
		return String.format("%s%s%s", TableViewConfiguration.getInstance().getDirectoryPath(), File.separator,
			StringX.replace(this.getClass().getName(), ".", "_").toLowerCase()
		);
	}

	public abstract Object make(Object... args);

	public boolean contains(String key)
	{
		boolean result=false;
		if (TableViewConfiguration.getInstance().isMemoryCacheEnabled())
		{
			result=this.getCache().containsKey(key);
		}
		else
		{
			try
			{
				File file=new File(this.getFileName(key));
				result=file.exists();
			}
			catch (Exception ignored)
			{
			}
		}
		return result;
	}

	public abstract Map<String, Object> getCache();

	private String getFileName(String key)
	{
		return String.format("%s%s%s.ser", this.getDirectory(), File.separator, key);
	}

	public boolean load(String key)
	{
		Object result=this.get(key);
		return (null!=result);
	}

	public Object get(String key)
	{
		Object result=null;
		if (TableViewConfiguration.getInstance().isMemoryCacheEnabled())
		{
			result=this.getCache().get(key);
		}
		if (null==result)
		{
			String fileName=this.getFileName(key);
			result=ObjectX.readObject(fileName);
			if ((null!=result) && TableViewConfiguration.getInstance().isMemoryCacheEnabled())
			{
				this.store(key, result);
			}
		}
		return result;
	}

	public void store(String key, Object value)
	{
		if (TableViewConfiguration.getInstance().isMemoryCacheEnabled())
		{
			this.getCache().put(key, value);
		}
		String fileName=this.getFileName(key);
		ObjectX.writeObject(fileName, value);
	}

	public abstract void checkForUpdates();

	public abstract void removeAll();

	public void remove(String key)
	{
		this.getCache().remove(key);
	}

	public abstract void warmUp(boolean force);

// Getters and setters
	public boolean isRunning()
	{
		return this.running;
	}


	public void setRunning(boolean running)
	{
		this.running=running;
	}

	public abstract String getTitle();

	public abstract String getDescription();
}
