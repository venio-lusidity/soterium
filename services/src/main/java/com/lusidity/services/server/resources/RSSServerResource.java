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
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.xml.LazyXmlNode;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.representation.Representation;

import java.io.File;
import java.net.URI;

@AtWebResource(pathTemplate = "/svc/rss", methods = "get", description = "Attempts to retrieve the specified RSS feed and then returns it.", requiredParams = "url")
@AtAuthorization()
public class RSSServerResource
        extends BaseServerResource {

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
        JsonData result = null;
        String sUrl = this.getParameter("url");
        String offline = this.getParameter("offline");
       if(!StringX.isBlank(sUrl)){
           if(!StringX.isBlank(offline)){
               String name = StringX.removeNonAlphaNumericCharacters(StringX.urlEncode(sUrl), " ");
               result = new JsonData(new File(Environment.getInstance().getConfig().getResourcePath(), String.format("rss/%s.json", name)));
           }
           else {
               try {
                   sUrl = StringX.urlDecode(sUrl, "UTF-8");
                   URI url = URI.create(sUrl);
                   LazyXmlNode response = HttpClientX.getXML(url);
                   if (null != response) {
                       result = new JsonData(response);
                   }
               } catch (Exception ex) {
                   Environment.getInstance().getReportHandler().info(ex.getMessage());
               }
           }
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
