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

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.json.JsonDataEntity;
import com.lusidity.framework.text.StringX;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


@SuppressWarnings({
	"MagicNumber",
	"NestedConditionalExpression"
})
public final class HttpClientXPool implements Closeable {

    private final HttpHost httpHost;
	private final KeyStoreManager keyStoreManager;
	private int maxRoute = 20;
    private int limit = 200;
    private CloseableHttpClient httpClient = null;
    private PoolingHttpClientConnectionManager poolManager = null;

    public HttpClientXPool(int limit, int maxRoute, HttpHost httpHost, KeyStoreManager keyStoreManager)
	    throws Exception
    {
        super();
        this.limit = limit;
        this.maxRoute = maxRoute;
        this.httpHost = httpHost;
        this.keyStoreManager = keyStoreManager;
        this.load();
    }

    private void load()
	    throws Exception
    {
        this.poolManager = new PoolingHttpClientConnectionManager();
	    this.poolManager.setDefaultMaxPerRoute(this.maxRoute);
	    this.poolManager.setMaxTotal(this.limit);
	    this.poolManager.setMaxPerRoute(new HttpRoute(this.httpHost), this.maxRoute);

	    this.httpClient=HttpClients.custom().setConnectionManager(this.poolManager).build();
    }

    public JsonData getResponse(URL url, HttpClientX.Methods method, Object content, String referer, String... headers) {
        JsonData result;
        StringBuilder msg = new StringBuilder();
        int code = 500;

        try {
            HttpUriRequest request = null;
            HttpEntity entity = null;
	        //noinspection ChainOfInstanceofChecks
	        if(content instanceof String){
                entity = new StringEntity(content.toString());
            }
            else if(content instanceof JsonData){
                entity = new JsonDataEntity(content.toString());
            }
	        //noinspection SwitchStatementWithoutDefaultBranch,EnumSwitchStatementWhichMissesCases
	        switch (method) {
                case POST:
                    request = new HttpPost(url.toString());
                    if(null!=entity) {
                        ((HttpEntityEnclosingRequest) request).setEntity(entity);
                    }
                    break;
                case PUT:
                    request = new HttpPut(url.toString());
                    if(null!=entity) {
                        ((HttpEntityEnclosingRequest) request).setEntity(entity);
                    }
                    break;
                case DELETE:
                	if(null==entity){
		                request = new HttpDelete(url.toString());
	                }
	                else {
	                	request =  new HttpDeleteWithEntity(URI.create(url.toString()));
		                ((HttpEntityEnclosingRequest) request).setEntity(entity);
	                }
                    break;
                case GET:
                    if(null==entity) {
                        request = new HttpGet(url.toString());
                    }
                    else{
                        request = new HttpGetWithEntity(URI.create(url.toString()));
                        ((HttpEntityEnclosingRequest) request).setEntity(entity);
                    }
                    break;
            }

            if(null!=request){
                this.createHeaders(referer, request, headers);
                try (CloseableHttpResponse response = this.httpClient.execute(request)) {
                    HttpEntity httpEntity = response.getEntity();
                    result = this.getResponse(msg, httpEntity);

                    StatusLine statusLine = response.getStatusLine();
                    msg.append(statusLine.getReasonPhrase());
                    code = statusLine.getStatusCode();

                    result.put("http_response_status", code);
	                result.put("http_response_message", (msg.length() > 0) ? msg.toString() : (((code >= 200) && (code <= 299)) ? "OK" : "ERROR"));
                    result.put("http_ok", ((code >= 200) && (code <= 299)));
                } catch (Exception ex) {
                    result = null;
                    msg.append(String.format("\n%s: %s\n", ex.getClass().getName(), ex.getMessage()));
                }
            }
            else{
	            //noinspection ThrowCaughtLocally
	            throw new ApplicationException("Invalid request type.");
            }
        }
        catch(Exception ex){
            result = null;
            msg.append(String.format("\n%s: %s\n", ex.getClass().getName(), ex.getMessage()));
        }

        if(null==result){
            result = JsonData.createObject();
            result.put("http_response_status", code);
            result.put("http_response_message", (msg.length() > 0) ? msg.toString() : (((code >= 200) && (code <= 299)) ? "OK" : "ERROR"));
        }

        return result;
    }

