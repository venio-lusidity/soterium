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

package com.lusidity.services.server;

import com.lusidity.Environment;
import com.lusidity.data.interfaces.BaseServerConfiguration;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.restlet.data.Protocol;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

@SuppressWarnings("RedundantFieldInitialization")
public class WebServerConfig extends BaseServerConfiguration {
    private Set<String> allowedURIs = null;
    private final Map<String, String> headers = new HashMap<>();
    private final Collection<String> allowedClientCertificateTypes = new ArrayList<>();
    private final Collection<String> allowedCharacterEncodings = new ArrayList<>();
    private static WebServerConfig instance = null;

    @SuppressWarnings("unused")
    public WebServerConfig(){
        super();
        WebServerConfig.instance = this;
    }

    public WebServerConfig(JsonData jsonData) {
        super(jsonData);
        WebServerConfig.instance = this;
    }

    public static WebServerConfig getInstance()
    {
        return WebServerConfig.instance;
    }

	public boolean isValid() {
        return (null!=this.getProtocol()) &&
               ((!Objects.equals(this.getProtocol(), Protocol.HTTP)) || ((null!=this.getKeystore()) && this.getKeystore().isFile() && !StringX.isBlank(this.getKeystorePwd())))
               && (null!=this.getPort());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isEnabled() {
        return this.isOpened();
    }

    @Override
    public boolean transform(JsonData item) throws Exception {
        boolean result = false;
        if(null!=this.getConfigData()) {

            if (this.getConfigData().hasKey("allowedURIs")) {
                Collection<String> uris = this.getConfigData().getStringCollection("allowedURIs", "value");
                for (String uri : uris) {
                    this.getAllowedURIs().add(uri);
                }
            }

            if(this.getConfigData().hasKey("allowedClientCertificateTypes")){
                JsonData values = this.getConfigData().getFromPath("allowedClientCertificateTypes", "value");
                if((null!=values) && values.isJSONArray()){
                    for(Object o: values) {
                        if(o instanceof String){
                            this.getAllowedClientCertificateTypes().add(o.toString().toLowerCase());
                        }
                    }
                }
            }

            if(this.getConfigData().hasKey("allowedCharacterEncodings")){
                JsonData values = this.getConfigData().getFromPath("allowedCharacterEncodings", "value");
                if((null!=values) && values.isJSONArray()){
                    for(Object o: values){
                        if(o instanceof String){
                            this.allowedCharacterEncodings.add(o.toString());
                        }
                    }
                }
            }

            if(this.getConfigData().hasKey("headers"))
            {
                JsonData values=this.getConfigData().getFromPath("headers", "value");
                if((null!=values) && values.isJSONArray()){
                    for(Object o: values){
                        if(o instanceof String){
                            String[] parts = StringX.split(o.toString(), "::");
                            if((null!=parts) && (parts.length==2)){
                                this.headers.put(parts[0], parts[1]);
                            }
                        }
                    }
                }
            }

            result = true;
        }
        else{
            Environment.getInstance().getReportHandler().warning("The config data is null.");
        }
        return result;
    }

    protected Set<String> getAllowedURIs() {
        if(null==this.allowedURIs){
            this.allowedURIs = new CopyOnWriteArraySet<>();
        }
        return this.allowedURIs;
    }

    public WebServerConfig.AuthMode getAuthMode() {
        return this.getConfigData().getEnum(WebServerConfig.AuthMode.class, "authMode", "value");
    }

    protected File getKeystore() {
        String path = this.getConfigData().getString("keystore", "value");
        File result = BaseServerConfiguration.getFile(path);
        if(!result.exists() && result.getAbsolutePath().contains("/test/resource")){
            path = StringX.replace(result.getAbsolutePath(), "/test/resource", "/resource");
            result = new File(path);
        }
        return result;
    }

    protected String getKeystorePwd() {
        return this.getConfigData().getString("keystorePwd", "value");
    }

    protected File getTrusted() {
        String path = this.getConfigData().getString("trusted", "value");
        File result = BaseServerConfiguration.getFile(path);
        if(!result.exists() && result.getAbsolutePath().contains("/test/resource")){
            path = StringX.replace(result.getAbsolutePath(), "/test/resource", "/resource");
            result = new File(path);
        }
        return result;
    }

    protected String getTrustedPwd() {
        return this.getConfigData().getString("trustedPwd", "value");
    }

    public Integer getPort() {
        return this.getConfigData().getInteger("port", "value");
    }

    public Collection<String> getAllowedClientCertificateTypes() {
        return this.allowedClientCertificateTypes;
    }

    public String getWebPath() {
        return this.getConfigData().getString("webPath", "value");
    }

    public String getCacheExpiresAt()
    {
        return this.getConfigData().getString("cache", "expiresAt", "value");
    }

    public enum AuthMode {
        x509,
        basic,
        digest,
        form,
        azureACS,
        none
    }

	public Collection<String> getAllowedCharacterEncodings()
	{
		return this.allowedCharacterEncodings;
	}

    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    public Level getLogLevel(){
        String result = this.getConfigData().getString("logLevel", "value");
        if(StringX.isBlank(result)){
            result = "OFF";
        }
        return Level.parse(result);
    }

    public Level getRestletLogLevel(){
        String result = this.getConfigData().getString("restletLogLevel", "value");
        if(StringX.isBlank(result)){
            result = "OFF";
        }
        return Level.parse(result);
    }
}