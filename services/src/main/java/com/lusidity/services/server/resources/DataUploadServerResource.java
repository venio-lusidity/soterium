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
import com.lusidity.framework.json.PropertyExtractor;
import com.lusidity.services.security.annotations.AtAuthorization;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FilenameUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@AtWebResource(pathTemplate = "/svc/dataUpload", methods = "post", description = "Temporarily upload a csv file and extract and return the properties.")
@AtAuthorization()
public class DataUploadServerResource
        extends BaseServerResource {

    private static File tempDirectory = null;
    static {
        DataUploadServerResource.tempDirectory = new File("temp_data");
        if(!DataUploadServerResource.tempDirectory.exists()){
            //noinspection ResultOfMethodCallIgnored
            DataUploadServerResource.tempDirectory.mkdir();
        }
    }

    @Override
    public Representation remove()
    {
        return null;
    }

    @Override
    public Representation retrieve()
    {
        return null;
    }

    @Override
    public Representation store(Representation representation)
    {
        JsonData result = null;
        try
        {
            if (representation != null)
            {
                if (MediaType.MULTIPART_FORM_DATA.equals(representation.getMediaType(), true))
                {
                    // 1/ Create a factory for disk-based file items
                    DiskFileItemFactory factory = new DiskFileItemFactory();
                    factory.setSizeThreshold(1000240);

                    RestletFileUpload upload = new RestletFileUpload(factory);
                    FileItemIterator fileIterator = upload.getItemIterator(representation);

                    while (fileIterator.hasNext())
                    {
                        FileItemStream fi = fileIterator.next();
                        try (InputStream is = fi.openStream())
                        {
                            String fileName = FilenameUtils.getBaseName(fi.getName());
                            String ext = FilenameUtils.getExtension(fi.getName());
                            File file = new File(DataUploadServerResource.tempDirectory.getAbsolutePath(), String.format("%s.%s", fileName, ext));

                            if (file.exists())
                            {
                                //noinspection ResultOfMethodCallIgnored
                                file.delete();
                            }
                            Files.copy(is, file.toPath());
                            try
                            {
                                PropertyExtractor propertyExtractor = new PropertyExtractor(file);
                                result = propertyExtractor.getProperties();
                                result.put("fileName", file.getName());
                            }
                            catch (Exception ex)
                            {
                                Environment.getInstance().getReportHandler().warning(ex);
                            }
                        }
                        catch (Exception ex)
                        {
                            Environment.getInstance().getReportHandler().severe(ex);
                            setStatus(Status.SERVER_ERROR_INTERNAL);
                        }
                    }
                }
                else
                {
                    // POST request with no entity.
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().warning(ex);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return (null != result) ? result.toJsonRepresentation() : null;
    }

    @Override
    public Representation update(Representation representation)
    {
        return null;
    }
}

