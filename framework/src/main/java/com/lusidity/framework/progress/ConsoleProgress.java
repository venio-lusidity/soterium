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
import java.io.Closeable;
import java.io.IOException;

public class ConsoleProgress implements Closeable
{
	private final ProgressHandler handler;
	private Thread thread=null;
	private ProgressTask task=null;

	// Constructors
	public ConsoleProgress(ProgressHandler handler){
		super();
		this.handler = handler;
	}

	// Overrides
	@Override
	public void close()
		throws IOException
	{
		this.waitToStop();
	}

	public void waitToStop(){
		if (null!=this.task)
		{
			while (!this.task.isStopped())
			{
				try
				{
					this.task.stop();
					Thread.sleep(100);
				}
				catch (InterruptedException ignored)
				{
				}
			}
		}
	}

	public void start()
	{
		this.task=new ProgressTask(this.handler);
		this.thread=new Thread(this.task);
		this.thread.start();
	}
}
