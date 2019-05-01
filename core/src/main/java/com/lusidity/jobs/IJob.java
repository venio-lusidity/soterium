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

package com.lusidity.jobs;

import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.framework.json.JsonData;
import org.joda.time.DateTime;

import java.io.Closeable;

/**
 * An IJob requires no external information in order to run other than what maybe put into a configuration file.
 */
public interface IJob extends Closeable
{
	enum Status
	{
		processed,
		processing,
		waiting,
		failed,
		idle
	}

	/**
	 * Start auditor.
	 *
	 * @param args A collection of object instances.
	 */
	boolean start(Object... args);

	JsonData toJson();

	boolean recordInLog();

	boolean recordHistory();

	boolean stop();

	/**
	 * Can this job push out a report only before modifying the data.
	 *
	 * @return true or false;
	 */
	boolean hasReportOnly();

	// Getters and setters
	boolean isExecutable();

	boolean isStopping();

	void setStopping(boolean stopping);

	boolean isRunning();

	boolean isEnabled();

	/**
	 * The status of the job.
	 *
	 * @return A Status
	 */
	IJob.Status getStatus();

	void setStatus(IJob.Status status);

	/**
	 * User friendly name of this job.
	 *
	 * @return A title.
	 */
	String getTitle();

	/**
	 * Written text that represents what this Job does.
	 *
	 * @return The description.
	 */
	String getDescription();

	ProcessStatus getProcessStatus();

	DateTime getLastRun();

	void setLastRun(DateTime lastRun);

	/**
	 * The order in which to display the data.
	 * @return The ordinal.
	 */
	int getOrdinal();
}
