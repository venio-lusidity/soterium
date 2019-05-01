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

package com.lusidity.framework.internet.http;

public class HttpHeaders
{
    /** Retry-After. Value may be an integer number of seconds or an HTTP-date value. */
    public static final String RETRY_AFTER = "Retry-After";
	public static final String ACCEPT= "Accept";

    private HttpHeaders()
    {
	    super();
    }
}
