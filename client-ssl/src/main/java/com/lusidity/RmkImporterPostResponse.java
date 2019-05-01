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

package com.lusidity;

import com.lusidity.framework.internet.UriBuilderX;
import com.lusidity.framework.json.JsonData;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;

public class RmkImporterPostResponse extends BaseResponse {

    public static final String DITRP = "com.lusidity.rmk.importer.enclave.DITPRImporter";
    public static final String COAMS_LOCATIONS = "com.lusidity.rmk.importer.coams.COAMSLocationImporter";
    public static final String COAMS_ORGANIZATIONS = "com.lusidity.rmk.importer.coams.COAMSOrgImporter";
    public static final String COMMON_VULNERABILITES_AND_EXPOSURES = "com.lusidity.rmk.importer.cve.CVEImporter";
    public static final String IAVM = "com.lusidity.rmk.importer.iavm.IAVMImporter";
    public static final String MICROSOFT_SECURITY_BULLETIN = "com.lusidity.rmk.importer.msb.MSBImporter";
    public static final String STIG = "com.lusidity.rmk.importer.stig.StigImporter";
    public static final String DPAS = "com.lusidity.rmk.importer.dpas.DPASImporter";
    public static final String HBSS_ALL = "com.lusidity.rmk.importer.hbss.HbssAssetsImporter," +
            "com.lusidity.rmk.importer.hbss.HbssOpsImporter," +
            "com.lusidity.rmk.importer.hbss.HbssStigImporter," +
            "com.lusidity.rmk.importer.hbss.HbssIavmImporter";
    public static final String HBSS_ASSETS = "com.lusidity.rmk.importer.hbss.HbssAssetsImporter";
    public static final String HBSS_IAVM = "com.lusidity.rmk.importer.hbss.HbssIavmImporter";
    public static final String HBSS_OPERATIONAL = "com.lusidity.rmk.importer.hbss.HbssOpsImporter";
    public static final String HBSS_STIG = "com.lusidity.rmk.importer.hbss.HbssStigImporter";
    public static final String NESSUS = "com.lusidity.rmk.importer.nessus.NessusReportImporter";
    public static final String SCCM = "com.lusidity.rmk.importer.msc.SCCMImporter";
    

    public RmkImporterPostResponse(JsonData response, URI endpointUri) throws IOException {
        super(response, endpointUri);
    }


    public String getExtension(){
        return this.getData().getString("extension");
    }

    public String getVertexId(){
        return this.getData().getString("vertexId");
    }

    public String getId(){
        return this.getData().getString("lid");
    }

    public String getVertexUri(){
        return this.getData().getString("/vertex/uri");
    }

    public String getTitle(){
        return this.getData().getString("title");
    }

    public String getImportType(){
        return this.getData().getString("type");
    }

    public String getCommandType(){
        return this.getData().getString("commandTypes");
    }

    public String getOriginalFileName(){
        return this.getData().getString("originalName");
    }

    public String getStatus(){
        return this.getData().getString("status");
    }

    public long getSizeInBytesPosted(){
        return this.getData().getLong("bytes");
    }

    public long getSizeInKbPosted(){
        return this.getData().getLong("kb");
    }

    /**
     * Post a file to the file importer endpoint.
     * @param config The configuration file.
     * @param file The file to post.
     * @param clsName The full class name of the importer to use.
     * @return Information about the posted file.
     * @throws Exception
     */
    public static RmkImporterPostResponse post(ClientConfiguration config, File file, String clsName) throws Exception {
        RmkImporterPostResponse result = null;

        if(!file.exists()){
            throw new InvalidParameterException(String.format("The file %s, does not exist.", file.getAbsolutePath()));
        }

        KeyStoreManager ksm = new KeyStoreManager(
                config.getKeystorePwd(),
                config.getKeystore(),
                config.getTrustStorePwd(),
                config.getTrustStore());

        AuthorizationResponse authorizationResponse = AuthorizationResponse.authenticate(config);

        if((null!=authorizationResponse) && !authorizationResponse.isRegistered()) {
            RegisterResponse ipr = RegisterResponse.post(config);
            if (null != ipr) {
                if (ipr.isRegistered()) {
                    authorizationResponse = AuthorizationResponse.authenticate(config);
                } else {
                    throw new Exception(ipr.getMessage());
                }
            } else {
                throw new Exception("Sorry we don't know what happened the response was null.");
            }
        }

        if(null!=authorizationResponse) {
            if (authorizationResponse.isAuthenticated()) {
                SoteriumClient client = new SoteriumClient();

                UriBuilderX builder = new UriBuilderX(config.getBaseServerUrl());
                builder.addPath(config.getEndpointPostRmkImporterFile());
                builder.addParameter("apiKey", config.getApiKey());
                builder.addParameter("type", clsName);
                builder.addParameter("uri", config.getPrincipalUri());

                URI uri = URI.create(builder.toString());
                JsonData response = client.postImporterRmk(ksm, uri, file);
                if((null!=response) && response.isValid()){
                    result = new RmkImporterPostResponse(response, uri);
                }
            }
            else if(!authorizationResponse.isValidated()){
                throw new Exception("The certificate used was not recognized.");
            }
            else if(!authorizationResponse.isRegistered()){
                throw new Exception("The API key is not registerd.");
            }
            else {
                throw new Exception("Sorry we don't know what happened the response was null.");
            }
        }

        return result;
    }
}