    @SuppressWarnings("NestedAssignment")
    private JsonData getResponse(StringBuilder msg, HttpEntity httpEntity) {
        StringBuilder response = new StringBuilder();
	    try (InputStreamReader isr = new InputStreamReader(httpEntity.getContent())) {
            try (BufferedReader br = new BufferedReader(isr)) {
	            String inputLine;
	            while ((inputLine = br.readLine())!=null) {
                    response.append(inputLine);
                }
            } catch (Exception ex) {
                msg.append(ex.getMessage());
            }
        } catch (Exception ex) {
            msg.append(ex.getMessage());
        }

        JsonData result = null;
        if(response.length()>0) {
        	String data = response.toString().trim();
        	if(StringX.startsWith(data, "{") || StringX.startsWith(data, "["))
	        {
		        result=new JsonData(response.toString());
	        }
	        else{
        		result = JsonData.createObject().put("content", data);
	        }
        }

        return result;
    }

    private void createHeaders(String referer, HttpUriRequest request, String... headers) {
        request.addHeader("Referer", referer);

        if ((null != headers) && (headers.length > 0)) {
            int nHeaders = headers.length;
            if ((nHeaders % 2) != 0) {
                throw new IllegalArgumentException("Headers must be key/value pairs expressed as String pairs.");
            }
            for (int keyIndex = 0; keyIndex < (nHeaders - 1); keyIndex++) {
                int valueIndex = keyIndex + 1;
                String key = headers[keyIndex];
                String value = headers[valueIndex];
                request.addHeader(key, value);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if(null!=this.httpClient){
            this.httpClient.close();
        }
	    if(null!=this.poolManager){
		    this.poolManager.close();
	    }
    }

    public SSLConnectionSocketFactory getIgnoreCertificate()
	    throws Exception
    {
	    SSLContextBuilder builder = new SSLContextBuilder();
	    builder.loadTrustMaterial(null, new TrustStrategy() {
		    public boolean isTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
			    return true;
		    }
	    });
	    return new SSLConnectionSocketFactory(builder.build());
    }

    public SSLContext getSSLContext(KeyStoreManager ksm) throws Exception {
    	// example.
    	//https://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https

        //KeyStore ks = HttpClientXPool.getKeyStore(ksm);
        KeyStore ts = HttpClientXPool.getTrustStore(ksm);


	    TrustManagerFactory tmf = TrustManagerFactory
		    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
		// Using null here initialises the TMF with the default trust store.
	    tmf.init((KeyStore) null);

		// Get hold of the default trust manager
	    X509TrustManager defaultTm = null;
	    for (TrustManager tm : tmf.getTrustManagers()) {
		    if (tm instanceof X509TrustManager) {
			    defaultTm = (X509TrustManager) tm;
			    break;
		    }
	    }


	    tmf = TrustManagerFactory
		    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(ts);

		// Get hold of the default trust manager
	    X509TrustManager myTm = null;
	    for (TrustManager tm : tmf.getTrustManagers()) {
		    if (tm instanceof X509TrustManager) {
			    myTm = (X509TrustManager) tm;
			    break;
		    }
	    }

		// Wrap it in your own class.
	    final X509TrustManager finalDefaultTm = defaultTm;
	    final X509TrustManager finalMyTm = myTm;
	    X509TrustManager customTm = new X509TrustManager() {
		    @Override
		    public X509Certificate[] getAcceptedIssuers() {
			    // If you're planning to use client-cert auth,
			    // merge results from "defaultTm" and "myTm".
			    return finalDefaultTm.getAcceptedIssuers();
		    }

		    @Override
		    public void checkServerTrusted(X509Certificate[] chain,
		                                   String authType) throws CertificateException {
			    try {
				    finalMyTm.checkServerTrusted(chain, authType);
			    } catch (CertificateException e) {
				    // This will throw another CertificateException if this fails too.
				    finalDefaultTm.checkServerTrusted(chain, authType);
			    }
		    }

		    @Override
		    public void checkClientTrusted(X509Certificate[] chain,
		                                   String authType) throws CertificateException
		    {
			    // If you're planning to use client-cert auth,
			    // do the same as checking the server.
			    finalDefaultTm.checkClientTrusted(chain, authType);
		    }
	    };


	    SSLContext sslContext = SSLContext.getInstance("TLS");
	    sslContext.init(null, new TrustManager[] { customTm }, null);

		// You don't have to set this as the default context,
	    // it depends on the library you're using.
	    SSLContext.setDefault(sslContext);
	    return sslContext;
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
}
