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
 * Once you receive an API key from Soterium you must register it from your application.
 * This only has to be done once.
 * If you call it again and have an account "hasAccount" in the response will be true.
 */
public class RegisterResponse extends BaseResponse {
    public RegisterResponse(JsonData response, URI endpointUri) throws IOException {
        super(response, endpointUri);
    }

    /**
     * Did the request fail in any way?
     * @return true or false.
     */
    public Boolean failed(){
        return this.getData().getBoolean("failed");
    }

    /**
     * Does the API identity used have an account and/or was it created?
     * @return true or false.
     */
    public boolean isRegistered(){
        return this.getData().getBoolean("hasAccount");
    }

    /**
     * Any error or message returned.
     */
    public String getMessage(){
        return this.getData().getString("error");
    }

    /**
     * Register the api key.
     * @param config The configuration file.
     * @return The response from regestering.
     * @throws Exception
     */
    public static RegisterResponse post(ClientConfiguration config) throws Exception {
        KeyStoreManager ksm = new KeyStoreManager(
                config.getKeystorePwd(),
                config.getKeystore(),
                config.getTrustStorePwd(),
                config.getTrustStore());

        SoteriumClient client = new SoteriumClient();

        UriBuilderX builder = new UriBuilderX(config.getBaseServerUrl());
        builder.addPath(config.getEndpointPostRegister());
        builder.addParameter("apiKey", config.getApiKey());

        URI uri = URI.create(builder.toString());
        JsonData data = JsonData.createObject();
        data.put("principalUri", config.getPrincipalUri());

        JsonData response = client.post(ksm, uri, data);
        return (null != response) ? new RegisterResponse(response, uri) : null;
    }
}
