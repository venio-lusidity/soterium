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
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.DateTimeX;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.joda.time.DateTime;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import sun.reflect.generics.scope.Scope;

import java.io.File;

@AtWebResource(pathTemplate = "/svc/file/locator", methods = "post", description = "Retrieve a file URL by path and file name. Admins and account manager are the only ")
@AtAuthorization()
public class ReportLocatorServerResource extends BaseServerResource
{
    @Override
    public Representation remove() {
        return null;
    }

    @Override
    public Representation retrieve() {
        return null;
    }

    @Override
    public Representation store(Representation representation) {
        return null;
    }

    @Override
    public Representation update(Representation representation) {
        JsonData result = JsonData.createObject();
        if (this.getUserCredentials().getPrincipal().isAdmin(true) ||
            ScopedConfiguration.getInstance().isAccountManager(this.getUserCredentials())) {
            String pathString = null;
            boolean exact = Boolean.parseBoolean(this.getParameter("exact"));
            JsonData data = this.getJsonContent();
            String fileName = data.getString("fileName");
            JsonData pathData = data.getFromPath("paths");
            if((null!=pathData) && pathData.isJSONArray()) {
                StringBuilder sb = new StringBuilder();
                for(Object o: pathData) {
                    JsonData path = JsonData.create(o);
                    String pathName = path.getString("path");
                    sb.append(pathName).append("/");
                }
                pathString = sb.toString();
            }
            String path = null;
            if(null != pathString){
                path = String.format("%s/%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files", pathString);
            }
            else{
                path = String.format("%s/%s", Environment.getInstance().getConfig().getResourcePath(), "web/files/");
            }
            File target = null;
            path = StringX.replace(path, "//", "/");
            File dir = new File(path);
            if(dir.exists() && dir.isDirectory()){
                if(exact) {
                    String fullPath = String.format("%s%s", path, fileName);
                    target = FileX.getFile(fullPath);
                }
                else{
                    DateTime latest = null;
                    File[] files = dir.listFiles();
                    if(null != files && files.length > 0) {
                        for(int i = 0; i < files.length; i++){
                            File f = files[i];
                            if(StringX.startsWithIgnoreCase(f.getName(), fileName)) {
                                DateTime actual = DateTimeX.getDateTimeFromStamp(String.valueOf(f.lastModified()));
                                if(null == latest || actual.isAfter(latest)) {
                                    latest = actual;
                                    target = f;
                                }
                            }
                        }
                    }
                }
            }
            if(null != target){
                String fileDir = String.format("/files/%s", pathString);
                //FileX.getWebUrl is necessary to get the relative path used in the UI
                String url = FileX.getWebUrl(target, fileDir);
                result.put("url", url);
            }
            else{
                result.put("url", null);
            }
        }
        else {
            this.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            result = JsonData.createObject();
            result.put("_response_status", "unauthorized", true);
            result.put("_response_code", 403, true);
            result.put("url", null);
        }
        return this.getRepresentation(result);
    }
}
