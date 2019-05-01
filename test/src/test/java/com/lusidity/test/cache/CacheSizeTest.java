

package com.lusidity.test.cache;

import com.lusidity.Environment;
import com.lusidity.domains.cache.TestCache;
import com.lusidity.test.BaseTest;
import org.junit.Test;

public
class CacheSizeTest extends BaseTest
{
	private static boolean ENABLED=false;
	// characters are 2 bytes each so 1MB would be 500000 characters.
	private int dataSize = 500000;
	// 1000 data items should be 1 GB but we want more to trigger the eviction in cache.
	private int max = 1500;

	// Overrides
	@Override
	public boolean isDisabled()
	{
		return false;
	}

	@Test
	public void eviction()
		throws Exception
	{
		if(CacheSizeTest.ENABLED)
		{
			for (int i=0; i<this.max; i++)
			{
				TestCache dataItem=this.createDataItem(this.dataSize);
				Environment.getInstance().getCache()
				           .put(TestCache.class, TestCache.class, String.format("#%d", i), dataItem);
			}
		}
	}

	private
	TestCache createDataItem(int size){
		StringBuffer sb = new StringBuffer(size);
		for(int i=0;i<size;i++){
			sb.append("a");
		}
		TestCache result = new TestCache();
		result.fetchTitle().setValue(sb.toString());
		return result;
	}
}
