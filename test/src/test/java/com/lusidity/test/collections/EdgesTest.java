package com.lusidity.test.collections;

import com.lusidity.Environment;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.test.TestObject;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class EdgesTest extends BaseTest
{
	@Override
	public boolean isDisabled()
	{
		return false;
	}

	@Test
	public void persistent()
		throws Exception
	{
		TestObject parent = this.create(0);
		parent.save();

		Assert.assertTrue("The id of the TestObject should not be null.", parent.hasId());

		String id = parent.fetchId().getValue();

		int len = 10;

		for(int i=0;i<len;i++){
			TestObject child = this.create((i+1));
			child.save();
			parent.getTestObjects().add(child);
		}

		Assert.assertEquals("The getTestObjects size should be 10.", 10, parent.getTestObjects().size());

		parent.getTestObjects().reload();

		Assert.assertEquals("The getTestObjects size should be 10.", 10, parent.getTestObjects().size());

		parent.getTestObjects().remove(2);

		parent = VertexFactory.getInstance().get(TestObject.class, id);

		Assert.assertNotNull("The parent should not be null.", parent);

		Assert.assertEquals("The getTestObjects size should be 9.", 9, parent.getTestObjects().size());

		parent.getTestObjects().clearAndDelete();

		Assert.assertTrue("The getTestObjects should be empty.", parent.getTestObjects().isEmpty());

		parent = VertexFactory.getInstance().get(TestObject.class, id);

		Assert.assertTrue("The getTestObjects should be empty.", parent.getTestObjects().isEmpty());
	}

	private TestObject create(int idx){
		TestObject result = new TestObject();
		result.fetchTitle().setValue(String.format("Test Object %d", idx));
		return result;
	}

	@Test
	public void soteriumPluginOtherEnd()
		throws Exception
	{
		TestObject testObject = new TestObject();
		testObject.fetchTitle().setValue("TO0");
		testObject.save();

		Stopwatch sw = new Stopwatch();
		sw.start();
		int max = 100;
		for(int i=0;i<max;i++){
			TestObject childObject = new TestObject();
			childObject.fetchTitle().setValue(String.format("TO%d", (i+1)));
			childObject.setImmediate(false);
			childObject.save();
			testObject.getElements().add(childObject);
		}
		sw.stop();

		System.out.println(String.format("It took %s to create %d vertices.", sw.elapsedToMillisString(), max));

		Environment.getInstance().getIndexStore().makeAvailable(TestObject.class, true);
		Environment.getInstance().getIndexStore().makeAvailable(Edge.class, true);

		this.iterate();
	}

	private void iterate()
	{
		this.testCommon();
		this.testPlugin();
	}

	private void testCommon()
	{
		TestObject testObject = VertexFactory.getInstance().getByTitle(TestObject.class, "TO0");
		Stopwatch sw = new Stopwatch();
		sw.start();
		int on = 0;
		for(TestObject co: testObject.getElements()){
			on++;
		}
		sw.stop();
		System.out.println(String.format("It took %s to iterate %d vertices using default iterator.", sw.elapsedToMillisString(), on));
	}

	private void testPlugin()
	{
		TestObject testObject = VertexFactory.getInstance().getByTitle(TestObject.class, "TO0");
		Stopwatch sw = new Stopwatch();
		sw.start();
		int on = 0;
		for(TestObject co: testObject.getNodeElements()){
			on++;
		}
		sw.stop();
		System.out.println(String.format("It took %s to iterate %d vertices using plugin iterator.", sw.elapsedToMillisString(), on));
	}
}
