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

import java.util.TimerTask;

public class CountdownTask extends TimerTask
{
	private final TimerX timerX;
	private final long interval;
	private final long duration;
	private final boolean output;
	private long elapsed = 0;

	public CountdownTask(TimerX timerX, long interval, long duration, boolean output)
	{
		super();
		this.timerX = timerX;
		this.interval = interval;
		this.duration = duration;
		this.output = output;
	}

	@Override
	public void run()
	{
		if(this.output){
			if(this.elapsed==0){
				System.out.println(String.format("Elapsed: %d", this.elapsed));
			}
			else{
				System.out.print(String.format("\rElapsed: %d", this.elapsed));
			}
		}
		this.elapsed+=this.interval;
		if(this.elapsed>=this.duration){
			this.timerX.shutdown();
		}
	}
}
