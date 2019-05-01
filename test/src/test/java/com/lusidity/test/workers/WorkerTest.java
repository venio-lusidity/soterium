package com.lusidity.test.workers;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.system.assistant.worker.BaseAssistantWorker;
import com.lusidity.framework.text.StringX;
import com.lusidity.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class WorkerTest extends BaseTest {

    @Test
    public void simple()
    {
	    if (!this.isDisabled())
	    {
		    Collection<Class<? extends BaseAssistantWorker>> clsWorkers=Environment.getInstance().getReflections().getSubTypesOf(BaseAssistantWorker.class);
		    if (null!=clsWorkers)
		    {
			    for (Class<? extends BaseAssistantWorker> clsWorker : clsWorkers)
			    {
				    String key=ClassHelper.getClassKey(clsWorker);
				    Assert.assertTrue("The worker does not have a SchemaClassAnnotation id.", !StringX.isBlank(key));

				    QueryResults queryResults=Environment.getInstance().getQueryFactory().matchAll(clsWorker, clsWorker, null, 0, 10);
				    Assert.assertTrue(String.format("The results should only contain 1 worker found %d", queryResults.size()), queryResults.size()==1);
			    }
		    }
	    }
	    else{
		    System.out.print("Worker Test 'Simple' has been Disabled");
	    }
    }

	@Override
	public
	boolean isDisabled()
	{
		return true;
	}
}
