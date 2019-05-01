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

package com.lusidity.framework.math;

public class Numbers
{
    private Numbers()
    {
	    super();
    }
/*************************************************************************************
 * Static Methods
 *************************************************************************************/
    /**
     * Calculates the nth generalized harmonic number. See "Harmonic Series" at http://mathworld.wolfram
     * .com/HarmonicSeries.html for more information.
     *
     * @param n
     *     the term in the series to calculate (must be \u2265 1)
     * @param m
     *     the exponent; special case m == 1.0 is the harmonic series
     * @return the nth generalized harmonic number
     */
    //	TODO: Contribute this to Apache Commons Math library
    //	from http://www.devdaily.com/java/jwarehouse/commons-math/src/main/java/org/apache/commons/math/distribution
    // /ZipfDistributionImpl.java.shtml.
    public static double generalizedHarmonic(int n, double m)
    {
        double value = 0;
        for (int k = n; k > 0; --k)
        {
            value += 1.0 / Math.pow(k, m);
        }
        return value;
    }
}
