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

package com.lusidity.framework.exceptions;

public class ApplicationException
    extends Exception
{
/*************************************************************************************
 * Constructors
 *************************************************************************************/
    /** Default constructor. */
    public ApplicationException()
    {
        super();
    }

    /**
     * Initializing constructor.
     *
     * @param format
     *     Message format, as used by String.format().
     * @param params
     *     Message parameters.
     */
    public ApplicationException(String format, Object... params)
    {
        super(String.format(format, params));
    }

    /**
     * Initializing constructor.
     *
     * @param cause
     *     Cause.
     */
    public ApplicationException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Initializing constructor.
     *
     * @param cause
     *     Cause.
     * @param format
     *     Message format, as used by String.format.
     * @param params
     *     Message parameters.
     */
    public ApplicationException(Throwable cause, String format, Object... params)
    {
        super(String.format(format, params), cause);
    }
}
