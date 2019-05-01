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
import com.lusidity.data.ApolloVertex;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/domains/{domain}/{webId}/images", methods = "get", description = "Get any images related to the vertex.")
@AtAuthorization()
public
class VertexImageServerResource extends BaseServerResource
{
    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        String webId = String.valueOf(this.getRequest().getAttributes().get("webId"));
        JsonData result = new JsonData("");
        if (!StringX.isBlank(webId))
        {
            try
            {
                ApolloVertex vertex = VertexFactory.getInstance().get(ApolloVertex.class, webId);

                if(null!=vertex)
                {
                    VertexImageServerResource.processImages(vertex);
                    //TODO make images work again.
                    result = null;// vertex.getImages().toJson();
                }
                else
                {
                    Environment.getInstance().getReportHandler().severe("The entity requested for %s is null.", webId);
                }
            }
            catch (Exception e)
            {
                Environment.getInstance().getReportHandler().severe(e);
            }
        }
        else
        {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            Environment.getInstance().getReportHandler().warning("Missing webId parameter");
            result = JsonData.createObject();
            result.put("error", "Missing webId parameter");
        }

        if(null==result){
            result = JsonData.createObject();
            result.put("error", "No images found.");
        }

        return result.toJsonRepresentation();
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

    private static void processImages(ApolloVertex vertex) {
        Environment.getInstance().getReportHandler().notImplemented();
        // TODO: Search was removed which is why this code no longer works.
        /*
        if ((null != entity.getImages()) && !entity.getImages().isEmpty()) {
            for (ElementImage elementImage : entity.getImages()) {
                try {
                    @SuppressWarnings({"TypeMayBeWeakened", "unchecked"})
                    ImageTask imageTask = new ImageTask(entity, elementImage);
                    imageTask.call();
                }
                catch (Exception ignored){}
            }
        }
        */
    }

}
