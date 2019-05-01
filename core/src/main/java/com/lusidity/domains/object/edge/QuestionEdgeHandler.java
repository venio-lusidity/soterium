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

import com.lusidity.Environment;
import com.lusidity.collections.IEdgeHandler;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;

public class QuestionEdgeHandler implements IEdgeHandler
{
// Overrides
	@Override
	public boolean handle(DataVertex origin, DataVertex other, EdgeData edgeData, JsonData data, ProcessStatus processStatus, Object... args)
	{
		boolean result=false;
		try
		{
			if (ClassX.isKindOf(edgeData.getEdge(), QuestionEdge.class))
			{
				QuestionEdge questionEdge=(QuestionEdge) edgeData.getEdge();
				QuestionEdge.ResponseType responseType=data.getEnum(QuestionEdge.ResponseType.class, "responseType");
				if (responseType!=null)
				{
					questionEdge.fetchResponseType().setValue(responseType);
					result=questionEdge.save();
				}
			}
		}
		catch (Exception x)
		{
			Environment.getInstance().getReportHandler().warning(x);
		}
		return result;
	}
}
