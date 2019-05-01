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

package com.lusidity.services.security.authentication.pki;

import com.lusidity.Environment;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.routing.Filter;
import org.restlet.security.CertificateAuthenticator;
import org.restlet.security.User;

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.logging.Level;

public class PKIAuthenticator extends CertificateAuthenticator {
    @Override
    protected boolean authenticate(Request request, Response response) {
        return super.authenticate(request, response);
    }

    @Override
    protected int authenticated(Request request, Response response) {

        if (Environment.getInstance().isDebugMode())
        {
            Environment.getInstance().getReportHandler().info("PKIAuthenticator started.");
        }

        int result = super.authenticated(request, response);

        if(result== Filter.CONTINUE){
            if(Environment.getInstance().getDataStore().isOffline())
            {
                if (Environment.getInstance().isDebugMode())
                {
                    Environment.getInstance().getReportHandler().info("PKIAuthenticator datastore is offline.");
                }
                Environment.getInstance().getDataStore().verifyConnection();
            }

            if(Environment.getInstance().getDataStore().isOffline()){
                result = this.unauthenticated(request, response);
            }
            else
            {
                Form form=(null!=request.getResourceRef()) ? request.getResourceRef().getQueryAsForm() : null;
                String apiKey=(null!=form) ? form.getValues("apiKey") : null;
                PKICredentials pkiInfo=new PKICredentials(request, apiKey);

                if(Environment.getInstance().getDataStore().isOffline())
                {
                    Environment.getInstance().getReportHandler().info(pkiInfo.toJson().toString());
                }
                if (!pkiInfo.isValidated())
                {
                    request.setClientInfo(null);
                    result=this.unauthenticated(request, response);

                    if (Environment.getInstance().isDebugMode())
                    {
                        Environment.getInstance().getReportHandler().info(String.format("The authentication failed for the identifier \"%s\" using the %s scheme.",
                            request.getChallengeResponse().getIdentifier(),
                            request.getChallengeResponse().getScheme()));

                        if(null==pkiInfo.getPrincipal()){
                            Environment.getInstance().getReportHandler().info("PKIAuthenticator: The principal is null.");
                        }

                        if(null==pkiInfo.getIdentity()){
                            Environment.getInstance().getReportHandler().info("PKIAuthenticator: The identity is null.");
                        }
                    }
                }
                else if(Environment.getInstance().isDebugMode()){
                    Environment.getInstance().getReportHandler().info("PKIAuthenticator: PKI is validated.");
                }
            }
        }
        else if (Environment.getInstance().isDebugMode())
        {
            Environment.getInstance().getReportHandler().info(String.format("The authentication failed for the identifier \"%s\" using the %s scheme.",
                request.getChallengeResponse().getIdentifier(),
                request.getChallengeResponse().getScheme()));
        }
        return result;
    }

    @Override
    protected List<Principal> getPrincipals(List<Certificate> certificateChain) {
        return super.getPrincipals(certificateChain);
    }

    @Override
    protected User getUser(Principal principal) {
        return super.getUser(principal);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        return super.beforeHandle(request, response);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        super.afterHandle(request, response);
    }

    @Override
    protected int doHandle(Request request, Response response) {
        int result = Filter.SKIP;
        try{
            result = super.doHandle(request, response);
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().warning(ex);
        }
        return result;
    }

    public PKIAuthenticator(Context context) {
        super(context);
    }
}
