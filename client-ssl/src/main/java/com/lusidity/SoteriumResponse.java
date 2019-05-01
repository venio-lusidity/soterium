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
public class SoteriumResponse extends BaseResponse {

	// Constructors
	public SoteriumResponse(JsonData response, URI endpointUri) throws IOException {
		super(response, endpointUri);
	}

	// Methods
	public static SoteriumResponse get(ClientConfiguration config, String baseServerUrl, String relativePath) throws Exception {
		KeyStoreManager ksm = new KeyStoreManager(
			config.getKeystorePwd(),
			config.getKeystore(),
			config.getTrustStorePwd(),
			config.getTrustStore());

		SoteriumClient client = new SoteriumClient();

		UriBuilderX builder = new UriBuilderX(baseServerUrl);
		builder.addPath(relativePath);
		builder.addParameter("apiKey", config.getApiKey());

		URI uri = URI.create(builder.toString());
		JsonData response = client.get(ksm, uri);
		return (null!=response) ? new SoteriumResponse(response, uri) : null;
	}

// Getters and setters
    /**
     * Is the data valid.
     * @return true if valid.
     */
    public boolean isValid(){
        return (null!=this.getData());
    }
}
