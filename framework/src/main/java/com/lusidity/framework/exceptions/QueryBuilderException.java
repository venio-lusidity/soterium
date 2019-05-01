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

public class QueryBuilderException
    extends Exception
{
    private boolean isInternalError = true;
/*************************************************************************************
 * Constructors
 *************************************************************************************/
    /** Default constructor. */
    public QueryBuilderException()
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
    public QueryBuilderException(boolean isInternalError, String format, Object... params)
    {
        super(String.format(format, params));
        this.isInternalError = isInternalError;
    }

    /**
     * Initializing constructor.
     *
     * @param cause
     *     Cause.
     */
    public QueryBuilderException(Throwable cause)
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
    public QueryBuilderException(Throwable cause, String format, Object... params)
    {
        super(String.format(format, params), cause);
    }

    public boolean isInternalError(){
        return this.isInternalError;
    }
}
