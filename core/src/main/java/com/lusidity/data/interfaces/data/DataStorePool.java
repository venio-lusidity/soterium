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

package com.lusidity.data.interfaces.data;

import com.lusidity.Environment;
import com.lusidity.blockers.VertexBlocker;
import com.lusidity.data.interfaces.operations.IAction;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataStorePool implements IDataThreadPool
{
	private static final long SHUTDOWN_WAIT_INTERVAL=((60*1000)*10);
	
	private ExecutorService pool=null;
	private VertexBlocker blocker = null;
	private boolean started = false;

	protected DataStorePool(){
		super();
	}

	// Overrides
	@Override
	public void close()
		throws IOException
	{
		this.stop();
	}

	// Methods
	public static DataStorePool begin(int threadPoolSize)
	{
		DataStorePool result=new DataStorePool();
		result.start(threadPoolSize);
		return result;
	}

	@Override
	public void start(int threadPoolSize){
		this.pool =Executors.newFixedThreadPool(threadPoolSize);
		this.blocker = new VertexBlocker();
		this.started = true;
	}

	@Override
	public void stop(){
		if (null!=this.pool)
		{
			//  Stop accept new threads
			this.pool.shutdown();
			try {
				if (!this.pool.awaitTermination(DataStorePool.SHUTDOWN_WAIT_INTERVAL, TimeUnit.MILLISECONDS)) {
					this.pool.shutdownNow();
				}
			} catch (InterruptedException e) {
				this.pool.shutdownNow();
			}
		}
	}

	@Override
	public void submit(IAction action){
		boolean block =(null!=action.getOperation().getVertex()) &&
		               ((action.getOperation().getType()==IOperation.Type.update)
		               || (action.getOperation().getType()==IOperation.Type.delete));
		try
		{
			if(block)
			{
				this.blocker.start(action.getOperation().getVertex());
			}
			if((null!=action.getOperation().getBulkItems()) && !action.getOperation().getBulkItems().isEmpty()){
				action.getOperation().setSuccessful(action.execute());
			}
			else{
				action.getOperation().setSuccessful(action.execute(action.getOperation().getBulkItems()));
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().warning(ex);
		}
		finally
		{
			if(block)
			{
				this.blocker.finished(action.getOperation().getVertex());
			}
		}
	}

	@Override
	public boolean isStarted()
	{
		return this.started;
	}
}
