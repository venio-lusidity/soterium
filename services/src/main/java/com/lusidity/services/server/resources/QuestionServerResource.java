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

package com.lusidity.services.server.resources;

import com.lusidity.data.DataVertex;
import com.lusidity.domains.object.edge.QuestionEdge;
import com.lusidity.domains.word.sentence.elicit.BaseQuestion;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.representation.Representation;

import java.util.Collection;

/**
 * The object URI should always result in a Question being retrieved.
 */
@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/question", methods = "delete,get,post", description = "Delete, Get, " +
    "Create or Update a vertex.", matchingMode = AtWebResource.MODE_FIRST_MATCH)
@AtAuthorization()
public class QuestionServerResource
        extends BaseServerResource {

	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve()
    {
        return null;
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public Representation update(Representation representation) {

        // get the vertex...
        DataVertex vertex = null;
        // get cls for data passed.
        Class<? extends BaseQuestion> cls = null;
        // get edge for data passed.
        Class<? extends QuestionEdge> edgeType = null;
        BaseQuestion question = new BaseQuestion();
        Collection<BaseQuestion> questions = question.getQuestion(vertex, cls, edgeType);
        return null;
    }
}
