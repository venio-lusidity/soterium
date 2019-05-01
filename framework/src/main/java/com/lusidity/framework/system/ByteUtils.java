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

import java.nio.ByteBuffer;

@SuppressWarnings("MethodCanBeVariableArityMethod")
public class ByteUtils {
    private static final int LONG_BYTES =8;
    private static final int DOUBLE_BYTES=8;

    /**
     * Private default constructor. This is a utility class and should not be instantiated.
     */
    private ByteUtils()
    {
        super();
    }

    public static byte[] toBytes(long l)
    {
        ByteBuffer bytes=ByteBuffer.allocate(8);
        bytes.asLongBuffer().put(l);
        return bytes.array();
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static byte[] toBytes(long[] la)
    {
        ByteBuffer bytes=ByteBuffer.allocate(ByteUtils.LONG_BYTES *la.length);
        bytes.asLongBuffer().put(la);
        return bytes.array();
    }

    public static byte[] toBytes(double d)
    {
        ByteBuffer bytes=ByteBuffer.allocate(8);
        bytes.asDoubleBuffer().put(d);
        return bytes.array();
    }

    public static byte[] toBytes(double[] da)
    {
        ByteBuffer bytes=ByteBuffer.allocate(ByteUtils.DOUBLE_BYTES *da.length);
        bytes.asDoubleBuffer().put(da);
        return bytes.array();
    }

    public static double toDouble(byte[] bytes)
    {
        ByteBuffer byteBuffer=ByteBuffer.wrap(bytes);
        return byteBuffer.asDoubleBuffer().get();
    }

    public static long toLong(byte[] bytes)
    {
        ByteBuffer byteBuffer=ByteBuffer.wrap(bytes);
        return byteBuffer.asLongBuffer().get();
    }
}
