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

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.security.InvalidParameterException;

public class KeyStoreManager {

    private final File trustStore;
    private final String keyStorePwd;
    private final File keyStore;
    private final String trustStorePwd;

    public KeyStoreManager(String keyStorePwd, File keyStore, String trustStorePwd, File trustStore){
        super();
        this.keyStorePwd = keyStorePwd;
        this.keyStore = keyStore;
        this.trustStorePwd = trustStorePwd;
        this.trustStore = trustStore;
    }

    public KeyStoreManager(String keyStorePwd, String keyStorePath, String trustStorePwd, String trustStorePath) {
        super();

        String path = UtilsX.getParentDirectory();

        File kf = new File(path, keyStorePath);
        File tf = new File(path, trustStorePath);

        if(!kf.exists()){
            throw new InvalidParameterException(String.format("The keystore does not exist at %s.", kf.getAbsolutePath()));
        }

        if(!tf.exists()){
            throw new InvalidParameterException(String.format("The trust store does not exist at %s.", tf.getAbsolutePath()));
        }

        this.keyStorePwd = keyStorePwd;
        this.keyStore = kf;
        this.trustStorePwd = trustStorePwd;
        this.trustStore = tf;
    }

    public static KeyStoreManager make(ClientConfiguration config)
        throws Exception
    {
        return  new KeyStoreManager(
            config.getKeystorePwd(),
            config.getKeystore(),
            config.getTrustStorePwd(),
            config.getTrustStore()
        );
    }

	protected File getKeyStore() {
        return this.keyStore;
    }

    protected String getKeyStorePwd() {
        return this.keyStorePwd;
    }

    protected File getTrustStore() {
        return this.trustStore;
    }

    protected String getTrustStorePwd() {
        return this.trustStorePwd;
    }

    public String getKeyStoreType() {
        String result = "PKCS12";
        if(StringUtils.endsWithIgnoreCase(this.getKeyStore().toString(), "jks")){
            result = "JKS";
        }
        return result;
    }

    public String getTrustStoreType() {
        String result = "PKCS12";
        if(StringUtils.endsWithIgnoreCase(this.getTrustStore().toString(), "jks")){
            result = "JKS";
        }
        return result;
    }
}
