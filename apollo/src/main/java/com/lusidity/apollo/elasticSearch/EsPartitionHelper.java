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

import com.google.common.collect.Iterables;
import com.lusidity.Environment;
import com.lusidity.annotations.AtIndexedField;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import org.joda.time.DateTime;

import java.net.URI;
import java.util.*;

public class EsPartitionHelper
{
	static final Map<Class, JsonData> SCHEMA_MAP=new HashMap<>();

	static
	{
		EsPartitionHelper.SCHEMA_MAP.put(Object.class, new JsonData("{\"type\": \"string\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(String.class, new JsonData("{\"type\": \"string\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(Boolean.class, new JsonData("{\"type\": \"boolean\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(boolean.class, new JsonData("{\"type\": \"boolean\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(Double.class, new JsonData("{\"type\": \"double\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(double.class, new JsonData("{\"type\": \"double\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(Float.class, new JsonData("{\"type\": \"float\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(float.class, new JsonData("{\"type\": \"float\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(Long.class, new JsonData("{\"type\": \"long\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(long.class, new JsonData("{\"type\": \"long\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(DateTime.class, new JsonData("{\"type\": \"long\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(Integer.class, new JsonData("{\"type\": \"integer\"}"));
		EsPartitionHelper.SCHEMA_MAP.put(int.class, new JsonData("{\"type\": \"integer\"}"));
	}

	private EsPartitionHelper()
	{
	}

	/**
	 * Get JSON mapping structure for a field annotated with an @{link AtIndexedField} annotation.
	 *
	 * @param indexedField @{link AtIndexedField} annotation describing the field to be indexed.
	 * @return JSON mapping structure, in ElasticSearch format.
	 */
	// Methods
	public static JsonData getPropertyIndexMapping(AtIndexedField indexedField)
	{
		Class cls=indexedField.type();
		JsonData result;

		if (indexedField.indexable() && (cls.equals(String.class) || cls.equals(URI.class) || ClassX.isKindOf(cls, Enum.class)))
		{
			JsonData fields=JsonData.createObject();
			Collection<BaseQueryBuilder.StringTypes> analyzers=Arrays.asList(indexedField.analyzers());
			result=JsonData.createObject().put("type", "string");
			if (Iterables.getOnlyElement(analyzers, BaseQueryBuilder.StringTypes.na)!=BaseQueryBuilder.StringTypes.na)
			{
				for (BaseQueryBuilder.StringTypes analyzer : analyzers)
				{
					switch (analyzer)
					{
						case folded:
						{
							fields.put("folded", JsonData.createObject().put("type", "string").put("analyzer", "folding"));
							break;
						}
						case starts_with:
						{
							fields.put("starts_with", JsonData.createObject().put("type", "string").put("analyzer", "startsWith"));
							break;
						}
						case raw:
						default:
						{
							fields.put("raw", JsonData.createObject().put("type", "string").put("analyzer", "not_analyzed"));
							break;
						}
					}
				}
				result.put("fields", fields);
			}
		}
		else
		{
			result=EsPartitionHelper.SCHEMA_MAP.get(cls);
			if (!indexedField.indexable() || Objects.equals(cls, Object.class))
			{
				result.put("index", "not_analyzed");
			}
		}

		if (null==result)
		{
			Environment.getInstance().getReportHandler().warning("Unknown property index mapping for %s.", cls.getName());
		}
		return result;
	}
}
