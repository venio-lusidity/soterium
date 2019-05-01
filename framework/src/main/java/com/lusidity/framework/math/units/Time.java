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

package com.lusidity.framework.math.units;

public
class Time
{
	/**
	 * Private constructor for utility class.
	 */
	private Time()
	{
		super();
	}

	public static final long MILLIS_PER_SECOND = 1000;
	public static final long SECONDS_PER_MINUTE = 60;
	public static final long MINUTES_PER_HOUR = 60;
	public static final long HOURS_PER_DAY = 24;
	public static final long DAYS_PER_WEEK = 7;

	public static long secondsToMillis(long seconds)
	{
		return seconds*Time.MILLIS_PER_SECOND;
	}

	public static long minutesToMillis(long minutes)
	{
		return Time.secondsToMillis(minutes*Time.SECONDS_PER_MINUTE);
	}

	public static long hoursToMillis(long hours)
	{
		return Time.minutesToMillis(hours*Time.MINUTES_PER_HOUR);
	}

	public static long daysToMillis(long days)
	{
		return Time.hoursToMillis(days*Time.HOURS_PER_DAY);
	}

	public static long weeksToMillis(long weeks)
	{
		return Time.daysToMillis(weeks*Time.DAYS_PER_WEEK);
	}
}
