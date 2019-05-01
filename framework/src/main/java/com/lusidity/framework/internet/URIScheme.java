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

/** Internet protocol schemes. */
public class URIScheme
{
    /** HTTP. */
    public static final String HTTP = "http";

    /** HTTPS. */
    public static final String HTTPS = "https";

    /** FTP. */
    public static final String FTP = "ftp";

    /** File. */
    public static final String FILE = "file";

    private URIScheme()
    {
	    super();
    }
}
