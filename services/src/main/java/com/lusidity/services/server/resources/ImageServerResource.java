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
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.image.ImageData;
import com.lusidity.framework.image.ImageX;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.text.TextEncoding;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@AtWebResource(pathTemplate = "/svc/image", methods = "get",
        description = "Either retrieve an image from blob storage or from a third party URL, returns a data URI for embedding.",
        requiredParams = "url (String), size (Integer Optional) (Note: Converts image to a proportional size base on size provided.")
@AtAuthorization()
public class ImageServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        String url = this.getParameter("url");
        String size = this.getParameter("size");

        JSONObject result = null;
        if (!StringX.isBlank(url)) {
            try {
                url = StringX.urlDecode(url, TextEncoding.UTF8);
                URI paramUri = new URI(url);

                String sourceUri = ImageServerResource.getFromWeb(url, size);

                if (!StringX.isBlank(sourceUri)) {
                    result = new JSONObject();
                    result.put("dataUri", sourceUri);
                    this.getResponse().setStatus(Status.SUCCESS_OK);
                } else {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            } catch (Exception e) {
                Environment.getInstance().getReportHandler().severe("Could not get image %s: %s.", url, e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        } else {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        return (null == result) ? null : new JsonRepresentation(result);
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

    private static String getFromWeb(String paramUrl, String size)
            throws ApplicationException, MalformedURLException {
        String result = null;
        String workingUrl = paramUrl;
        if (paramUrl.toLowerCase().contains("googleapis.com")) {
            workingUrl = workingUrl + (paramUrl.contains("?") ? "&key=" : "?key=") + Environment.getInstance().getSetting("google_api_key");
        }
        URL url = new URL(workingUrl);
        int squared = 0;
        if (!StringX.isBlank(size)) {
            squared = Integer.parseInt(size);
        }
        if (squared > 0) {
            ImageData imageData = ImageX.getImageData(url, Environment.DEFAULT_HOST, squared, false);
            result = ImageX.toHTMLSource(imageData.getImageBits(), imageData.getContentType());
        } else {

            result = ImageX.getAsHTMLSource(url, Environment.DEFAULT_HOST);
        }
        return result;
    }
}
