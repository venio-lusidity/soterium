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

import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class TimerX
{
	private volatile ScheduledExecutorService ses;
	private volatile ScheduledFuture<?> sf;
	private Timer timer = null;

	private boolean stopped = false;

	/**
	 * This is thread blocking!!
	 * @param interval the period of time to report, in seconds.
	 * @param duration max time to countdown to, in seconds.
	 * @param output output elapsed seconds at interval.
	 */
	public synchronized void countdown(long interval, long duration, boolean output)
	{
		this.timer = new Timer();
		CountdownTask task=new CountdownTask(this, interval, duration, output);
		this.timer.scheduleAtFixedRate(task, 0, interval*1000);
	}

	public synchronized void shutdown()
	{
		this.timer.cancel();
		this.stopped = true;
	}

	public boolean isStopped(){
		return this.stopped;
	}
}
