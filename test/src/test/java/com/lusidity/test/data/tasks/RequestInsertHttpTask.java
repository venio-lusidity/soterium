

package com.lusidity.test.data.tasks;


import com.lusidity.data.DataVertex;

import java.util.concurrent.Callable;

public class RequestInsertHttpTask implements Callable<Boolean>
{
	private final DataVertex vertex;

	// Constructors
	public RequestInsertHttpTask(DataVertex vertex)
	{
		this.vertex = vertex;
	}

	// Overrides
	@Override
	public Boolean call()
		throws Exception
	{
		return this.vertex.save();
	}
}
