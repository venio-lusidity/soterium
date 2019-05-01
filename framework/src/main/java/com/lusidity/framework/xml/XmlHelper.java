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

package com.lusidity.framework.xml;

import com.lusidity.framework.exceptions.ApplicationException;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * User: jjszucs
 * Date: 7/25/12
 * Time: 13:21
 */
public class XmlHelper
{
	private
	XmlHelper()
	{
	}

	/**
	 * Parse an XML value.
	 *
	 * @param cls
	 *     Type of value.
	 * @param value
	 *     Value; null if value could not be parsed.
	 * @return
	 *      Parsed XML value of appropriate type.
	 */
	public static Object parseValue(Class cls, String value)
	    throws ApplicationException
	{
		if (cls.isAssignableFrom(String.class))
	    {
	        return value;
	    }
	    else if (cls.isAssignableFrom(Boolean.class) || cls.equals(boolean.class))
	    {
	        return Boolean.parseBoolean(value);
	    }
	    else if (cls.isAssignableFrom(Double.class) || cls.equals(double.class))
	    {
	        return Double.parseDouble(value);
	    }
	    else if (cls.isAssignableFrom(Float.class) || cls.equals(float.class))
	    {
	        return Float.parseFloat(value);
	    }
	    else if (cls.isAssignableFrom(Long.class) || cls.equals(long.class))
	    {
	        return Long.parseLong(value);
	    }
	    else if (cls.isAssignableFrom(Short.class) || cls.equals(short.class))
	    {
	        return Short.parseShort(value);
	    }
	    else if (cls.isAssignableFrom(Integer.class) || cls.equals(int.class))
	    {
	        return Integer.parseInt(value);
	    }
	    else if (cls.isAssignableFrom(Duration.class))
	    {
	        return Duration.parse(value);
	    }
	    else if (cls.isAssignableFrom(DateTime.class))
	    {
	        return DateTime.parse(value);
	    }
	    else if (cls.isAssignableFrom(URI.class))
	    {
	        try
	        {
	            return new URI(value);
	        }
	        catch (URISyntaxException e)
	        {
	            throw new ApplicationException(e);
	        }
	    }
	    else
	    {
	        throw new ApplicationException("Cannot parse type '%s'.", cls.toString());
	    }
	}
}
