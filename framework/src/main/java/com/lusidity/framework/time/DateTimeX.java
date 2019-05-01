/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.framework.time;

import com.lusidity.framework.regex.RegExHelper;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Objects;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class DateTimeX {
    private static final String[] FORMATS = {"E MMM dd HH mm ss yyyy", "MM dd yyyy hh mm ss aa", "dd MMM yyyy", "MM dd yyyy", "dd MM yyyy" };
    public static final Integer NUM_DAYS_IN_YEAR=365;

	public static DateTime parse(String value) {
        DateTime result = null;
        String fValue = value;
        if(!StringX.isBlank(fValue)) {
            try {
                result = DateTime.parse(fValue);
            } catch (Exception ignored) {
                fValue = StringX.replace(fValue, "(", "");
                fValue = StringX.replace(fValue, ")", "");
                fValue = StringX.replace(fValue, ".", " ");
                fValue = StringX.replace(fValue, "/", " ");
                fValue = StringX.replace(fValue, "-", " ");
                fValue = StringX.replace(fValue, ":", " ");
                fValue = StringX.replace(fValue, "  ", " ");
                fValue = StringX.replace(fValue, "   ", " ");
                for (String format : DateTimeX.FORMATS) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
                        result = DateTime.parse(fValue, formatter);
                        break;
                    } catch (Exception ignore) {
                    }
                }
            }

            if (null == result) {
                if(null!=ReportHandler.getInstance()) {
                    ReportHandler.getInstance().severe("Could not parse DateTime %s.", fValue);
                }
            }
        }

        return result;
    }

    public enum UnitOfTime{
        days,
	    hours,
        milliseconds,
	    minutes,
	    months,
        seconds,
        weeks,
	    years
    }
    public static DateTime getDateTimeFromStamp(String stampInSeconds){
        DateTime dt = null;
        try{
            Long stampLong = Long.valueOf(stampInSeconds);
            dt = new DateTime(Long.valueOf(stampLong * 1000), DateTimeZone.UTC);
        } catch(Exception ignore) {}
        return dt;
    }
    @SuppressWarnings("OverlyComplexMethod")
    public static DateTime getDateTime(DateTime from, DateTimeX.UnitOfTime unitOfTime, int span){
        DateTime result = null;
        if((null != from) && (null!=unitOfTime)){
            switch (unitOfTime){
                case days:
                    result = from.plusDays(span);
                    break;
                case hours:
                    result = from.plusHours(span);
                    break;
                case milliseconds:
                    result = from.plusMillis(span);
                    break;
                case minutes:
                    result = from.plusMinutes(span);
                    break;
                case months:
                    result = from.plusMonths(span);
                    break;
                case seconds:
                    result = from.plusSeconds(span);
                    break;
                case weeks:
                    result = from.plusWeeks(span);
                    break;
                case years:
                    result = from.plusYears(span);
                    break;
            }
        }
        return result;
    }

    public static
    DateTime addTimeFrom(DateTime value, UnitOfTime unitOfTime, Integer span)
    {
        DateTime result = null;
        if(null!=value)
        {
            switch (unitOfTime)
            {
                case hours:
                    result=value.plusHours(span);
                    break;
                case minutes:
                    result=value.plusMinutes(span);
                    break;
                case days:
                    result=value.plusDays(span);
                    break;
                case months:
                    result=value.plusMonths(span);
                    break;
                case years:
                    result=value.plusYears(span);
                    break;
            }
        }
        return result;
    }

	/**
     * Are the days the same?
     * @param lastDate Last day recorded
     * @return true or false
     */
    public static boolean isSameDay(DateTime lastDate)
    {
        DateTime now = new DateTime(lastDate.getZone());
        //noinspection ConstantConditions
        return (null!=lastDate) && (lastDate.getDayOfYear()==now.getDayOfYear());
    }

    public static boolean isSameDay(DateTime a, DateTime b)
    {
        //noinspection ConstantConditions
        return (null!=a) && (null!=b) && (a.getDayOfYear()==b.getDayOfYear());
    }

    public static boolean isSameDayOrAfter(DateTime a, DateTime b){
        boolean result = DateTimeX.isSameDay(a, b);
        if(!result){
            result = a.isAfter(b);
        }
        return result;
    }

	/**
	 * Simple before and after operations
	 * @param dateTime the base DateTime
	 * @param dateToCompare the DateTime to compare
	 * @return true or false
	 */
    public static boolean isBefore(DateTime dateTime, DateTime dateToCompare){
        return dateTime.isBefore(dateToCompare);
    }
    public static boolean isAfter(DateTime dateTime, DateTime dateToCompare){
        return dateTime.isAfter(dateToCompare);
    }
    /**
     * Is the actual time the same or after the expected time of day.
     * @param actual A date time value to evaluate the timeOfDay.
     * @param timeOfDay 24 hour time of day in the format of HH:mm, 14:32
     * @return true of false
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isSameOrAfterTimeOfDay(DateTime actual, String timeOfDay)
    {
        boolean result = false;
        if(!StringX.isBlank(timeOfDay)){
            try{
                String[] parts = StringX.split(timeOfDay, ":");
                int hour = Integer.parseInt(parts[0]);
                int min = Integer.parseInt(parts[1]);
                int ah = actual.getHourOfDay();
                int am = actual.getMinuteOfHour();
                result = (ah>hour) || ((ah>=hour) && (am>=min));
            }
            catch (Exception ex){
                ReportHandler.getInstance().info("The time of day is not in the expected format, HH:mm, found %s", timeOfDay);
            }
        }
        return result;
    }

    public static DateTime tomorrowStartOfDay()
    {
        return DateTimeX.todayStartOfDay().plusDays(1).withTimeAtStartOfDay();
    }

    private static DateTime todayStartOfDay()
    {
        return new DateTime().withTimeAtStartOfDay();
    }

    public static DateTime beginingOfTime()
    {
        return new DateTime(0000,1,1,0,0,0, DateTimeZone.UTC);
    }

    public static int compare(DateTime value1, DateTime value2)
    {
        int result=0;

        if (Objects.equals(value1, value2))
        {
            result=0;
        }
        else if (null==value1)
        {
            result=-1;
        }
        else if (null==value2)
        {
            result=1;
        }
        else
        {
            result=value1.compareTo(value2);
        }
        return result;
    }

    public static boolean is24HourTime(String time)
    {
        return !StringX.isBlank(time) && RegExHelper.TIME_24_HOURS_PATTERN.matcher(time).matches();
    }

    public static boolean equals(DateTime a, DateTime b)
    {
        int compare = DateTimeX.compare(a, b);
        return (compare==0);
    }
}
