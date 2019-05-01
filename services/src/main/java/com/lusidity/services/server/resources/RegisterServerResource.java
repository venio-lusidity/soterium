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
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.common.PhoneNumber;
import com.lusidity.domains.organization.OrganizationHelper;
import com.lusidity.domains.people.Person;
import com.lusidity.email.EmailConfiguration;
import com.lusidity.email.EmailTemplate;
import com.lusidity.email.EmailX;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.security.IdentityHelper;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.security.authentication.pki.PKICredentials;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import java.util.ArrayList;
import java.util.Collection;

@AtWebResource(pathTemplate = "/svc/register", methods = "post", description = "Register a user.")
@AtAuthorization(required = false, anonymous = true)
public class RegisterServerResource
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

    @Post
    public Representation update(Representation representation) {
        JsonData result = JsonData.createObject();
        if ((null!=this.getClientInfo()) && this.getClientInfo().isAuthenticated()
            && (null!=this.getClientInfo().getUser())) {
            String apiKey = this.getParameter("apiKey");
            PKICredentials pkiInfo = new PKICredentials(this.getRequest(), apiKey);
            if(!pkiInfo.isRegistered()){
                try {
                    JsonData data = this.getJsonContent();
                    if(data.isValid()) {
                        if(!StringX.isBlank(apiKey)){
                            this.getOrCreateIdentity(result, apiKey, pkiInfo, data);
                        }
                        else
                        {
                            this.getOrCreatePerson(pkiInfo, data);
                        }
                        if(pkiInfo.isRegistered()){
                            try
                            {
                                EmailTemplate template=EmailConfiguration.getInstance().getTemplate("register");
                                if(null!=template)
                                {
                                    String subject=template.getSubject();
                                    String message=template.getBody();
                                /*if(StringX.startsWith(message, "file")){
                                    String[] parts = message.split("//");
                                    String path = parts[1];
                                    File file = new File(Environment.getInstance().getConfig().getResourcePath(), path);
                                    message = FileX.getString(file);
                                }*/
                                    if (StringX.contains(message, "[who]"))
                                    {
                                        message=StringX.replace(message, "[who]", String.format("%s %s", pkiInfo.getFirstName(), pkiInfo.getLastName()));
                                    }

                                    String email=data.getString("email");
                                    if (!StringX.isBlank(email))
                                    {
                                        String[] recipients=new String[1];
                                        recipients[0]=email;
                                        EmailX.sendMail(EmailX.getDefaultServerKey(),
                                            EmailX.getDefaultFrom(), recipients, null, null, subject, message, true, null
                                        );
                                    }
                                    BasePrincipal principal = pkiInfo.getPrincipal();
                                    principal.setCredentials(pkiInfo);
                                    UserActivity.logActivity(principal, LogEntry.OperationTypes.registered, String.format("%s has registered for an account.", principal.fetchTitle().getValue()), true);
                                }
                                else{
                                    Environment.getInstance().getReportHandler().warning("Email template for \"register\" is not configured.");
                                }
                            }
                            catch (Exception ex){
                                Environment.getInstance().getReportHandler().warning(ex);
                            }                             
                            try
                            {
                                EmailTemplate template=EmailConfiguration.getInstance().getTemplate("register_requested");
                                if(null!=template)
                                {
                                    String subject=template.getSubject();
                                    String message=template.getBody();
                                    if (StringX.contains(message, "[who]"))
                                    {
                                        message=StringX.replace(message, "[who]", String.format("%s %s", pkiInfo.getFirstName(), pkiInfo.getLastName()));
                                    }
                                    EmailX.sendMail(EmailX.getDefaultServerKey(),
                                        EmailX.getDefaultFrom(), EmailX.getDefaultTo(), null, null, subject, message, true, null
                                    );
                                }
                                else{
                                    Environment.getInstance().getReportHandler().warning("Email template for \"register_requested\" is not configured.");
                                }
                            }
                            catch (Exception ex){
                                Environment.getInstance().getReportHandler().warning(ex);
                            }
                        }
                        if(pkiInfo.isRegistered() && data.hasKey("organization")){
                            OrganizationHelper.register(pkiInfo.getPrincipal(), data);
                        }

                        pkiInfo = new PKICredentials(this.getRequest(), apiKey);
                        result.put("failed", !pkiInfo.isRegistered());
                        result.put("hasAccount", pkiInfo.isRegistered());
                        result.put("error", "none");
                    }
                    else{
                        result.put("failed", true);
                        result.put("hasAccount", false);
                        result.put("error", "The data sent is not in the correct format.");
                    }
                }
                catch (Exception ex){
                    result.put("failed", true);
                    result.put("hasAccount", false);
                    result.put("error", "Failed to create the user account.");
                }
            }
            else{
                result.put("failed", true);
                result.put("hasAccount", true);
                result.put("error", "This user has already been registered.");
            }
        }
        else{
            result.put("failed", true);
            result.put("hasAccount", false);
            result.put("error", "The user is not authenticated.");
        }

        return this.getRepresentation(result);
    }

    private void getOrCreateIdentity(JsonData result, String apiKey, PKICredentials pkiInfo, JsonData data)
        throws ApplicationException
    {
        if (data.hasKey("principalUri"))
        {
            Identity identity = IdentityHelper.getOrCreateApiIdentity(data, apiKey, "x509", pkiInfo.getCommonName());
            if (null==identity)
            {
                result.put("failed", true);
                result.put("hasAccount", false);
                result.put("error", "The identity does not exist.");
            }
        }
        else
        {
            result.put("failed", true);
            result.put("hasAccount", false);
            result.put("error", "The principalUri is missing.");
        }
    }

    private void getOrCreatePerson(PKICredentials pkiInfo, JsonData data)
        throws Exception
    {
        data.put("identifier", pkiInfo.getIdentifier());
        data.put("provider", "x509");
        Collection<BaseContactDetail> contactDetails = new ArrayList<>();
        String value = data.getString("email");
        if(!StringX.isBlank(value))
        {
            contactDetails.add(new Email(BaseContactDetail.CategoryTypes.work_email, data.getString("email")));
        }
        Person person = IdentityHelper.getOrCreate(data, contactDetails, Identity.LoginType.pki);
        if (null != person)
        {
            String phone = data.getString("phone");
            if (!StringX.isBlank(phone))
            {
                String ext = data.getString("ext");
                PhoneNumber phoneNumber = new PhoneNumber(BaseContactDetail.CategoryTypes.work_phone, phone, ext);
                if (phoneNumber.isValid())
                {
                    phoneNumber.save();
                    if (!StringX.isBlank(phoneNumber.fetchId().getValue()))
                    {
                        boolean added = person.getContactDetails().add(phoneNumber);
                        if (!added)
                        {
                            Environment.getInstance().getReportHandler().warning("Failed to add contact information to %s.", person.fetchId().getValue());
                        }
                    }
                }
            }
        }
    }
}
