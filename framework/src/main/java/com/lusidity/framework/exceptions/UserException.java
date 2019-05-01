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

/**
 * This class represents an exception attributable to the user. It can be used to drive user-facing error handling
 * routines.
 */
public class UserException
    extends Exception
{
/*************************************************************************************
 * Constructors
 *************************************************************************************/
    /** Default constructor. */
    public UserException()
    {
        super();
    }

    /**
     * Initializing constructor.
     *
     * @param message
     *     Message.
     */
    public UserException(String message)
    {
        super(message);
    }

    /**
     * Initializing constructor.
     *
     * @param cause
     *     Cause.
     */
    public UserException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Initializing constructor.
     *
     * @param message
     *     Message.
     * @param cause
     *     Cause.
     */
    public UserException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
