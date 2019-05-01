package com.lusidity.test.core;

import com.lusidity.Environment;
import com.lusidity.domains.people.Person;
import com.lusidity.test.BaseTest;
import com.lusidity.test.RandomNames;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class DateTest extends BaseTest{

    //NOTE: These tests must be ran with an empty database each time.

    //TODO: There should only be one instance of a term.
    //TODO: There should only be one instance of an element type.

    private Collection<Person> people = new ArrayList<>();


    @Test
    public void dates()
        throws Exception
    {

        DateTime start = DateTime.now().minusYears(20);

        for(int i=0;i<50;i++){
            Person person = new Person();
            person.fetchDob().setValue(start.minusMonths(i));
            person.fetchFirstName().setValue(RandomNames.at(i));
            person.save();

            Assert.assertNotNull("The id of the entity should not be empty.", person.fetchId().getValue());

            this.people.add(person);
        }

        for(Person entity: this.people) {
            Person person = Environment.getInstance().getDataStore().getObjectById(Person.class, Person.class, entity.fetchId().getValue());
            Assert.assertTrue("The person object should not be null", null!=person);
            Assert.assertTrue("The date attribute should not be null", null!=person.fetchDob().getValue());
        }
    }

    @Test
    public void bornSameDay()
        throws Exception
    {
        DateTime start = DateTime.now();

        for (int i = 0; i < 20; i++) {
            Person person = new Person();
            person.fetchDob().setValue(start.minusYears(i));
            person.fetchFirstName().setValue(RandomNames.at(i));
            person.save();

            Assert.assertNotNull("The id of the entity should not be empty.", person.fetchId().getValue());

            this.people.add(person);
        }

        for (Person entity : this.people) {

            Person person = Environment.getInstance().getDataStore().getObjectById(Person.class, Person.class, entity.fetchId().getValue());
            Assert.assertTrue("The person object should not be null", null!=person);
            Assert.assertTrue("The date attribute should not be null", null!=person.fetchDob().getValue());

            // TODO: born same day
        }
    }

    @Test
    public void bornSameMonthYear()
        throws Exception
    {
        DateTime start = DateTime.now();

        for (int i = 0; i < 20; i++) {
            Person person = new Person();
            person.fetchDob().setValue(start.minusYears(i));
            person.fetchFirstName().setValue(RandomNames.at(i));
            person.save();

            Assert.assertNotNull("The id of the entity should not be empty.", person.fetchId().getValue());

            this.people.add(person);
        }

        for (Person entity : this.people) {

            Person person = Environment.getInstance().getDataStore().getObjectById(Person.class, Person.class, entity.fetchId().getValue());
            Assert.assertTrue("The person object should not be null", null!=person);
            Assert.assertTrue("The date attribute should not be null", null!=person.fetchDob().getValue());

            // TODO: born same month and year
        }
    }

    @Test
    public void bornSameYear()
        throws Exception
    {
        DateTime start = DateTime.now();

        for (int i = 0; i < 20; i++) {
            Person person = new Person();
            person.fetchDob().setValue(start.minusYears(i));
            person.fetchFirstName().setValue(RandomNames.at(i));
            person.save();

            Assert.assertNotNull("The id of the entity should not be empty.", person.fetchId().getValue());

            this.people.add(person);
        }

        for (Person entity : this.people) {

            Person person = Environment.getInstance().getDataStore().getObjectById(Person.class, Person.class, entity.fetchId().getValue());
            Assert.assertTrue("The person object should not be null", null!=person);
            Assert.assertTrue("The date attribute should not be null", null!=person.fetchDob().getValue());

            // TODO: born same year
        }
    }

    @Override
    public
    boolean isDisabled()
    {
        return false;
    }
}
