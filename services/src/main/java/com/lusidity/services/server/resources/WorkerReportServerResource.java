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
import com.lusidity.data.DataVertex;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.system.assistant.message.AssistantMessage;
import com.lusidity.domains.system.assistant.worker.BaseAssistantWorker;
import com.lusidity.domains.system.assistant.worker.JobWorker;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.jobs.IJob;
import com.lusidity.jobs.JobsEngine;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.system.security.UserCredentials;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.io.InvalidClassException;
import java.security.InvalidParameterException;

@AtWebResource(pathTemplate = "/svc/worker/{*}", matchingMode = AtWebResource.MODE_BEST_MATCH,
        methods = "get, delete", description = "Either gets a report on the specified worker or deletes the specified message.", optionalParams = "id")
@AtAuthorization()
public class WorkerReportServerResource
        extends BaseServerResource {

    @Override
    public Representation retrieve() {
        JsonData result = null;
        UserCredentials credentials = this.getUserCredentials();
        if(null==credentials){
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
        BasePrincipal principal = credentials.getPrincipal();
        if(null==principal){
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        if(!principal.isAdmin(false))
        {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        try
        {
            if (this.getRequest().getResourceRef().getPath().contains("worker/jobs"))
            {
                result =JobsEngine.getInstance().toJson();
            }
            else
            {
                result= this.getWorkerReport();
            }
        }
        catch (Exception ignored)
        {
           throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    private JsonData getWorkerReport()
        throws InvalidClassException
    {
        JsonData result = null;
        String key=this.getKey("worker");
        if (!StringX.isBlank(key))
        {
            Class<? extends DataVertex> cls=BaseDomain.getDomainType(key);
            if ((null!=cls) && ClassX.isKindOf(cls, BaseAssistantWorker.class))
            {
                @SuppressWarnings("unchecked")
                BaseAssistantWorker worker=Environment.getInstance().getWorker((Class<? extends BaseAssistantWorker>) cls);
                if (null!=worker)
                {
                    result=worker.getReport(true);
                }
                else
                {
                    throw new InvalidClassException("The worker could not be found, %s.", key);
                }
            }
            else
            {
                this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
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
        JsonData result = JsonData.createObject().put("started", false);
        try {
            if(this.getUserCredentials().getPrincipal().isAdmin(true))
            {
                if (this.getRequest().getResourceRef().getPath().contains("worker/jobs"))
                {
                    JsonData data = this.getJsonContent();
                    boolean success = false;
                    if(data.hasKey("type"))
                    {
                        Class<?> cls = data.getClassFromName("type");
                        if(ClassX.isKindOf(cls, IJob.class))
                        {
	                        @SuppressWarnings("unchecked")
                            JobWorker worker = JobsEngine.getInstance().getWorker((Class<? extends IJob>) cls);
	                        if(null!=worker)
                            {
                                if(data.hasKey("timeOfDay")){
                                    String time = data.getString("timeOfDay");
                                    if(DateTimeX.is24HourTime(time)){
                                        worker.getJobItem().updateTimeOfDay(time);
                                        success = true;
                                    }
                                }
                                if (worker.getJobItem().isExecutable())
                                {
                                    //noinspection unchecked
                                    JobsEngine.getInstance().doJob((Class<? extends IJob>) cls, data.getBoolean("stop"), data.getBoolean("reset"), data);
                                    result.update("started", true);
                                    success = true;
                                }
                            }
                        }
                    }
                    UserActivity.logActivity(data.toString(), this.getUserCredentials(), LogEntry.OperationTypes.post, this.getRequest().getOriginalRef(), true);
                }
            }
            else{
                this.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
        }
        catch (Exception e)
        {
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    @Override
    public Representation remove() {
        JsonData result = null;
        try {
            String key = this.getKey("worker");
            String id = this.getParameter("id");
            if(!StringX.isBlank(key) && !StringX.isBlank(id)) {
                Class<? extends DataVertex> cls = BaseDomain.getDomainType(key);
                if((null!=cls) && ClassX.isKindOf(cls, BaseAssistantWorker.class)) {
                    @SuppressWarnings("unchecked")
                    BaseAssistantWorker worker = Environment.getInstance().getWorker((Class<? extends BaseAssistantWorker>) cls);
                    if (null != worker) {
                        AssistantMessage msg =  VertexFactory.getInstance().get(id);
                        if (null != msg) {
                            boolean removed = worker.remove(msg);
                            if (removed) {
                                removed = msg.delete();
                                result = JsonData.createObject().put("deleted", removed);
                                this.setStatus(Status.SUCCESS_OK);
                            }

                        } else {
                            this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                            throw new InvalidParameterException(String.format("The worker message could not be found, %s.", key));
                        }
                    } else {
                        throw new InvalidParameterException(String.format("The worker could not be found, %s.", key));
                    }
                }
                else{
                    this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    throw new InvalidClassException("The worker could not be found, %s.", key);
                }
            }
        }
        catch (Exception e)
        {
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return this.getRepresentation(result);
    }

    public String getKey(String delimiter) {
        String result = "";
        String url = StringX.getFirst(this.getRequest().getOriginalRef().toString(), "?");
        String[] parts = StringX.split(url, "/");
        if(null!=parts){
            boolean found = false;
            for(String part: parts){
                if(found){
                    result += String.format("/%s", part);
                }
                else{
                    found = StringX.equalsIgnoreCase(part, delimiter);
                }
            }
        }
        return result;
    }
}
