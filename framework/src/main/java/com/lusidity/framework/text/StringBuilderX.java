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

package com.lusidity.framework.text;

public class StringBuilderX
{
    private StringBuilderX()
    {
	    super();
    }

    /**
     * Append the specified string to a StringBuilder, inserting the specified
     * separator before the newly-appended string if the StringBuilder is not
     * empty. This can be used to build human-readable names and other sequences.
     * @param sb StringBuilder.
     * @param str String to append.
     * @param separator Separator (may be blank or null for no separator).
     */
    public static
    void smartAppend(StringBuffer sb, String str, String separator)
    {
        if (!StringX.isBlank(str))
        {
            if (sb.length()>0)
            {
                if (null!=separator)
                {
                    sb.append(separator);
                }
            }
            sb.append(str);
        }
    }
}
