package com.lusidity.test.cache;

import com.lusidity.Environment;
import com.lusidity.cache.ICache;
import com.lusidity.data.ClassHelper;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.test.BaseTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheTest extends BaseTest {

    @BeforeClass
    public static
    void beforeClass() throws Exception
    {
        BaseTest.setTestConfig(BaseTest.getTestConfig());
        BaseTest.setClearStores(true);
        BaseTest.setInitialize(false);
        BaseTest.beforeClass();
    }

    @Test
    public void vertex()
        throws Exception
    {
        AtSchemaClass sca = ClassHelper.getSchema(Person.class);
        if(!Environment.getInstance().getCache().isDisabled())
        {
	        if (sca.writable())
	        {
				// Ensuer caching is enabled for com.lusidity.domains.people.Person
		        ICache cache=Environment.getInstance().getCache();
		        Person expected=new Person();
		        expected.fetchFirstName().setValue("Will");
		        expected.fetchLastName().setValue("Moore");
		        expected.fetchTitle().setValue("Mr.");
		        expected.save();

		        Assert.assertTrue("The person id should not be null.", expected.hasId());

		        String id=expected.fetchId().getValue();

		        //todo create message if class is 'not cacheable
		        Person actual=cache.get(Person.class, Person.class, id);

		        Assert.assertNotNull("The actual should not be null.", actual);
		        Assert.assertEquals("Wrong ID.", expected.fetchId().getValue(), actual.fetchId().getValue());
		        Assert.assertEquals("Wrong first name after get.", "Will", actual.fetchFirstName().getValue());
		        Assert.assertEquals("Wrong last name after get.", "Moore", actual.fetchLastName().getValue());

		        actual.fetchLastName().setValue("Paris");
		        actual.fetchFirstName().setValue("Thomas");
		        actual.save();

		        Person updated=cache.get(Person.class, Person.class, id);

		        Assert.assertEquals("Wrong first name after update.", "Thomas", updated.fetchFirstName().getValue());
		        Assert.assertEquals("Wrong last name after update.", "Paris", updated.fetchLastName().getValue());

		        expected.delete();

		        actual=cache.get(Person.class, Person.class, id);

		        Assert.assertNull("The actual should be null.", actual);
	        }
	        else
	        {
		        System.out.println("\n\n************ NOTE ***********"+
		                           "\nIn order to test caching com.lusidity.framework.annotations.SchemaClassAnnotation.cacheEnabled must be set to true."+
		                           "\nIf you do this ensure you set it back after testing.\n\n");
	        }
        }
	    else{
	        System.out.println("\n\n************ CACHING NOT ENABLED !!! ***********"+
	                           "\nIn order to test caching com.lusidity.framework.annotations.SchemaClassAnnotation.cacheEnabled must be set to true."+
	                           "\nIf you do this ensure you set it back after testing.\n\n");
        }
    }

    @Override
    public
    boolean isDisabled()
    {
        return false;
    }
}
