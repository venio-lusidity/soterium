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

package com.lusidity.framework.math.units;

public class Storage
{
// ------------------------------ FIELDS ------------------------------

    /** Bytes per kilobyte. */
    public static final long KILOBYTES = 1024;

    /** Bytes per megabyte. */
    public static final long MEGABYTES = Storage.KILOBYTES * 1024L;

    /** Bytes per gigabyte. */
    public static final long GIGABYTES = Storage.MEGABYTES * 1024L;

    /** Bytes per terabyte. */
    public static final long TERABYTES = Storage.GIGABYTES * 1024L;

    /** Bytes per petabyte. */
    public static final long PETABYTES = Storage.TERABYTES * 1024L;

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Private constructor for utility class.
	 */
    private Storage()
    {
	    super();
    }
}
