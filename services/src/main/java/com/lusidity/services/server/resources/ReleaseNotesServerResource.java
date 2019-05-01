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
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

@AtWebResource(pathTemplate = "/svc/notes", methods = "get", description = "Get, " +
    "release notes.", matchingMode = AtWebResource.MODE_BEST_MATCH)
@AtAuthorization()
public class ReleaseNotesServerResource
        extends BaseServerResource {

	// Overrides
	@Override
	public Representation remove()
	{
		return null;
	}

    @Override
    public Representation retrieve() {
        JsonData result = JsonData.createObject();
	    JsonData results = JsonData.createArray();
	    try {
	        Collection<File> noteFiles = new ArrayList<>();
            String path=String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "data/release");
            path=StringX.replace(path, "//", "/");
            File file=new File(path);
	        if(file.isDirectory()){
		        File[] files = file.listFiles();
		        if ((null!=files) && (files.length>0))
		        {
			        for(File working: files) {
				        if(StringX.startsWithIgnoreCase(working.getName(),"release") && StringX.endsWithIgnoreCase(working.getName(),"json")){
					        noteFiles.add(working);
				        }
			        }
		        }
	        }

	        for(File noteFile: noteFiles){
		        JsonData item =new JsonData(noteFile);
		        if(item.isValid() && (!item.isEmpty()))
		        {
			        results.put(item);
		        }
	        }
		    int hits=0;
		    if(!results.isEmpty()) {
		       hits = results.length();
	        }
	        result.put("hits", String.valueOf(hits));
		    result.put("results", results);
        }
        catch (Exception e)
        {
            Environment.getInstance().getReportHandler().severe(e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public Representation update(Representation representation) {return null;}
}
