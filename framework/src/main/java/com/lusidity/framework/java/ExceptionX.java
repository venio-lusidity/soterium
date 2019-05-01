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

package com.lusidity.framework.java;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionX
{
	/**
	 * Private constructor. This is a utility class and cannot be instantiated.
	 */
    private ExceptionX()
    {
	    super();
    }

	/**
	 * Dump an exception to a String.
	 * @param event Throwable event.
	 * @return Throwable event formatted into a string.
	 */
    public static String dump(Throwable event)
    {
	    ByteArrayOutputStream byteStream=new ByteArrayOutputStream();
	    PrintStream printStream=new PrintStream(byteStream);
		ExceptionX.dump(event, printStream);
	    return byteStream.toString();
    }

	private static
	void dump(Throwable event, PrintStream printStream)
	{
		printStream.format("%s\n", event.toString());
		event.printStackTrace(printStream);
		Throwable cause=event.getCause();
		if (null!=cause)
		{
			printStream.format("caused by ");
			ExceptionX.dump(cause, printStream);
		}
	}
}
