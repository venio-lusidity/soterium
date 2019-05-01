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


import com.lusidity.framework.json.JsonData;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.omg.CORBA.Environment;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;

public class SoteriumClient {

    public SoteriumClient() throws Exception {
        super();
    }

    public JsonData get(KeyStoreManager ksm, URI endpoint) throws Exception {
        JsonData result = JsonData.createObject();
        try
        {
            HttpClient httpClient=this.getClient(ksm);

            HttpResponse response=httpClient.execute(new HttpGet(endpoint));
            if (null!=response)
            {
                result=this.getData(response);
            }
        }
        catch (Exception ex){
            result.put("response_status", "failed");
            result.put("response_msg", ex.getMessage());
        }
        return result;
    }

    public JsonData post(KeyStoreManager ksm, URI endpoint, Object content) throws Exception {
        JsonData result = null;

        HttpClient httpClient = this.getClient(ksm);

        HttpPost post = new HttpPost(endpoint);
        HttpEntity postEntity = null;
        String contentType = "text/plain";
        if (content instanceof File) {
            File file = (File) content;
            postEntity = new FileEntity(file);
            contentType = UtilsX.getContentType(file);
        } else if (content instanceof JsonData) {
            postEntity = new StringEntity(content.toString());
        }

        post.setEntity(postEntity);
        post.setHeader("Content-Type", contentType);

        HttpResponse response = httpClient.execute(post);
        if (null != response) {
            result = this.getData(response);
        }

        return result;
    }

    public JsonData postImporterRmk(KeyStoreManager ksm, URI endpoint, File file) throws Exception {
        JsonData result = null;

        HttpClient client = this.getClient(ksm);

        HttpPost post = new HttpPost(endpoint);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file,
                ContentType.APPLICATION_OCTET_STREAM,
                file.getName());
        HttpEntity multipart = builder.build();
        post.setEntity(multipart);

        HttpResponse response = client.execute(post);
        if (null != response) {
            result = this.getData(response);
        }

        return result;
    }

    public HttpClient getClient(KeyStoreManager ksm) throws Exception {
        KeyStore ks = SoteriumClient.getKeyStore(ksm);
        KeyStore ts = SoteriumClient.getTrustStore(ksm);

        SSLContext sslContext = SSLContexts.custom()
                                           .loadKeyMaterial(ks, ksm.getKeyStorePwd().toCharArray())
                                           .loadTrustMaterial(ksm.getTrustStore(), ksm.getTrustStorePwd().toCharArray())
                                           .build();

        return HttpClients.custom().setSSLContext(sslContext).build();
    }

    private static KeyStore getKeyStore(KeyStoreManager ksm) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ksm.getKeyStoreType());
        try(FileInputStream fis = new FileInputStream(ksm.getKeyStore()))
        {
            keyStore.load(fis, ksm.getKeyStorePwd().toCharArray());
        }
        return keyStore;
    }

    private static KeyStore getTrustStore(KeyStoreManager ksm) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(ksm.getTrustStoreType());
        try(FileInputStream fis = new FileInputStream(ksm.getTrustStore()))
        {
            trustStore.load(fis, ksm.getTrustStorePwd().toCharArray());
        }
        return trustStore;
    }

    private JsonData getData(HttpResponse response) throws IOException
    {
        HttpEntity entity = response.getEntity();
        String data = EntityUtils.toString(entity);
        return new JsonData(data);
    }
}
