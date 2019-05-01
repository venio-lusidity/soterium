package com.lusidity.test.domains;

import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.PhoneNumber;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.text.StringX;
import com.lusidity.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class PersonTest extends BaseTest {
    @Test
    public void create() throws Exception {
        Person person = new Person();
        person.fetchFirstName().setValue("Will");
        person.fetchLastName().setValue("Moore");
        person.fetchTitle().setValue("Mr.");

        UriValue uriValue = new UriValue("person://tom/paris");
        person.fetchIdentifiers().add(uriValue);

        person.save();
        PhoneNumber pn = new PhoneNumber(BaseContactDetail.CategoryTypes.home_phone, "9104969164", null);
        person.getContactDetails().add(pn);

        Assert.assertTrue("The person should have at least one contact.", !person.getContactDetails().isEmpty());

        String id = person.fetchId().getValue();
        person = VertexFactory.getInstance().get(Person.class, id);

        Assert.assertTrue("The id's do not match.", (StringX.equals(person.fetchId().getValue(), id)));
    }

	@Override
	public
	boolean isDisabled()
	{
		return false;
	}
}
