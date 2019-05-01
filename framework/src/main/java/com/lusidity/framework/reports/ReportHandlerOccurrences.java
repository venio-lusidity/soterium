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

package com.lusidity.framework.reports;

import com.lusidity.framework.system.logging.LoggerX;
import com.lusidity.framework.text.StringX;

import java.util.Objects;
import java.util.logging.Level;

public class ReportHandlerOccurrences
{
	private String message = null;
	private long occurrences = 0L;
	private Level level = null;

	// Constructors
	public ReportHandlerOccurrences(){

	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	public synchronized boolean log(LoggerX logger, Level level, String message)
	{
		boolean result=true;
		if (Objects.equals(this.getLevel(), level) && StringX.equals(message, this.getMessage()))
		{
			result=false;
			this.increment();
		}
		else
		{
			if((this.getOccurrences()>0) && (null!=this.getLevel()) && !StringX.isBlank(this.getMessage())){
				String msg = String.format("Occurred %d times. %s", this.getOccurrences(), this.getMessage());
				logger.log(this.getLevel(), msg);
			}
			this.setLevel(level);
			this.setMessage(message);
			this.setOccurrences(0L);
		}
		return result;
	}

	public synchronized Level getLevel()
	{
		return this.level;
	}

	public synchronized String getMessage()
	{
		return this.message;
	}
	
	private synchronized void increment()
	{
		this.occurrences++;
	}

	public synchronized long getOccurrences()
	{
		return this.occurrences;
	}
	
	@SuppressWarnings("SameParameterValue")
	private synchronized void setOccurrences(long occurrences)
	{
		this.occurrences=occurrences;
	}

	private synchronized void setMessage(String message)
	{
		this.message=message;
	}

	private synchronized void setLevel(Level level)
	{
		this.level=level;
	}
}
