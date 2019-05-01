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

package com.lusidity;

import com.lusidity.framework.internet.UriBuilderX;
import com.lusidity.framework.json.JsonData;

import java.io.IOException;
import java.net.URI;

/**
 * The AuthorizationResponse doesn't really do anything except to answer the following questions.
 * authorizationResponse.isAuthenticated()
 * authorizationResponse.isValidated()
 * authorizationResponse.isRegistered()
 * Each request is authenticated at the time of the request.
 */
public class AuthorizationResponse extends BaseResponse {

    public AuthorizationResponse(JsonData response, URI endpointUri) throws IOException {
        super(response, endpointUri);
    }

    public static AuthorizationResponse authenticate(ClientConfiguration config) throws Exception {
        KeyStoreManager ksm = new KeyStoreManager(
                config.getKeystorePwd(),
                config.getKeystore(),
                config.getTrustStorePwd(),
                config.getTrustStore());

        SoteriumClient client = new SoteriumClient();

        UriBuilderX builder = new UriBuilderX(config.getBaseServerUrl());
        builder.addPath(config.getEndpointGetAuthenticate());
        builder.addParameter("apiKey", config.getApiKey());

        URI uri = URI.create(builder.toString());
        JsonData response = client.get(ksm, uri);
        return (null != response) ? new AuthorizationResponse(response, uri) : null;
    }

    /**
     * The certificate passed is recognized as having an identity.
     * @return true if authenticated.
     */
    public boolean isAuthenticated(){
        return this.getData().getBoolean("authenticated");
    }

    /**
     * The certificate used was recognized.
     * @return true if validated.
     */
    public boolean isValidated(){
        return this.getData().getBoolean("validated");
    }

    /**
     * Determines if the certificate used has been registered.
     * @return true if registered.
     */
    public boolean isRegistered(){
        return this.getData().getBoolean("registered");
    }

    /**
     * The CN of the certificate.
     * @return The host name.
     */
    public String getCommonName(){
        return this.getData().getString("commonName");
    }

    /**
     * The relative URI to the principal of the identity.
     * @return A relative URI to a principal.
     */
    public URI getPrincipalUri(){
        return this.getData().getUri("host");
    }
}
