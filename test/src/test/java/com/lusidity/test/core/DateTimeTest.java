

package com.lusidity.test.core;

import com.lusidity.Environment;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.people.Person;
import com.lusidity.test.BaseTest;
import com.lusidity.test.RandomNames;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class DateTimeTest extends BaseTest{

	// Overrides
	@Override
	public boolean isDisabled()
	{
		return false;
	}

    @Test
    public void onOrBefore()
        throws Exception
    {
        DateTime start = DateTime.now();

        for(int i=0;i<10;i++){
            Person person = new Person();
            person.fetchDob().setValue(start.minusMonths(i));
            person.fetchFirstName().setValue(RandomNames.at(i));
            person.save();
            Assert.assertNotNull("The id of the entity should not be empty.", person.fetchId().getValue());
        }

	    BaseQueryBuilder qb = Environment.getInstance().getIndexStore().getQueryBuilder(Person.class, Person.class, 0, 0);
	    qb.filter(BaseQueryBuilder.Operators.must, "deprecated", BaseQueryBuilder.StringTypes.na, false);
	    DateTime value = start.minusMonths(2);
	    qb.filter(BaseQueryBuilder.Operators.lte, "dob", value);
	    QueryResults qrs = qb.execute();
	    Assert.assertEquals(String.format("%s expected %d got %d", "DateTime On or Before", 8, qrs.size()), 8, qrs.size());
    }
}
