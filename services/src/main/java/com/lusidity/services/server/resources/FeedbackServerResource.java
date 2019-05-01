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

import com.lusidity.Environment;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.common.PhoneNumber;
import com.lusidity.domains.people.Person;
import com.lusidity.email.EmailX;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/feedback", methods = "post", description = "Sends a Feedback email to the development team.",
        bodyFormat = "{\"category\":\"The subject for the feedback.\"," +
                "\"note\",\"Any other key value paris will be reformatted as plain text and inserted into the body.\"}")
@AtAuthorization()
public class FeedbackServerResource
        extends BaseServerResource {

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
    public Representation update(Representation representation)
    {
        JSONObject result = new JSONObject();
        try {
            result.put("sent", "failed");
            JsonData data = this.getJsonContent();
            String from = EmailX.getDefaultFrom();
            BasePrincipal principal = this.getUserCredentials().getPrincipal();
            if(ClassX.isKindOf(principal, Person.class))
            {
                Person person=(Person) principal;
                Email email = null;
                PhoneNumber phoneNumber = null;
                data.put("who", person.fetchTitle());
                for(BaseContactDetail contact: person.getContactDetails()){
                    if(ClassX.isKindOf(contact, Email.class)){
                        Email test = (Email)contact;
                        if(test.fetchValue().isNotNullOrEmpty())
                        {
                            email = test;
                            if (email.fetchCategory().getValue()==BaseContactDetail.CategoryTypes.work_email)
                            {
                                break;
                            }
                        }
                    }
                    else if(ClassX.isKindOf(contact, PhoneNumber.class)){
                        PhoneNumber test = (PhoneNumber)contact;
                        if(test.fetchValue().isNotNullOrEmpty())
                        {
                            phoneNumber = test;
                            if (phoneNumber.fetchCategory().getValue()==BaseContactDetail.CategoryTypes.work_phone)
                            {
                                break;
                            }
                        }
                    }
                }
                if(null!=email){
                    data.put("email", String.format("mailTo:%s", email.fetchValue().getValue()));
                }
                if(null!=phoneNumber){
                    data.put("phone", phoneNumber.fetchValue().getValue());
                }
            }

            String body = EmailX.toPlainTextFormat(data, 0);
            if (!StringX.isBlank(body)) {
                String subject = data.getString("category");
                subject = StringX.isBlank(subject) ? "feedback: Inquiry" : String.format("feedback: %s", subject);
                boolean sent=EmailX.sendMail(EmailX.getDefaultServerKey(),
                    from, EmailX.getDefaultTo(), null, null, subject, body, true, null
                );
                result.put("sent", sent);
            } else {
                Environment.getInstance().getReportHandler().warning("The json object posted did not have any data.");
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception ex) {
            Environment.getInstance().getReportHandler().severe(ex.getMessage());
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return new JsonRepresentation(result);
    }
}
