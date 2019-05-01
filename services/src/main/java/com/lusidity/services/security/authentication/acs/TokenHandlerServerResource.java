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

import com.lusidity.Environment;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

@AtWebResource(pathTemplate = "/svc/acs/azure/tokenhandler", matchingMode = AtWebResource.MODE_FIRST_MATCH,
		methods ="get",
        description = "Uses Azure ACS to get a client.")
@AtAuthorization(required = false)
public
class TokenHandlerServerResource
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
		return null;
	}

	@Override
	public Representation store(Representation representation)
	{
		return null;
	}

	@Override
	public Representation update(Representation representation)
	{
		String returnUrl = this.getQuery().getValues("returnUrl");
		try
		{
			if (!StringX.isBlank(returnUrl))
			{
				this.getAccessControlHandler().handle(representation);
				Reference reference = new Reference(returnUrl);

				this.getResponse().redirectPermanent(reference);
			}
			else
			{
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No url specified to return to.");
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().critical(ex);
		}
		return null;
	}
}
