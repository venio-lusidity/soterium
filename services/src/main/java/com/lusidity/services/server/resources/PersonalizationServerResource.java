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
import com.lusidity.domains.people.person.Personalization;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@SuppressWarnings("ThrowCaughtLocally")
@AtWebResource(pathTemplate = "/svc/personalization", matchingMode = AtWebResource.MODE_FIRST_MATCH,
        methods = "get, post, delete", description = "Store any value for a user's personalization.")
@AtAuthorization()
public class PersonalizationServerResource extends BaseServerResource {

	// Overrides
	@Override
	public Representation remove()
	{
		JsonData result=JsonData.createObject();
		boolean removed=false;
		try
		{
			JsonData content=this.getJsonContent();
			if (((null!=content) && content.isJSONArray()))
			{
				Personalization personalization=this.getCommonPersonalization();
				if (null!=personalization)
				{
					for (Object o : content)
					{
						if (o instanceof String)
						{
							String key=(String) o;
							personalization.getVertexData().update(key, null);
							removed=true;
						}
					}
					if (removed)
					{
						personalization.save();
					}
				}
				else
				{
					throw new ApplicationException("Only people can have personalizations.");
				}
			}
			this.getResponse().setStatus(removed ? Status.SUCCESS_ACCEPTED : Status.SUCCESS_NO_CONTENT);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		result.put("removed", removed);
		return this.getRepresentation(result);
	}

    @Override
    public Representation retrieve(){
        JsonData result = null;
        try{
	        Personalization personalization = this.getCommonPersonalization();

	        if(null!=personalization) {
		        result = personalization.toJson(false);
		        this.getResponse().setStatus(Status.SUCCESS_OK);
	        }
	        else{
		        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		        throw new ApplicationException("Only people can have personalizations.");
	        }
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().severe(ex);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return ((null!=result) && !result.isEmpty()) ? result.toJsonRepresentation() : null;
    }

    @Override
    public Representation store(Representation representation)
    {
        return null;
    }

    @Override
    public Representation update(Representation representation){
	    JsonData result = JsonData.createObject();
	    boolean updated = false;
	    try{
		    JsonData content = this.getJsonContent();
		    if (((null!=content) && content.isJSONObject())) {
			    Personalization personalization = this.getCommonPersonalization();
			    if(null!=personalization) {
			    	personalization.getVertexData().merge(content, true, true, false);
			    	personalization.save();
			    }
			    else{
				    throw new ApplicationException("Only people can have personalizations.");
			    }
		    }
		    this.getResponse().setStatus(updated ? Status.SUCCESS_ACCEPTED : Status.SUCCESS_NO_CONTENT);
	    }
	    catch (Exception ex){
		    Environment.getInstance().getReportHandler().severe(ex);
		    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	    }
	    result.put("updated", updated);
	    return this.getRepresentation(result);
    }
}
