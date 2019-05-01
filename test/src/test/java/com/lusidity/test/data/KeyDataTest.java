

package com.lusidity.test.data;

import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.system.assistant.worker.JobHistory;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.jobs.IJob;
import com.lusidity.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class KeyDataTest extends BaseTest
{
	// Overrides
	@Override
	public boolean isDisabled()
	{
		return false;
	}

	@Test
	public void json()
		throws Exception
	{
		Person tom = new Person();
		tom.fetchFirstName().setValue("thomas");
		tom.fetchMiddleName().setValue("carl");
		tom.fetchLastName().setValue("paris");
		tom.fetchPrefix().setValue("mr.");
		tom.save();

		Assert.assertTrue("firstName should not be null.", tom.fetchFirstName().isNotNullOrEmpty());
		Assert.assertTrue("middleName should not be null.", tom.fetchMiddleName().isNotNullOrEmpty());
		Assert.assertTrue("lastName should not be null.", tom.fetchLastName().isNotNullOrEmpty());
		Assert.assertTrue("prefix should not be null.", tom.fetchPrefix().isNotNullOrEmpty());
		Assert.assertTrue("dob should be null.", tom.fetchDob().isNullOrEmpty());
		Assert.assertTrue("deprecated should be null.", tom.fetchDeprecated().isNotNullOrEmpty());
		Assert.assertFalse("the value deprecated should be false.", tom.fetchDeprecated().isTrue());

		tom.fetchDeprecated().setValue(true);
		Assert.assertTrue("the value deprecated should be true.", tom.fetchDeprecated().isTrue());

		tom.fetchDeprecated().setValue(false);
		Assert.assertFalse("the value deprecated should be false.", tom.fetchDeprecated().isTrue());

		// get the JSON data.
		JsonData data = tom.toJson(false);

		// ensure keys are valid.
		Assert.assertTrue("should have key firstName.", data.hasKey("firstName"));
		Assert.assertTrue("should have key middleName.", data.hasKey("middleName"));
		Assert.assertTrue("should have key lastName.", data.hasKey("lastName"));
		Assert.assertTrue("should have key prefix.", data.hasKey("prefix"));

		Assert.assertEquals("firstName should match.", tom.fetchFirstName().getValue(), data.getString("firstName"));
		Assert.assertEquals("middleName should match.", tom.fetchMiddleName().getValue(), data.getString("middleName"));
		Assert.assertEquals("lastName should match.", tom.fetchLastName().getValue(), data.getString("lastName"));
		Assert.assertEquals("prefix should match.", tom.fetchPrefix().getValue(), data.getString("prefix"));

		UriValue uri1 = new UriValue("mailto:thomas.paris1@gmail.com");
		UriValue uri2 = new UriValue("mailto:thomas.paris2@gmail.com");
		UriValue uri3 = new UriValue("mailto:thomas.paris3@gmail.com");

		tom.fetchIdentifiers().add(uri1);
		tom.fetchIdentifiers().add(uri2);
		tom.fetchIdentifiers().add(uri3);
		tom.fetchIdentifiers().add(uri1);

		Assert.assertTrue("The collection should not be empty", !tom.fetchIdentifiers().isEmpty());
		Assert.assertEquals("The collection should have three identifiers", tom.fetchIdentifiers().size(), 3);

		tom.fetchIdentifiers().remove(1);

		for(UriValue uri: tom.fetchIdentifiers()){
			if(uri.equals(uri2)){
				Assert.fail("uri1 or uri3 was expected but not found.");
			}
			else if(uri.equals(uri1)){
				uri.fetchLabel().setValue("I changed");
			}
			else if(uri.equals(uri3)){
				uri.fetchLabel().setValue("I changed");
			}
		}

		for(UriValue uri: tom.fetchIdentifiers()){
			if(!uri.fetchLabel().getValue().equals("I changed")){
				Assert.fail("The label did not change.");
			}
		}

		data = tom.toJson(true);

		tom.fetchIdentifiers().clear();

		Assert.assertTrue("The collection should be empty", tom.fetchIdentifiers().isEmpty());

		// create a new person from the data.
		// check KeyData values
		// get and view final JSON in viewer.

	}

	@Test
	public void keyDataPrimitive()
		throws Exception
	{
		String title = "KeyData Primitive";
		Stopwatch sw = Stopwatch.begin();
		Thread.sleep(10000);
		sw.stop();
		JobHistory expected = JobHistory.create(title, this.getClass(), sw, new ProcessStatus(), IJob.Status.processed);
		expected.save();
		JsonData jd = expected.toJson(false);
		Assert.assertTrue("ProcessStatus node missing.", jd.hasKey("/data/process_status/processStatus"));

		JobHistory actual =VertexFactory.getInstance().getByTitle(JobHistory.class, title);
		Assert.assertNotNull("The JobHistory actual should not be null", actual);
	}
}
