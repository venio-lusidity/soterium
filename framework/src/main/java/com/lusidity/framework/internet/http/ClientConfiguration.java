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

package com.lusidity.framework.internet.http;

import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.security.ObfuscateX;
import com.lusidity.framework.text.StringX;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;

@SuppressWarnings("unused")
public class ClientConfiguration
{

    private static final String SEED = "16beadt8-efbf-4d0d-8t8b-2b258t05bb24";
    private static String[] keys = null;
    
    static {
        ClientConfiguration.keys = new String[] {
                "apiKey",
                "keystore",
                "keystorePwd",
                "trustStore",
                "trustStorePwd",
                "principalUri",
                "baseServerUrl",
                "endpointGetAuthenticate",
                "endpointPostRmkImporterFile",
                "endpointPostRegister"
        };
    }

	private boolean ignoreMissingValues= false;

	private JsonData data=null;

	// Constructors
	public ClientConfiguration(File jsonFile, boolean ignoreBadCertificates) throws Exception {
		super();
		this.load(jsonFile, ignoreBadCertificates);
	}

	public ClientConfiguration(File jsonFile, boolean ignoreMissingValues, boolean ignoreBadCertificates) throws Exception {
		super();
		this.ignoreMissingValues=ignoreMissingValues;
		this.load(jsonFile, ignoreBadCertificates);
	}

	// Methods
	public static ClientConfiguration create(String configPath, boolean ignoreBadCertificates)
		throws Exception
	{
		String path=UtilsX.getParentDirectory();
		File file;
		if (StringX.contains(configPath, path))
		{
			file=new File(configPath);
		}
		else
		{
			file=new File(path, configPath);
		}
		return new ClientConfiguration(file, false, ignoreBadCertificates);
	}

    private void load(File jsonFile, boolean ignoreBadCertificates) throws Exception {
        if(null==jsonFile){
            throw new InvalidParameterException("The jsonFile cannot be null.");
        }
        if(!jsonFile.exists()){
            throw new InvalidParameterException(
                    String.format("There is no configuration file at %s", jsonFile.getAbsolutePath()));
        }

        this.data = new JsonData(jsonFile);

	    this.validate();

        if(!this.data.getBoolean("obfuscated")){
            String kp = this.data.getString("keystorePwd");
            String tp = this.data.getString("trustStorePwd");
            this.data.remove("keystorePwd");
            this.data.remove("trustStorePwd");
            this.data.remove("seed");
            this.data.remove("obfuscated");
            UUID seed = UUID.randomUUID();
            this.data.put("seed", ObfuscateX.obfuscate(seed.toString(), ClientConfiguration.SEED, null));
            this.data.put("obfuscated", true);
            this.data.put("keystorePwd", ObfuscateX.obfuscate(kp, seed.toString(), null));
            this.data.put("trustStorePwd", ObfuscateX.obfuscate(tp, seed.toString(), null));
            boolean saved = this.data.save(new File(jsonFile.getParent()), StringX.getFirst(jsonFile.getName(), "."), true);
            if(saved) {
                this.load(jsonFile, ignoreBadCertificates);
            }
            else{
                throw new IOException(String.format("Could not save file at %s", jsonFile.getAbsolutePath()));
            }
        }
        else if(ignoreBadCertificates && this.data.getBoolean("disableCertificateValidation")){
            this.disableCertificateValidation();
        }
    }

    private void validate() throws InvalidObjectException {
        StringBuilder missing = new StringBuilder();
        for(String key: ClientConfiguration.keys){
            if(!this.data.hasKey(key) || StringX.isBlank(this.data.getString(key))){
                if(missing.length()>0){
                    missing.append(", ");
                }
                missing.append(key);
            }
        }
        if(!this.ignoreMissingValues && (missing.length()>0)){
            throw new InvalidObjectException(
                    String.format("The configuration file is missing some properties. (%s)", missing.toString()));
        }
    }

	@SuppressWarnings("AnonymousInnerClassWithTooManyMethods")
	private void disableCertificateValidation()
		throws KeyManagementException, NoSuchAlgorithmException
	{
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts={
			new X509TrustManager()
			{
				// Overrides
				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType)
				{
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType)
				{
				}

				@SuppressWarnings("ZeroLengthArrayAllocation")
				@Override
				public X509Certificate[] getAcceptedIssuers()
				{
					return new X509Certificate[0];
				}
			}
		};
		// Ignore differences between given hostname and certificate hostname
		HostnameVerifier hv=new HostnameVerifier()
		{
			// Overrides
			@Override
			public boolean verify(String hostname, SSLSession session)
			{
				return true;
			}
		};
		// Install the all-trusting trust manager
		SSLContext sc=SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	// Getters and setters
	public boolean isValid() {
		boolean result = true;
		for(String key: ClientConfiguration.keys){
			if(!this.data.hasKey(key) || StringX.isBlank(this.data.getString(key))){
				result = false;
			}
			if(!result){
				break;
			}
		}
		return result;
	}

    public String getApiKey() {
        return this.data.getString("apiKey");
    }

    public String getBaseServerUrl() {
        return this.data.getString("baseServerUrl");
    }

    public String getEndpointGetAuthenticate() {
        return this.data.getString("endpointGetAuthenticate");
    }

    public String getEndpointPostRmkImporterFile() {
        return this.data.getString("endpointPostRmkImporterFile");
    }

    public String getEndpointPostRegister(){
        return this.data.getString("endpointPostRegister");
    }

    public String getKeystore() {
        return this.data.getString("keystore");
    }

    public String getKeystorePwd() throws Exception {
        String result = null;
        String value = this.data.getString("keystorePwd");
        if(!StringX.isBlank(value) && !StringX.isBlank(this.getSeed())){
            result = ObfuscateX.decrypt(value, this.getSeed(), null);
        }
        return result;
    }

	private String getSeed()
		throws Exception
	{
		String result=null;
		String value=this.data.getString("seed");
		if (!StringX.isBlank(value))
		{
			result=ObfuscateX.decrypt(value, ClientConfiguration.SEED, null);
		}
		return result;
	}

    public String getPrincipalUri() {
        return this.data.getString("principalUri");
    }

    public String getTrustStore() {
        return this.data.getString("trustStore");
    }

    public String getTrustStorePwd() throws Exception {
        String result = null;
        String value = this.data.getString("trustStorePwd");
        if(!StringX.isBlank(value) && !StringX.isBlank(this.getSeed())){
            result = ObfuscateX.decrypt(value, this.getSeed(), null);
        }
        return result;
    }
}
