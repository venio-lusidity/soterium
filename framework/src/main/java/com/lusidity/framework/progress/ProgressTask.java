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

package com.lusidity.framework.progress;

import com.lusidity.framework.time.Stopwatch;

public class ProgressTask implements Runnable
{
	// Fields
	public static final int PAUSE_MILLIS=500;
	private final ProgressHandler handler;
	private boolean initialized=false;
	private Stopwatch timer=null;
	private boolean stopping=false;
	private boolean stopped=false;

	// Constructors
	public ProgressTask(ProgressHandler handler)
	{
		super();
		this.handler=handler;
	}

	// Overrides
	@Override
	public void run()
	{
		// reset properties.
		this.pause(ProgressTask.PAUSE_MILLIS);
		this.stopped=false;
		this.stopping=false;
		this.initialized=false;
		this.timer=new Stopwatch();
		this.timer.start();

		while (!this.isStopping())
		{
			this.pause(ProgressTask.PAUSE_MILLIS);
			this.report();
		}

		this.report();
		this.timer.stop();
		this.pause(ProgressTask.PAUSE_MILLIS);
		this.setStopped(true);
	}

	private void pause(int millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException ignored)
		{
		}
	}

	public synchronized boolean isStopping()
	{
		return this.stopping;
	}

	private void report()
	{
		try
		{
			if (!this.initialized)
			{
				System.out.println();
			}
			this.initialized=true;
			long current=this.handler.getCurrentProgressCount();
			long whole=this.handler.getWholeProcessCount();
			@SuppressWarnings("MagicNumber")
			float pcComplete=0;
			if ((current>0) && (whole>0))
			{
				pcComplete=(current/(float) whole)*100.0f;
			}
			String say=String.format(
				"\r%s %d/%d (%.2f%%) elapsed: %s started: %s",
				this.handler.getProgressStatusText(),
				current, whole, pcComplete,
				this.timer.elapsed().toString(), this.timer.getStartedWhen().toString("dd-MMM-yyyy HH:mm:ss")
			);
			System.out.print(say);
		}
		catch (Exception ignored){}
	}

	public synchronized void stop()
	{
		this.stopping=true;
	}

	// Getters and setters
	public synchronized boolean isStopped()
	{
		return this.stopped;
	}

	private synchronized void setStopped(boolean stopped)
	{
		this.stopped=stopped;
	}
}
