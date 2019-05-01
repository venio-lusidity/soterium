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

package com.lusidity.framework.security;

import com.lusidity.framework.exceptions.ApplicationException;
import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("UnusedDeclaration")
public class DigestX
{
    private DigestX()
    {
	    super();
    }

    /**
     * Get Base64-encoded MD5 hash for a byte array.
     * @param bytes Bytes to hash.
     * @return Base64-encoded MD5 hash.
     */
    public static
    String base64MD5(byte[] bytes)
        throws ApplicationException
    {
        MessageDigest messageDigest;
        try
        {
            messageDigest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new ApplicationException(e);
        }
        byte[] md5=messageDigest.digest(bytes);

        Base64 base64Codec=new Base64();

	    return base64Codec.encodeAsString(md5);
    }
}
