package com.lusidity.test.providers;

import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.providers.wikipedia.WikipediaProvider;
import com.lusidity.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class WikiProviderTest extends BaseTest {
    @Test
    public void parse(){
    	if(!this.isDisabled())
	    {
		    WikipediaProvider provider=new WikipediaProvider();
		    Collection<DiscoveryItem> results=provider.discover("United States", 0, 0);
		    Assert.assertTrue("The results should not be empty.", !results.isEmpty());
	    }
    }

	@Override
	public
	boolean isDisabled()
	{
		return true;
	}
}
