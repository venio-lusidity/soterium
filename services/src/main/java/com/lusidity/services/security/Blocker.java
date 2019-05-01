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

package com.lusidity.services.security;

import com.lusidity.Environment;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.server.WebServerConfig;
import com.lusidity.services.server.WebServices;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.*;
import org.restlet.routing.Filter;
import org.restlet.util.Series;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

public class Blocker
	extends Filter
{
// ------------------------------ FIELDS ------------------------------

	@SuppressWarnings("FieldHasSetterButNoGetter")
	private static Set<String> allowableURIs = new HashSet<>();

	/**
	 * A value of 0 means not blocked.
	 */
	private int blocked=Filter.STOP;

// --------------------------- CONSTRUCTORS ---------------------------

// Constructors
	public Blocker(Context context)
	{
		super(context);
		Blocker.allowableURIs=new CopyOnWriteArraySet<>();
	}

// -------------------------- OTHER METHODS --------------------------

// Overrides
	@Override
	protected void afterHandle(Request request, Response response)
	{
		try
		{
			Blocker.setHeaders(request, response);
			super.afterHandle(request, response);
		}
		catch (Exception ignored)
		{
		}
	}

	@SuppressWarnings("MethodWithMultipleReturnPoints")
	@Override
	public int beforeHandle(Request request, Response response)
	{
		try
		{
			if (Environment.getInstance().isDebugMode()){
				Environment.getInstance().getReportHandler().info("Blocker called");
			}
			Blocker.setHeaders(request, response);
			String origin=request.getOriginalRef().toString().toLowerCase();

			Protocol protocol=WebServices.getInstance().getProtocol();
			if (!request.getProtocol().equals(protocol))
			{
				if (protocol.equals(Protocol.HTTPS))
				{
					response.setStatus(
						Status.CLIENT_ERROR_FORBIDDEN, "The address that you requested does not support SSL."
					);
				}
				else
				{
					response.setStatus(
						Status.CLIENT_ERROR_FORBIDDEN,
						"The address that you requested requires SSL.  Try using https."
					);
				}
			}
			else if (this.isAuthorized(request) || StringX.endsWith(origin, "/svc/ping")
			         || !StringX.containsIgnoreCase(origin, "/svc"))
			{
				if (Environment.getInstance().isDebugMode()){
					Environment.getInstance().getReportHandler().info("Blocker request is authorized.");
				}
				this.blocked=Filter.CONTINUE;
			}
			else
			{
				if (Environment.getInstance().isDebugMode())
				{
					String referrerDomain=(null!=request.getReferrerRef()) ?
						request.getReferrerRef().getHostDomain() : "unknown";

					String rootRefDomain=(null!=request.getRootRef()) ?
						request.getRootRef().getHostDomain() : "unknown";
					String oDomain=(null!=request.getOriginalRef()) ?
						request.getOriginalRef().getHostDomain() : "unknown";
					String hDomain=(null!=request.getHostRef()) ? request.getHostRef().getHostDomain() : "unknown";
					String resourceRefDomain=(null!=request.getResourceRef()) ?
						request.getResourceRef().getHostDomain() : "unknown";

					String msg = "Your location was blocked."+
					             "  The original domain was "+oDomain+'.'+
					             "  The host domain was "+hDomain+'.'+
					             "  The root reference domain was "+rootRefDomain+'.'+
					             "  The resource reference domain was "+resourceRefDomain+'.'+
					             "  The referrer domain was "+referrerDomain+'.';

					Environment.getInstance().getReportHandler().info(msg);

					response.setStatus(
						Status.CLIENT_ERROR_FORBIDDEN, msg
					);
				}
				else
				{
					response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().critical(ex);
			this.blocked=Filter.STOP;
		}

		if (this.blocked!=0)
		{
			Reference origin=request.getOriginalRef();
			Series<Header> headers=request.getHeaders();
			StringBuilder msg=new StringBuilder();
			for (Header header : headers)
			{
				if (msg.length()>0)
				{
					msg.append("\r\n");
				}
				msg.append(header.getName()).append(": ").append(header.getValue());
			}
			Environment.getInstance().getReportHandler().severe("A request was blocked: %s\n\r%s", origin.toString(), msg.toString());
		}
		return this.blocked;
	}

	@Override
	public int doHandle(Request request, Response response)
	{
		if (!Method.OPTIONS.equals(request.getMethod()))
		{
			super.doHandle(request, response);
		}
		return this.blocked;
	}

	private boolean isAuthorized(Request request)
	{
		String address=(null!=request.getReferrerRef()) ? request.getReferrerRef().getHostDomain() : request.getHostRef().getHostDomain();
		return (Blocker.allowableURIs.contains(address));
	}

	public static void setHeaders(Request request, Response response)
	{
		String origin = request.getHeaders().getFirstValue("Origin");
		if(StringX.isBlank(origin)){
			origin = request.getHeaders().getFirstValue("origin");
		}
		Reference reference;
		if(!StringX.isBlank(origin)){
			reference = new Reference(origin);
		}
		else{
			reference = (null==request.getReferrerRef()) ? request.getOriginalRef() : request.getReferrerRef();
		}

		if (null!=reference)
		{
			String host=reference.getHostDomain();
			String referer=String.format("%s://%s", reference.getSchemeProtocol().getSchemeName(), host);
			// The port will come back as a -1 if no specified port.
			int port=reference.getHostPort();
			if ((port>0) && (port!=443) && (port!=80) && (port!=WebServerConfig.getInstance().getPort()))
			{
				referer+=String.format(":%d", port);
			}

			response.setAccessControlAllowOrigin(referer);
		}
		else
		{
			response.setAccessControlAllowOrigin("*");
		}

		Set<String> allowHeaders=new TreeSet<>();
		allowHeaders.add("X-FileInfo-Title");
		allowHeaders.add("X-FileInfo-Size");
		allowHeaders.add("X-FileInfo-Type");
		allowHeaders.add("X-Version");
		allowHeaders.add("referer");

		response.setAccessControlAllowHeaders(allowHeaders);

		Set<Method> allowMethods=new TreeSet<>();
		allowMethods.add(Method.GET);
		allowMethods.add(Method.DELETE);
		allowMethods.add(Method.POST);
		allowMethods.add(Method.PUT);
		allowMethods.add(Method.OPTIONS);

		response.setAccessControlAllowMethods(allowMethods);
		response.setAccessControlAllowCredentials(true);
		response.setAge(60);

		Map<String, String> entries=WebServerConfig.getInstance().getHeaders();

		for (Map.Entry<String, String> entry : entries.entrySet())
		{
			Form form=(Form) response.getAttributes().get("org.restlet.http.headers");
			if (null==form)
			{
				form=new Form();
				response.getAttributes().put("org.restlet.http.headers", form);
			}
			form.add(entry.getKey(), entry.getValue());
		}
	}

	private static void report(String host, Reference ref)
	{
		try
		{
			Environment.getInstance().getReportHandler().info("%s %s", ref.getHostDomain());
		}
		catch (Exception ignored)
		{
		}
	}

	public void setAllowableURIs(@SuppressWarnings("TypeMayBeWeakened") Set<String> allowableURIs)
	{
		for (String uri : allowableURIs)
		{
			Blocker.allowableURIs.add(uri);
		}
	}
}
