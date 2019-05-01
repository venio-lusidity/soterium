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
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.email.EmailTemplate;
import com.lusidity.email.EmailX;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.common.Statements;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.net.URI;

@AtWebResource(pathTemplate = "/svc/email", methods = "post", description = "Send an email.",
        bodyFormat = "{\"sender\":\"senders email address\"," +
                "\"recipients\": [\"An array of email addresses to send to.\"]," +
                "\"carbonCopy\": \"(Optional) Expects true or false no quotes.  Send a copy to the sender.\"," +
                "\"name\": \"(Optional) senders name\"," +
                "\"content\": \"the message body\"," +
                "\"url\": \"(Optional) Typically used to retrieve a vertex and send a link to it but can be any url.\"" +
                "}")
@AtAuthorization()
public class EmailServerResource
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
        JSONObject result = new JSONObject();
        try {
            if((null!=this.getUserCredentials()) && this.getUserCredentials().isValidated())
            {
                result.put("sent", "failed");
                JsonData data=this.getJsonContent();
                if (!data.has("sender") || !data.has("uri") || !data.has("recipients"))
                {
                    Environment.getInstance().getReportHandler().warning("The json object posted does not have the correct properties.");
                    this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }

                try
                {
                    String sender=data.getString("sender");
                    String who=((data.has("name") && !StringX.isBlank(data.getString("name"))) ? data.getString("name") :
                        this.getName(sender));
                    String content=data.has("content") ? data.getString("content") : "";
                    String url=data.getString("uri");
                    String[] carbonCopy=(data.has("carbonCopy") && data.getBoolean("carbonCopy")) ? new String[]{sender} : null;

                    JsonData recipients=data.getFromPath("recipients");
                    int x=recipients.length();
                    String[] to=new String[x];
                    int i=0;
                    for (Object o : recipients)
                    {
                        if (o instanceof String)
                        {
                            to[i]=o.toString();
                        }
                        i++;
                    }

                    boolean sent=url.toLowerCase().contains("/vertices") ? this.send(who, sender, carbonCopy, to, content, url) :
                        this.sendGeneric(who, sender, carbonCopy, to, content, url);

                    result.put("sent", sent);
                    if (!sent)
                    {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    }
                }
                catch (Exception ignore)
                {
                    Environment.getInstance().getReportHandler().warning("Some of the properties in the JSON object are not the right type.");
                    this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }

        } catch (Exception ex) {
            Environment.getInstance().getReportHandler().severe(ex.getMessage());
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return new JsonRepresentation(result);
    }

    @SuppressWarnings({
        "DynamicRegexReplaceableByCompiledPattern",
        "MethodMayBeStatic"
    })
    private String getName(String sender) {
        return sender.split("@")[0];
    }

    @SuppressWarnings({
        "MethodMayBeStatic",
        "MethodWithTooManyParameters"
    })
    private boolean sendGeneric(String who, String sender, String[] carbonCopy, String[] to, String content, String url) {
        boolean sent;
        try {
            URI uri = new URI(url);
            String subject = String.format("%s has sent you a link.", who);
            String description = Statements.toHtml(Statements.StatementTypes.about);
            String body = EmailTemplate.getEntityMail(who, sender, EmailX.getDefaultSubject(), content, description, uri, null);
            EmailX.sendMail(EmailX.getDefaultServerKey(), EmailX.getDefaultFrom(), to, carbonCopy, new String[]{sender}, subject, body, false, null);
            sent = true;
        } catch (Exception ignored) {
            sent = false;
        }

        return sent;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private boolean send(
        String who, String sender, String[] carbonCopy, String[] to, String content, String url
    ) {
        boolean sent = false;
        try {
            URI uri = new URI(url);
            ApolloVertex vertex = VertexFactory.getInstance().get(DataVertex.class, URI.create(uri.getPath()));
            if (null != vertex) {
                String title = vertex.fetchTitle().getValue();

                String subject = String.format("%s has sent you a link for %s", who, title);

                RawString txtDescription = vertex.getDescriptions().get();
                String description = "";
                if (null != txtDescription) {
                    description = txtDescription.fetchValue().getValue();
                }

                // TODO make images work again.
                URI imageUri = null;
               /* ElementImage image = vertex.getImages().get();
                if (null != image) {
                    imageUri = (null != image.getTileUri()) ? image.getTileUri() : image.getThumbnailUri();
                }*/

                @SuppressWarnings("ConstantConditions")
                String body = EmailTemplate.getEntityMail(
                    who, sender, title, content, description, uri, imageUri
                );

                EmailX.sendMail(
                    EmailX.getDefaultServerKey(), EmailX.getDefaultFrom(), to, carbonCopy, new String[]{sender}, subject, body, false, null
                );
                sent = true;
            }
        } catch (Exception ignored) {
            sent = false;
        }

        return sent;
    }
}
