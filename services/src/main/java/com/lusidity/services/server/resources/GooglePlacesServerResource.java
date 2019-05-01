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

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.lusidity.Environment;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

@AtWebResource(pathTemplate = "/svc/google/nearbysearch", methods = "get", description = "Use the Google place search API.",
requiredParams = "sensor (String), location (String), name (String), radius (String)")
@AtAuthorization()
public class GooglePlacesServerResource
        extends BaseServerResource {
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/apiKey/place/search/json?";

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
        JsonData result = null;

        String paramSensor = this.getParameter("sensor");
        String paramLocation = this.getParameter("location");
        String paramName = this.getParameter("name");
        String paramRadius = this.getParameter("radius");
        if (!StringX.isBlank(paramSensor) && !StringX.isBlank(paramLocation) && !StringX.isBlank(paramName) && !StringX.isBlank(paramRadius)) {
            try {
                HttpTransport httpTransport = new NetHttpTransport();
                HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory();
                GenericUrl url = new GenericUrl(GooglePlacesServerResource.PLACES_SEARCH_URL);
                HttpRequest request = httpRequestFactory.buildGetRequest(url);
                HttpHeaders headers = new HttpHeaders();
                headers.put("X-HTTP-Method-Override", "GET");
                headers.put("Referer", Environment.DEFAULT_HOST);
                request.setHeaders(headers);
                request.getUrl().put("key", Environment.getInstance().getSetting("google_api_key"));
                request.getUrl().put("location", paramLocation);
                request.getUrl().put("radius", paramRadius);
                request.getUrl().put("name", paramName);
                request.getUrl().put("sensor", paramSensor);
                HttpResponse httpResponse = request.execute();

                String strResponse = httpResponse.parseAsString();

                if (!StringX.isBlank(strResponse)) {
                    result = new JsonData(strResponse);
                    if (result.has("status") && result.getString("status").equals("REQUEST_DENIED")) {
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
                    }
                }
            } catch (Exception ignored) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
            }
        } else {
            throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "'location', 'name', 'sensor', and 'radius' parameters are required."
            );
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
