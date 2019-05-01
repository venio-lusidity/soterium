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

import com.lusidity.collections.IEdgeHandler;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="Question Edge", discoverable=false, writable=false,
	description="")
public class QuestionEdge extends TermEdge
{
	public enum ResponseType
	{
		yes,
		no,
		ask_later,
		na,
		unanswered
	}

	private KeyData<QuestionEdge.ResponseType> responseType=null;

	// Constructors
	public QuestionEdge()
	{
		super();
	}

	public QuestionEdge(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public QuestionEdge(EdgeData edgeData, Endpoint fromEndpoint, Endpoint toEndpoint)
	{
		super(edgeData, fromEndpoint, toEndpoint);
	}

	// Overrides
	@Override
	public IEdgeHandler getHandler()
	{
		return new QuestionEdgeHandler();
	}

	@Override
	public boolean save()
		throws Exception
	{
		if(this.fetchDeprecated().getValue()){
			return this.delete();
		}
		else {
			return super.save();
		}
	}

	public KeyData<QuestionEdge.ResponseType> fetchResponseType()
	{
		if (null==this.responseType)
		{
			this.responseType=new KeyData<>(this, "responseType", QuestionEdge.ResponseType.class, false, QuestionEdge.ResponseType.unanswered);
		}
		return this.responseType;
	}
}
