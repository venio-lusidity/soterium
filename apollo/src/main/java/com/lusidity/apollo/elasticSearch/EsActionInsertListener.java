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
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;

public class EsActionInsertListener implements ActionListener<IndexResponse>
{
	private final DataVertex vertex;

	// Constructors
	public EsActionInsertListener(DataVertex vertex)
	{
		super();
		this.vertex=vertex;
	}

	// Overrides
	@Override
	public void onResponse(IndexResponse indexResponse)
	{
		if (indexResponse.isCreated())
		{
			this.vertex.setIndexId(indexResponse.getIndex());
		}
		else
		{
			Environment.getInstance().getReportHandler().severe("Failed to insert vertex.");
		}
	}

	@Override
	public void onFailure(Throwable throwable)
	{
		Environment.getInstance().getReportHandler().severe(throwable.getMessage());
	}
}
