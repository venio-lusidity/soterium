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

import com.lusidity.framework.text.StringX;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.util.*;

/**
 * Math utility functions.
 */
public
class MathX
{
    /**
     * PI divided by 2.
     */
    public static final double HALF_PI =Math.PI/2.0;
	public static final int BYTE_FACTOR=1024;

    /**
     * Epsilon.
     */
    public static final double EPSILON = 1.0E-5;
	public static final int THOUSAND = 1000;
    public static final int MILLION = MathX.THOUSAND * MathX.THOUSAND;
	public static final long BINARY_KILO = 1024;
	public static final long BINARY_MEGA = MathX.BINARY_KILO * MathX.BINARY_KILO;
	public static final long BINARY_GIGA = MathX.BINARY_MEGA * MathX.BINARY_KILO;
    public static final int MINUTES_IN_HOUR = 60;
    public static final int HOURS_IN_DAY = 24;

	public static final long BYTES_IN_GB = 1073741824;
	public static final long BYTES_IN_MB = 1048576;
	public static final double MAX_PERCENTILE=100.0;

	/** Utility class; cannot be instantiated. */
	private
	MathX()
	{
		super();
	}

	/**
	 * Logarithm in arbitrary base.
	 *
	 * @param x
	 * 	Value.
	 * @param b
	 * 	Base.
	 *
	 * @return Base-b logarithm of x.
	 */
	public static
	double log(double x, double b)
	{
		return StrictMath.log(x) / StrictMath.log(b);
	}

	/**
	 * Base-2 logarithm.
	 *
	 * @param x
	 * 	Value.
	 *
	 * @return Base-2 logarithm of x.
	 */
	public static
	double log2(double x)
	{
		return Math.log(x) / MathX.NATURAL_LOG_2;
	}

    public static
    double log10(double x)
    {
        return Math.log10(x);
    }

    /**
     * Natural logarithm of 2 (from Wolfram|Alpha).
     */
    public static final double NATURAL_LOG_2 =0.6931471805599453094172321214581765680755001343602552;

    /**
     * Clamp a value to an (inclusive) range.
     * @param n Value to clamp.
     * @param min Lower bounds.
     * @param max Upper bounds.
     * @return Clamped value.
     */
    public static
    double clamp(double n, double min, double max)
    {
        return Math.max(min, Math.min(max, n));
    }

    public static final Vector2D POSITIVE_Y_2D =new Vector2D(0, 1);
    public static final Vector2D NEGATIVE_Y_2D =new Vector2D(0, -1);
    public static final Vector2D POSITIVE_X_2D =new Vector2D(1, 0);
    public static final Vector2D NEGATIVE_X_2D =new Vector2D(-1, 0);

    public static final Vector2D NORTH_2D = MathX.POSITIVE_Y_2D;
    public static final Vector2D SOUTH_2D = MathX.NEGATIVE_Y_2D;
    public static final Vector2D EAST_2D = MathX.POSITIVE_X_2D;
    public static final Vector2D WEST_2D = MathX.NEGATIVE_Y_2D;

	/**
	 * Multiply the specified value by 1,000. This is useful for more readable constants.
	 * @param i Value.
	 * @return Value multiplied by 1,000.
	 */
	public static
	int thousands(int i)
	{
		return i*MathX.THOUSAND;
	}

	/**
	 * Are two values equal, within epsilon?
	 * @param v1 Value 1.
	 * @param v2 Value 2.
	 * @param epsilon Epsilon.
	 * @return true if value 1 and 2 are equal within epsilon.
	 */
	public static
	boolean equals(double v1, double v2, double epsilon)
	{
		return Math.abs(v1-v2)<epsilon;
	}

	public static Double truncate(double x,int numberofDecimals) {
		BigDecimal working=null;
		if ( x > 0) {
			working=new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
		} else {
			working=new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
		}
		return working.doubleValue();
	}

	public static
	Float toKiloBytes(long sizeInBytes)
	{
		//noinspection UnnecessaryBoxing
		return Float.valueOf(sizeInBytes/1024);
	}

	public static
	Float toMegaBytes(long sizeInBytes)
	{
		//noinspection UnnecessaryBoxing
		return MathX.toKiloBytes(sizeInBytes)/1024;
	}

	public static
	Float toGigaBytes(long sizeInBytes)
	{
		//noinspection UnnecessaryBoxing
		return MathX.toMegaBytes(sizeInBytes)/1024;
	}

	public static long getPerSecond(long seconds, long count)
	{
		long result;
		try
		{
			long fs = (seconds<=0) ? 1 : seconds;
			result = Math.floorDiv(count, fs);
		} catch (Exception ignored)
		{
			result = 1;
		}
		return (result<=0) ? 1 : result;
	}

	/**
	 * format a decimal as a long for math.
	 * @param value the value to convert to a long
	 * @param places how many decimals to keep or add 000 to,
	 *                  ex .79 with places of 4 would be "7900",
	 *                  ex .79346 would be 7934
	 *                  ex 1.0 would be 0000 = 10000
	 * @return
	 */
	public static long makeLong(double value, int places)
	{
		Long result =0L;
		String test = String.valueOf(value);
		if(!StringX.isBlank(test)){
			String dblVal = StringX.getLast(test, ".");
			if(dblVal.length()<places)
			{
				StringBuffer sb = new StringBuffer();
				int add = places-dblVal.length();
				for (int i=0; i<add; i++)
				{
					sb.append("0");
				}
				test = sb.toString();
			}

			if(dblVal.length()>places){
				int remove = dblVal.length()-places;
				test = test.substring(0, test.length()-remove);
			}
			test = StringX.replace(test, ".", "");
			result = Long.valueOf(test);
		}
		return result;
	}

	public static Double percentile(DescriptiveStatistics statistics, double score)
	{
		int pos = Arrays.binarySearch(statistics.getSortedValues(), score);
		return (((pos<0) ? (-1-pos) : pos)* MathX.MAX_PERCENTILE)/statistics.getN();
	}

	public static int getRandomNumber()
	{
		return MathX.getRandomNumber(1, 1000);
	}

	public static int getRandomNumber(int min, int max)
	{
		@SuppressWarnings("UnsecureRandomNumberGeneration")
		Random r = new Random();
		return (r.nextInt(max-min) + min);
	}
	public static <T extends Number & Comparable<T>> double median(Collection<T> numbers){
		if(numbers.isEmpty()){
			throw new IllegalArgumentException("Cannot compute median on empty collection of numbers");
		}
		List<T> numbersList = new ArrayList<>(numbers);
		Collections.sort(numbersList);
		int middle = numbersList.size()/2;
		if((numbersList.size()%2)==0){
			return 0.5 * (numbersList.get(middle).doubleValue() + numbersList.get(middle-1).doubleValue());
		} else {
			return numbersList.get(middle).doubleValue();
		}

	}
	public static <T extends Number & Comparable<T>> double average(Collection<T> numbers){
		if(numbers.isEmpty()){
			throw new IllegalArgumentException("Cannot compute median on empty collection of numbers");
		}
		List<Double> list = new ArrayList<>();
		List<T> numbersList = new ArrayList<>(numbers);
		for (T aNumbersList : numbersList)
		{
			double d=aNumbersList.doubleValue();
			list.add(d);
		}
		return list.stream().mapToDouble(i -> i).average().orElse(0);

	}
}