/*
 * Copyright (c) 2008-2013, Venio, Inc.
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

package com.lusidity.services.security.authentication.acs;

import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.json.JsonData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
@AtWebResource(pathTemplate = "/svc/acs/azure/authenticated", matchingMode = AtWebResource.MODE_FIRST_MATCH, methods =
		"get",
        description = "Uses Azure ACS to get a client.")
@AtAuthorization(required = false)
public
class AuthorizationServerResource
	extends BaseServerResource
{
	@Override
	public Representation remove()
	{
		return null;
	}

	@Override
	public Representation retrieve()
	{
		JsonData result = null;
		try
		{
			result = this.getAccessControlHandler().getUserAsJSON();
			this.getResponse().setStatus(
				((null == result) || !this.getAccessControlHandler().isAuthenticated()) ? Status.CLIENT_ERROR_FORBIDDEN : Status.SUCCESS_OK
			);
		}
		catch (Exception ignore)
		{
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}

		if(null == result)
		{
			result =  JsonData.createObject();
		}

		return this.getRepresentation(result);
	}

	@Override
	public Representation store(Representation representation)
	{
		return null;
	}

	@Override
	public Representation update(Representation representation)
	{
		return null;
	}
}
