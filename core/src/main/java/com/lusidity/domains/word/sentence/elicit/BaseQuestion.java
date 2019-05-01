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

package com.lusidity.domains.word.sentence.elicit;


import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.object.edge.ArtifactEdge;
import com.lusidity.domains.object.edge.QuestionEdge;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;

import java.util.ArrayList;
import java.util.Collection;

@AtSchemaClass(name="Base Question", discoverable=false, description="A sentence designed to elicit information.")
public class  BaseQuestion extends BaseDomain
{
	@AtSchemaProperty(name="Respondents", edgeType=QuestionEdge.class, description="The people who have been or will be questioned.",
		expectedType=Person.class)
	private ElementEdges<Person> respondents=null;
	@AtSchemaProperty(name="Artifacts", edgeType=ArtifactEdge.class, description="The object in question.",
		expectedType=DataVertex.class, isSingleInstance = true)
	private ElementEdges<DataVertex> artifacts=null;

// Constructors
	public BaseQuestion()
	{
		super();
	}

	public BaseQuestion(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public Collection<BaseQuestion> getQuestion(DataVertex artifact, Class<? extends BaseQuestion> questionType, Class<? extends QuestionEdge> edgeType)
	{
		Collection<BaseQuestion> results=new ArrayList<>();
		BaseQueryBuilder qb=BaseQuestion.getQuery(artifact, questionType, edgeType);
		QueryResults queryResults=qb.execute();
		if ((null!=queryResults) && !queryResults.isEmpty())
		{
			for (IQueryResult queryResult : queryResults)
			{
				DataVertex vertex=queryResult.getOtherEnd(artifact.fetchId().getValue());
				if ((null!=vertex) && ClassX.isKindOf(vertex, BaseQuestion.class))
				{
					CollectionX.addIfUnique(results, (BaseQuestion) vertex);
				}
			}
		}
		return results;
	}

	private static BaseQueryBuilder getQuery(DataVertex artifact, Class<? extends BaseQuestion> questionType, Class<? extends QuestionEdge> edgeType)
	{
		String endpointLabel=Endpoint.KEY_TO_EP_ID;
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(edgeType, questionType, 0, 10);
		qb.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, ClassHelper.getPropertyKey(DataVertex.class, "artifacts"));
		qb.filter(BaseQueryBuilder.Operators.must, endpointLabel, BaseQueryBuilder.StringTypes.raw, artifact.fetchId().getValue());
		qb.filter(BaseQueryBuilder.Operators.must, "deprecated", BaseQueryBuilder.StringTypes.raw, false);
		return qb;
	}

// Getters and setters
	public ElementEdges<Person> getRespondents()
	{
		if (null==this.respondents)
		{
			this.buildProperty("respondents");
		}
		return this.respondents;
	}

	public ElementEdges<DataVertex> getArtifacts()
	{
		if (null==this.artifacts)
		{
			this.buildProperty("artifacts");
		}
		return this.artifacts;
	}
}
