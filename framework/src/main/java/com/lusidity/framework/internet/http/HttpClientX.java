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

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.text.TextEncoding;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.framework.xml.LazyXmlNode;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public
class HttpClientX
{
// ------------------------------ FIELDS ------------------------------

	private static final int SOCKET_TIMEOUT = 100;
	private static final int REQUEST_TIMEOUT = 100;
	private static final int CONNECTION_TIMEOUT = 100;
	@SuppressWarnings("UnusedDeclaration")
	private static final int HTTP_RETRY_WAIT = 100;
	private static final int MIN_OK = 200;
	private static final int MAX_OK = 299;

    private static final int MAX_QUEUED = 500;

// -------------------------- STATIC METHODS --------------------------

    /**
     * Utility class.
     */
    private HttpClientX() {
        super();
    }

	/**
	 * Everything in the 200 range means that the server will response with some result. Whether you like the response or not is up to you.
     * <p/>
     * Also note that if the server that you are trying to reach does not allow head requests this will return a result of false even
     * though the
	 * server may still be available.
	 *
	 * @param url
	 * 	The URL of the site to contact.
	 *
	 * @return true if the url is reachable.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public static
	boolean isReachable(URL url)
	{
		boolean result = true;
		try
		{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if ((responseCode < HttpClientX.MIN_OK) || (responseCode > HttpClientX.MAX_OK))
			{
				result = false;
			}
		}
		catch (Exception ignored)
		{
			result = false;
		}
		return result;
	}

	/**
	 * Dynmaically build a URI.
	 *
	 * @param protocol
	 * 	Protocol.
	 * @param baseUri
	 * 	Host name.
	 * @param relativePath
	 * 	Relative path.
	 * @param queryParams
	 * 	Query parameters.
	 */
	public static
	URI buildUri(HttpClientX.Protocol protocol, String baseUri, String relativePath, String... queryParams)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(protocol.toString().toLowerCase());
		sb.append("://");

		String s = StringX.removeEnd(baseUri, "/");
		sb.append(s);

		sb.append('/');
		s = StringX.removeStart(relativePath, "/");
		sb.append(s);

		int n = queryParams.length;
		if ((n % 2) != 0)
		{
			throw new IllegalArgumentException("Query parameters must be expressed as key/value pairs.");
		}

		for (int i = 0; i < n; i += 2)
		{
			if (i == 0)
			{
				sb.append('?');
			}
			else
			{
				sb.append('&');
			}
			String key = queryParams[i];
			String value = queryParams[i + 1];
			sb.append(key);
			sb.append('=');
			sb.append(value);
		}

