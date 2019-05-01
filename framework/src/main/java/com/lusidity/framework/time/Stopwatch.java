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

package com.lusidity.framework.time;

import com.lusidity.framework.math.MathX;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Stopwatch.
 *
 * @author jjszucs
 */
public class Stopwatch
{
    /**
     * ********************************************************************************** Fields
     * ***********************************************************************************
     */
    protected DateTime startedWhen;
    protected DateTime stoppedWhen;
    private boolean started = false;
/*************************************************************************************
 * Constructors
 *************************************************************************************/
    /** Default constructor. */
    public Stopwatch()
    {
        super();
        this.startedWhen = null;
        this.stoppedWhen = null;
    }

    public String elapsedToString()
    {
        return (null==this.elapsed()) ? "00:00:00" : TimeX.fromMillis(this.elapsed().getMillis());
    }

    public String elapsedToMillisString()
    {
        return (null==this.elapsed()) ? "No time recorded." : String.valueOf(this.elapsed().getMillis());
    }

    public static Stopwatch begin()
    {
        Stopwatch result = new Stopwatch();
        result.start();
        return result;
    }

/*************************************************************************************
 * Getter/Setter Methods
 *************************************************************************************/
    /**
     * Get date/time when stopwatch was started.
     *
     * @return DateTime when stopwatch was started.
     */
    public DateTime getStartedWhen()
    {
        return this.startedWhen;
    }

    /**
     * Get date/time when stopwatch was stopped.
     *
     * @return DateTime when stopwatch was stopped.
     */
    public DateTime getStoppedWhen()
    {
        return this.stoppedWhen;
    }

/*************************************************************************************
 * Other Methods
 *************************************************************************************/
    @Override
    public String toString() {
        return (this.elapsed()!=null) ? String.format("elapsed: %s", this.elapsed().toString()) : super.toString();
    }

    /**
     * Get duration since start.
     *
     * @return Duration.
     */
    public Duration elapsed()
    {
        Duration result = null;
        if((null!=this.getStartedWhen()))
        {
            DateTime now=(null!=this.stoppedWhen) ? this.stoppedWhen : DateTime.now();
            result = this.elapsed(now);
        }
        return result;
    }

    /** Start. */
    public void start()
    {
        this.started = true;
        this.startedWhen = DateTime.now();
    }

    /** Stop. */
    public Duration stop()
    {
        Duration result = null;
        if(this.isStarted())
        {
            this.stoppedWhen=DateTime.now();
            this.started=false;
            result = this.elapsed(this.stoppedWhen);
        }
        return (null==result) ? Duration.ZERO : result;
    }

    /**
     * Get elapsed time between start time and specified time.
     *
     * @param untilWhen
     *     Time to measure.
     * @return Duration between start time and specified time.
     */
    public Duration elapsed(DateTime untilWhen)
    {

        return (null==this.startedWhen) ? null : new Duration(this.startedWhen, untilWhen);
    }

    public boolean isStarted() {
        return this.started;
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public long getPerSecond(int items)
    {
        return MathX.getPerSecond(this.elapsed().getStandardSeconds(), items);
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public long getPerSecond(long items)
    {
        return MathX.getPerSecond(this.elapsed().getStandardSeconds(), items);
    }
}
