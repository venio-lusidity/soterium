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
import com.lusidity.framework.text.StringX;
import com.lusidity.services.common.DynamicFileRepresentation;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Disposition;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.net.URL;

@AtWebResource(pathTemplate = "/svc/file", methods = "get", description = "Retrieve a file by URL.", requiredParams = "url (String)")
@AtAuthorization()
public class FileServerResource
        extends BaseServerResource {
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve() {
	    DynamicFileRepresentation result = null;
        String url = this.getParameter("url");
        String path = this.getParameter("path");
        String filename = this.getParameter("filename");
        if(!StringX.isBlank(filename) && !StringX.isBlank(path)){
        	if(this.getUserCredentials().getPrincipal().isAdmin(true))
	        {
		        url=String.format("file:/%s/%s/%s", Environment.getInstance().getConfig().getTempDir(), path, filename);
	        }
	        else{
        		url = null;
	        }
        }
        if (null != url) {
            try {
                result = DynamicFileRepresentation.get(new URL(url), this.getResponse());
				if(!StringX.isBlank(filename))
				{
					Disposition disposition = new Disposition();
					disposition.setFilename(filename);
					disposition.setType(Disposition.TYPE_ATTACHMENT);
					result.setDisposition(disposition);
				}
            } catch (Exception ignore) {
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        } else {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return result;
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

