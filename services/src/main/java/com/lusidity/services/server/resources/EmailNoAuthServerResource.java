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


import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.PhoneNumber;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/email/no_auth", matchingMode = AtWebResource.MODE_FIRST_MATCH, methods = "post", description = "Send an email.",
        bodyFormat = "{\"sender\":\"senders email address\"," +
                "\"recipients\": [\"An array of email addresses to send to.\"]," +
                "\"carbonCopy\": \"(Optional) Expects true or false no quotes.  Send a copy to the sender.\"," +
                "\"name\": \"(Optional) senders name\"," +
                "\"content\": \"the message body\"," +
                "\"url\": \"(Optional) Typically used to retrieve a vertex and send a link to it but can be any url.\"" +
                "}")
@AtAuthorization(required = false)
public class EmailNoAuthServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        JsonData result = null;
        try
        {
            String email=this.getParameter("email");
            if(!StringX.isBlank(email))
            {
                Person person=Person.Queries.getByEmail(email);
                result=(null!=person) ? person.toJson(false) : null;
                if (null==result)
                {
                    this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
                }
                else{
                    result = JsonData.createObject()
                                     .put("lastName", person.fetchLastName().getValue())
                        .put("middleName", person.fetchMiddleName().getValue())
                        .put("firstName", person.fetchFirstName().getValue());
                    for(BaseContactDetail detail: person.getContactDetails()){
                        if(detail instanceof PhoneNumber){
                            PhoneNumber pn = (PhoneNumber)detail;
                            if(pn.fetchCategory().getValue()==BaseContactDetail.CategoryTypes.work_phone){
                                result.put("phoneNumber", pn.fetchValue().getValue());
                            }
                        }
                    }
                }
            }
            else{
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        catch (Exception ignored){
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return this.getRepresentation(result);
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