		return URI.create(sb.toString());
	}

	/**
	 * Execute a simple HTTP GET with custom headers.
	 *
	 * @param uri
	 * 	URI.
	 *
	 * @return Response, as a string.
	 */
	public static
	String getString(URI uri)
	{
		return HttpClientX.getString(uri, (String[]) null);
    }

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Execute a simple HTTP GET with custom headers. http://hc.apache.org/httpcomponents-client-4.3.x/examples.html
	 *
	 * @param uri
	 * 	URI.
	 * @param headers
	 * 	Request headers; may be null.
	 *
	 * @return Response, as a string.
	 */
	public static
	String getString(URI uri, String... headers)
	{
		String result = null;
		try(CloseableHttpClient httpClient = HttpClients.createDefault())
		{

			HttpGet httpGet = new HttpGet(uri);

			RequestConfig requestConfig =
				RequestConfig.custom()
					    .setConnectionRequestTimeout(HttpClientX.REQUEST_TIMEOUT)
                        .setExpectContinueEnabled(true)
                        .setCookieSpec(CookieSpecs.DEFAULT)
                        .build();

			httpGet.setConfig(requestConfig);

			if (null != headers)
			{
				int nHeaders = headers.length;
				if ((nHeaders % 2) != 0)
				{
					throw new IllegalArgumentException("Headers must be key/value pairs expressed as String pairs.");
				}
				for (int keyIndex = 0; keyIndex < (nHeaders - 1); keyIndex++)
				{
					int valueIndex = keyIndex + 1;
					String key = headers[keyIndex];
					String value = headers[valueIndex];
					httpGet.addHeader(key, value);
				}
			}

			try(CloseableHttpResponse httpResponse = httpClient.execute(httpGet))
            {
			    HttpEntity httpEntity = httpResponse.getEntity();
			    try(InputStream contentStream = httpEntity.getContent())
                {
			        result = IOUtils.toString(contentStream, TextEncoding.UTF8);
                }
                catch (Exception ex)
                {
                    result = ex.toString();
                }
            }
            catch (Exception ex)
            {
                result = ex.toString();
            }
		}
		catch (Exception ex)
		{
            result = ex.toString();
		}

		return result;
	}

    public static URL goSecure(URL url) {
        URL result = url;
        if(url.getProtocol().equals("http"))
        {
            try {
                String original = url.toString();
                original = StringX.replace(original, "http:", "https:");
                result = URI.create(original).toURL();
            }
            catch (Exception ignored){}
        }

        return result;
    }

	public static JsonData getResponse(String content, URL url, HttpClientX.Methods method, String referer, String... headers) {
		HttpContent c = null;
		if (null != content) {
			c = new ByteArrayContent("automatic", content.getBytes());
		}
		return HttpClientX.getResponse(url, method, c, referer, headers);
	}

    public static JsonData getResponse(URL url, HttpClientX.Methods method, HttpContent content, String referer, String... headers) {
        JsonData result = JsonData.createObject();
        StringBuilder msg = new StringBuilder();
        int code = 500;
        if(null!=url) {
            HttpURLConnection connection = null;
            try {
                connection = HttpClientX.getHttpURLConnection(url);
                HttpClientX.createHeaders(method, referer, headers, connection);
                HttpClientX.sendContent(content, msg, connection);
                StringBuffer response = HttpClientX.getResponse(msg, connection);

                if (response.length() > 0) {
                    result = new JsonData(response.toString());
                }


                msg.append(connection.getResponseMessage());
                code = connection.getResponseCode();

                result.put("http_response_status", code);
                result.put("http_response_message", (msg.length() > 0) ? msg.toString() : (((code >= 200) && (code <= 299)) ? "OK" : "ERROR"));
	            result.put("http_ok", ((code >= 200) && (code <= 299)));
            }
            catch (Exception ex)
            {
                result = null;
                msg.append(ex.getMessage());
                code = 500;
            }
            finally {
	            if(null!=connection){
	            	HttpClientX.getHeaders(connection, result);
	            }
            }
        }

        if(null==result){
            result = JsonData.createObject();
            result.put("http_response_status", code);
            //noinspection ConstantConditions
            result.put("http_response_message", (msg.length() > 0) ? msg.toString() : (((code >= 200) && (code <= 299)) ? "OK" : "ERROR"));
        }
        return result;
    }

	private static void getHeaders(HttpURLConnection connection, JsonData result)
	{
		try{
			JsonData values = JsonData.createArray();
			Map<String, List<String>> headers = connection.getHeaderFields();
			Set<String> keys = headers.keySet();
			for (String key : keys)
			{
				JsonData value = JsonData.createObject();
				value.put("key", key);
				value.put("value", headers.get(key));
			}
			result.put("response_headers", values);
		}
		catch (Exception ignored){}
	}

	private static StringBuffer getResponse(StringBuilder msg, HttpURLConnection connection) {
        StringBuffer response = new StringBuffer();
        String inputLine;
        try (InputStreamReader isr = new InputStreamReader(connection.getInputStream())) {
            try (BufferedReader br = new BufferedReader(isr)) {
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (Exception ex) {
                msg.append(ex.getMessage());
            }
        } catch (Exception ex) {
            msg.append(ex.getMessage());
        }

        return response;
    }

    private static void sendContent(HttpContent content, StringBuilder msg, HttpURLConnection connection) throws IOException {
        if (null != content) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", content.getType());
            connection.setRequestProperty("Content-Length", String.format("%d", content.getLength()));

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                content.writeTo(wr);
                wr.flush();
            } catch (Exception ex) {
                msg.append(ex.getMessage());
            }
        }
    }

    private static void createHeaders(HttpClientX.Methods method, String referer, String[] headers, HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod(method.toString());
        connection.setRequestProperty("X-HTTP-Method-Override", method.toString());
        connection.setRequestProperty("Referer", referer);
        connection.setDefaultUseCaches(false);

        if ((null != headers) && (headers.length > 0)) {
            int nHeaders = headers.length;
            if ((nHeaders % 2) != 0) {
                throw new IllegalArgumentException("Headers must be key/value pairs expressed as String pairs.");
            }
            for (int keyIndex = 0; keyIndex < (nHeaders - 1); keyIndex++) {
                int valueIndex = keyIndex + 1;
                String key = headers[keyIndex];
                String value = headers[valueIndex];
                connection.setRequestProperty(key, value);
            }
        }
    }

    private static HttpURLConnection getHttpURLConnection(URL url) throws ApplicationException {
        HttpURLConnection connection = null;
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        do {
            try {
                String protocol = url.getProtocol();
                if (protocol.equals("http")) {
                    connection = (HttpURLConnection) url.openConnection();
                } else {
                    connection = (HttpsURLConnection) url.openConnection();
                }
            } catch (Exception ex) {
                if(stopwatch.elapsed().getMillis()>60000){
                    throw new ApplicationException(ex);
                }
            }
        }
        while (null==connection);
        return connection;
    }

    public static URL createURL(String url) {
        URL result = null;
        try{
            result = new URL(url);
        }
        catch (Exception ignored){}
        return result;
    }

    public static boolean isValidResponse(JsonData response) {
        boolean result = (null != response);
        if(result && response.isValid()){
            int status = 0;
            if(response.hasKey("http_response_status")){
               status = response.getInteger("http_response_status");
            }
            else if(response.hasKey("status")){
                status = response.getInteger("status");
            }
            result = (status>=200) && (status<300);
        }
        return result;
    }

    public static LazyXmlNode getXML(URI url) throws ApplicationException {
        String response = HttpClientX.getString(url);
	    LazyXmlNode result = null;
	    if(!StringX.isBlank(response))
	    {
		     result = LazyXmlNode.load(response, false);
	    }
	    return result;
    }

// -------------------------- ENUMERATIONS --------------------------

	public
	enum Protocol
	{
		HTTP,
		HTTPS
	}

    public static enum Methods
    {
        OPTIONS, HEAD, GET, POST, PUT, menthod, DELETE
    }
}
