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

import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.services.common.Statements;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/statement/{statement}", methods = "get", description = "Get a statement.")
@AtAuthorization()
public class StatementServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
        JSONObject result = null;
        String statement = this.getParameter("statement");
        try {
            Statements.StatementTypes statementType = Statements.StatementTypes.valueOf(statement.toLowerCase());
            result = Statements.getStatement(statementType);
            this.getResponse().setStatus((null == result) ? Status.CLIENT_ERROR_NOT_FOUND : Status.SUCCESS_OK);
        } catch (Exception ignore) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (null == result) ? null : new JsonRepresentation(result);
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public Representation update(Representation representation)
    {
        return null;
    }
}
