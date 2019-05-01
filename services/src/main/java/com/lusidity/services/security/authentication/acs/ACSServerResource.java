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
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.net.URI;
import java.net.URLEncoder;

@AtWebResource(pathTemplate = "/svc/acs/azure", matchingMode = AtWebResource.MODE_BEST_MATCH, methods = "get",
        description = "Uses Azure ACS to get a client.")
@AtAuthorization(required = false)
public class ACSServerResource
        extends BaseServerResource {
    private static final String ENDPOINT_BASE_HOST = "https://%s.accesscontrol.windows.net";
    private static final String ENDPOINT_RELATIVE_PATH =
            "/v2/metadata/IdentityProviders.js?protocol=wsfederation&realm=%s&reply_to=%s&context=&request_id=&version=1.0";


    @Override
    public Representation remove()
    {
        return this.getRepresentation(this.getAccessControlHandler().logout());
    }

    @Override
    public Representation retrieve()
    {
        JsonRepresentation result = null;
        String serviceNamespace = this.getQuery().getValues("serviceNamespace");
        String appId = this.getQuery().getValues("appId");
        String replyTo = this.getQuery().getValues("reply_to");
        if (!StringX.isBlank(serviceNamespace) && !StringX.isBlank(appId) && !StringX.isBlank(replyTo)) {
            try {
                replyTo = URLEncoder.encode(replyTo, "UTF-8");
                String host = String.format(ACSServerResource.ENDPOINT_BASE_HOST, serviceNamespace);
                String relativePath = String.format(ACSServerResource.ENDPOINT_RELATIVE_PATH, appId, replyTo);
                String response = HttpClientX.getString(new URI(host + relativePath));
                if (!StringX.isBlank(response)) {
                    result = new JsonRepresentation(response);
                }
            } catch (Exception ignore) {

            }
        }

        //noinspection VariableNotUsedInsideIf
        if (null == result) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return result;
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
