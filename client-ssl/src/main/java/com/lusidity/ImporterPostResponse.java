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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;

public class ImporterPostResponse extends BaseResponse {

    public ImporterPostResponse(JsonData response, URI endpointUri) throws IOException {
        super(response, endpointUri);
    }


    public String getExtension(){
        return this.getData().getString("extension");
    }

    public String getVertexId(){
        return this.getData().getString("vertexId");
    }

    public String getId(){
        return this.getData().getString("lid");
    }

    public String getVertexUri(){
        return this.getData().getString("/vertex/uri");
    }

    public String getTitle(){
        return this.getData().getString("title");
    }

    public String getImportType(){
        return this.getData().getString("type");
    }

    public String getCommandType(){
        return this.getData().getString("commandTypes");
    }

    public String getOriginalFileName(){
        return this.getData().getString("originalName");
    }

    public String getStatus(){
        return this.getData().getString("status");
    }

    public long getSizeInBytesPosted(){
        return this.getData().getLong("bytes");
    }

    public long getSizeInKbPosted(){
        return this.getData().getLong("kb");
    }

    /**
     * Post a file to the file importer endpoint.
     * @param config The configuration file.
     * @param file The file to post.
     * @param clsName The full class name of the importer to use.
     * @return Information about the posted file.
     * @throws Exception
     */
    public static ImporterPostResponse post(ClientConfiguration config, File file, String clsName) throws Exception {
        ImporterPostResponse result = null;

        if(!file.exists()){
            throw new InvalidParameterException(String.format("The file %s, does not exist.", file.getAbsolutePath()));
        }

        KeyStoreManager ksm = new KeyStoreManager(
                config.getKeystorePwd(),
                config.getKeystore(),
                config.getTrustStorePwd(),
                config.getTrustStore());

        AuthorizationResponse authorizationResponse = AuthorizationResponse.authenticate(config);

        if((null!=authorizationResponse) && !authorizationResponse.isRegistered()) {
            RegisterResponse ipr = RegisterResponse.post(config);
            if (null != ipr) {
                if (ipr.isRegistered()) {
                    authorizationResponse = AuthorizationResponse.authenticate(config);
                } else {
                    throw new Exception(ipr.getMessage());
                }
            } else {
                throw new Exception("Sorry we don't know what happened the response was null.");
            }
        }

        if(null!=authorizationResponse) {
            if (authorizationResponse.isAuthenticated()) {
                SoteriumClient client = new SoteriumClient();

                UriBuilderX builder = new UriBuilderX(config.getBaseServerUrl());
                builder.addPath(config.getEndpointPostAppImporterFile());
                builder.addParameter("apiKey", config.getApiKey());
                builder.addParameter("type", clsName);
                builder.addParameter("uri", config.getPrincipalUri());

                URI uri = URI.create(builder.toString());
                JsonData response = client.postImporterRmk(ksm, uri, file);
                if((null!=response) && response.isValid()){
                    result = new ImporterPostResponse(response, uri);
                }
            }
            else if(!authorizationResponse.isValidated()){
                throw new Exception("The certificate used was not recognized.");
            }
            else if(!authorizationResponse.isRegistered()){
                throw new Exception("The API key is not registerd.");
            }
            else {
                throw new Exception("Sorry we don't know what happened the response was null.");
            }
        }

        return result;
    }
}
