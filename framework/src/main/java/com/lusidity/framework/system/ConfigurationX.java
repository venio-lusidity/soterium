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

package com.lusidity.framework.system;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.text.StringX;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public
class ConfigurationX
{
    private static final Pattern PATTERN_PAIR_SEPARATOR = Pattern.compile(";");
    private static final Pattern PATTERN_KEY_VALUE_SEPARATOR = Pattern.compile("=");

    private ConfigurationX()
    {
        super();
    }

    /**
     * Parse a semicolon-delimited list of key/value pairs (e.g., "key1=value1;key2=value2")
     * @param str String to parse.
     * @return Map of key/value pairs.
     */
    public static
    Map<String, String> parse(String str)
        throws ApplicationException
    {
        Map<String, String> results=new HashMap<String, String>();
        if (!StringX.isBlank(str))
        {
            String[] array = ConfigurationX.PATTERN_PAIR_SEPARATOR.split(str);
            for (String entry : array)
            {
                String[] parts= ConfigurationX.PATTERN_KEY_VALUE_SEPARATOR.split(entry);
                if (parts.length!=2)
                {
                    throw new ApplicationException("Missing (or extra) '='.");
                }
                String key=parts[0].trim();
                String value=parts[1].trim();
                results.put(key, value);
            }

        }
        return results;
    }
}
