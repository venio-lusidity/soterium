/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.services.security;


import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

public
class Tracer
	extends Restlet
{
	public
	Tracer(Context context)
	{
		super(context);
	}

	/**
	 * Returns the Uri or IP Address of the requesting client.
	 *
	 * @param request
	 * 	The http request.
	 * @param response
	 * 	The http response.
	 */
	@SuppressWarnings("RefusedBequest")
	@Override
	public
	void handle(Request request, Response response)
	{
		String inboundIdentity =
			(null == request.getClientInfo().getFrom()) ? request.getClientInfo().getAddress() : request.getClientInfo().getFrom();
		response.setEntity(inboundIdentity, MediaType.TEXT_PLAIN);
	}
}
