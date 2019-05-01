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

package com.lusidity.configuration;

import com.lusidity.data.table.TableView;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;
import org.restlet.data.Method;

import java.io.File;

@SuppressWarnings({
	"UnusedDeclaration",
	"ClassWithTooManyFields"
})
public class TableViewConfiguration extends BaseSoteriumConfiguration
{
	private static final Integer MAX_VIEW_SIZE=500;
	private static final Integer DEFAULT_HOURS_EXPIRE=24;
	private static TableViewConfiguration instance=null;

	// Constructors
	public TableViewConfiguration(File file)
	{
		super(file);
		this.load();
		TableViewConfiguration.instance=this;
	}

// Overrides
	@Override
	public boolean isLoggable(Method method)
	{
		return false;
	}

	// Methods
	public static synchronized TableViewConfiguration getInstance()
	{
		return TableViewConfiguration.instance;
	}

	public Integer getInterval(Class<? extends TableView> cls)
	{
		Integer result=null;
		JsonData items=this.getData().getFromPath("views", "value");
		if ((null!=items) && items.isJSONArray())
		{
			for (Object o : items)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=JsonData.create(o);
					String actual=item.getString("cls");
					if (StringX.equals(cls.getName(), actual))
					{
						result=item.getInteger("interval");
						break;
					}
				}
			}
		}
		return result;
	}

	public boolean isDisabled(Class<? extends TableView> cls)
	{
		Boolean result=true;
		JsonData items=this.getData().getFromPath("views", "value");
		if ((null!=items) && items.isJSONArray())
		{
			for (Object o : items)
			{
				if (o instanceof JSONObject)
				{
					JsonData item=JsonData.create(o);
					String actual=item.getString("cls");
					if (StringX.equals(cls.getName(), actual))
					{
						result=item.getBoolean("disabled");
						break;
					}
				}
			}
		}
		return result;
	}

// Getters and setters
	public int getMaxViewSize()
	{
		return this.getData().getInteger("max_view_size", "value");
	}

	public String getDirectoryPath()
	{
		return this.getData().getString("directory_path", "value");
	}

	public boolean isDisabled()
	{
		return this.getData().getBoolean("disabled", "value");
	}

	public boolean isMemoryCacheEnabled()
	{
		return this.getData().getBoolean("memory_cache_enabled", "value");
	}
}
