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

package com.lusidity.apollo.elasticSearch;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.system.primitives.Primitive;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("RedundantFieldInitialization")
public class IndexConfiguration
{
	private static IndexConfiguration instance=null;
	private final File file;
	private final Collection<Class<? extends DataVertex>> allowedTypes=new ArrayList<>();
	private final Collection<Class<? extends DataVertex>> blockedTypes=new ArrayList<>();
	private JsonData sourceData=null;
	private EsConfiguration config=null;
	private JsonData mapping=null;
	private JsonData common=null;

	// Constructors
	public IndexConfiguration(File file, EsConfiguration config)
	{
		super();
		this.file=file;
		this.config=config;
		this.transform();
		IndexConfiguration.instance=this;
	}

	private void transform()
	{
		this.sourceData=new JsonData(this.file);
		if (this.sourceData.isValid())
		{

			if (this.sourceData.hasKey("mapping"))
			{
				this.mapping=this.sourceData.getFromPath("mapping");
			}

			if (this.sourceData.hasKey("common"))
			{
				this.common=this.sourceData.getFromPath("common");
			}

			if (this.sourceData.hasKey("allowedTypes"))
			{
				JsonData types=this.sourceData.getFromPath("allowedTypes");
				if ((null!=types) && types.isJSONArray())
				{
					for (Object o : types)
					{
						if (o instanceof String)
						{
							String type=String.valueOf(o);
							Class cls=ClassX.getClassFromNameSafely(type);
							if ((null!=cls) && (Objects.equals(cls, Primitive.class)))
							{
								Collection<Class<? extends Primitive>> primitives=Primitive.getPrimitives();
								for (Class<? extends Primitive> primitive : primitives)
								{
									this.allowedTypes.add(primitive);
								}
							}
							else if ((null!=cls) && ClassX.isKindOf(cls, DataVertex.class))
							{
								//noinspection unchecked
								this.allowedTypes.add(cls);
							}
						}
					}
				}
			}

			if (this.sourceData.hasKey("blockedTypes"))
			{
				JsonData types=this.sourceData.getFromPath("blockedTypes");
				if ((null!=types) && types.isJSONArray())
				{
					for (Object o : types)
					{
						if (o instanceof String)
						{
							String type=String.valueOf(o);
							Class cls=ClassX.getClassFromNameSafely(type);
							if ((null!=cls) && (Objects.equals(cls, Primitive.class)))
							{
								Collection<Class<? extends Primitive>> primitives=Primitive.getPrimitives();
								for (Class<? extends Primitive> primitive : primitives)
								{
									this.blockedTypes.add(primitive);
								}
							}
							else if ((null!=cls) && ClassX.isKindOf(cls, DataVertex.class))
							{
								//noinspection unchecked
								this.blockedTypes.add(cls);
							}
						}
					}
				}
			}
		}
	}

	public EsIndexEngine start()
	{
		EsIndexEngine result=null;
		if (!this.allowedTypes.isEmpty())
		{
			try
			{
				result=new EsIndexEngine(this);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
		else
		{
			Environment.getInstance().getReportHandler().severe("The index configuration must have at least one allowed class.");
		}
		return result;
	}

	public Boolean isImmediate()
	{
		return this.config.isImmediatelyAvailable();
	}

	// Getters and setters
	protected static IndexConfiguration getInstance()
	{
		return IndexConfiguration.instance;
	}

	public JsonData getSourceData()
	{
		return this.sourceData;
	}

	public EsConfiguration getConfig()
	{
		return this.config;
	}

	public JsonData getMapping()
	{
		JsonData result=null;
		try
		{
			// keep original by only passing a clone
			result=(null!=this.mapping) ? this.mapping.clone() : null;
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	public void setMapping(JsonData mapping)
	{
		this.mapping=mapping;
	}

	public String getName()
	{
		return this.file.getName();
	}

	public Collection<Class<? extends DataVertex>> getAllowedTypes()
	{
		return this.allowedTypes;
	}

	public Collection<Class<? extends DataVertex>> getBlockedTypes()
	{
		return this.blockedTypes;
	}

	public JsonData getCommon()
	{
		JsonData result=null;
		try
		{
			// keep original by only passing a clone
			result=(null!=this.common) ? this.common.clone() : null;
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}
}
