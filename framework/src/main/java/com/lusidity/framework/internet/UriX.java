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

package com.lusidity.framework.internet;

import com.lusidity.framework.text.StringX;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jjszucs
 * Date: 7/24/12
 * Time: 15:28
 */
public class UriX
{
    /**
     * Private constructor. This is a utility class and cannot be instantiated.
     */
    private UriX()
    {
	    super();
    }

    public static boolean isValidUri(String str)
    {
        boolean result;
        try
        {
            @SuppressWarnings("UnusedDeclaration")
            URI uri=new URI(str);
            result = true;
        }
        catch (URISyntaxException ignored)
        {
            result = false;
        }
        return result;
    }

    public static String getParam(URI uri, String param)
            throws MalformedURLException {
        return (null!=uri) ? UriX.getParam(uri.toURL(), param) : null;
    }

    public static String getParam(URL url, String param)
    {
        return UriX.getParams(url).get(param);
    }

    private static Map<String, String> getParams(URI uri)
            throws MalformedURLException {
        return UriX.getParams(uri.toURL());
    }

    private static Map<String, String> getParams(URL url) {
        Map<String, String> results = new HashMap<>();
        if((null!=url))
        {
            String query = url.getQuery();
            if(!StringX.isBlank(query)) {
                String[] params = query.split("&");
                for(String param: params)
                {
                    String[] pair = param.split("=");
                    if((pair.length == 2) && !StringX.isBlank(pair[0]) && !StringX.isBlank(pair[1]))
                    {
                        results.put(StringX.urlDecode(pair[0], "UTF-8"), StringX.urlDecode(pair[1], "UTF-8"));
                    }
                }
            }

        }
        return results;
    }
}
