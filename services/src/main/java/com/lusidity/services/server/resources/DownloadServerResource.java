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
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@AtWebResource(pathTemplate = "/svc/downloads", methods = "get", description = "Gives a response of pong and/or other information if available.")
@AtAuthorization(required = false, anonymous = true)
public class DownloadServerResource
        extends BaseServerResource {
	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve(){
        JsonData result = JsonData.createArray();
        String path = String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files");
        path = StringX.replace(path, "//", "/");
        File directory = new File(path);
        File[] files = directory.listFiles();
        Reference reference = this.getRequest().getOriginalRef();
        String url = String.format("%s://%s%s/files/",
            reference.getSchemeProtocol().getSchemeName(),
            reference.getHostDomain(),
            ((reference.getHostPort()>0) ? String.format(":%d", reference.getHostPort()): ""));
        if(null!=files){
            Arrays.sort(files);
            for(File file: files){
                if(file.isFile()){
                    JsonData item = JsonData.createObject();
                    item.put("name", file.getName());
                    if(StringX.contains(file.getName(), "."))
                    {
                        item.put("fileType", StringX.getLast(file.getName(), ".").toLowerCase());
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM YYYY HH:mm:ss");
                    item.put("modified", sdf.format(file.lastModified()));
                    item.put("href", String.format("%s%s", url, StringX.urlEncode(file.getName())));
                    result.put(item);
                }
            }
            this.getResponse().setStatus(Status.SUCCESS_OK);
        }
        else{
            this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
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
