package com.lusidity.test.data;

import com.lusidity.Environment;
import com.lusidity.domains.event.Event;
import com.lusidity.domains.event.EventFact;
import com.lusidity.domains.event.Itinerary;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.json.JsonData;
import com.lusidity.test.BaseTest;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class EventTest extends BaseTest
{
	@Override
	public boolean isDisabled()
	{
		return false;
	}

	@Test
	public void event()
		throws Exception
	{
		Event event=new Event();
		event.fetchTitle().setValue("Event Test");

		Itinerary itinerary=new Itinerary();
		itinerary.fetchTitle().setValue("Conference Day 1");
		event.fetchItineraries().add(itinerary);

		EventFact fact=new EventFact();
		fact.fetchKey().setValue("start");
		fact.fetchFact().setValue(DateTime.now());
		fact.fetchLabel().setValue("Start");
		itinerary.fetchFacts().add(fact);

		fact=new EventFact();
		fact.fetchKey().setValue("subject");
		fact.fetchFact().setValue("Concurrency Programming");
		fact.fetchLabel().setValue("Subject");
		itinerary.fetchFacts().add(fact);

		fact=new EventFact();
		fact.fetchKey().setValue("end");
		fact.fetchFact().setValue(DateTime.now().plusHours(1));
		fact.fetchLabel().setValue("end");
		itinerary.fetchFacts().add(fact);

		fact=new EventFact();
		fact.fetchKey().setValue("today");
		fact.fetchFact().setValue(true);
		fact.fetchLabel().setValue("isToday");
		itinerary.fetchFacts().add(fact);

		fact=new EventFact();
		fact.fetchKey().setValue("hours");
		fact.fetchFact().setValue(1);
		fact.fetchLabel().setValue("How Long");
		itinerary.fetchFacts().add(fact);

		fact=new EventFact();
		fact.fetchKey().setValue("passing");
		fact.fetchFact().setValue(.76);
		fact.fetchLabel().setValue("% required to pass");
		itinerary.fetchFacts().add(fact);

		boolean saved = event.save();
		Assert.assertTrue("The event did not save.", saved);

		Event expected=VertexFactory.getInstance().getById(event.getUri().toString());
		Assert.assertNotNull("The expected event is null.", expected);

		Itinerary eItinerary=expected.fetchItineraries().get();
		Assert.assertNotNull("The itinerary is null.", eItinerary);

		for (EventFact fct : eItinerary.fetchFacts())
		{
			Assert.assertNotNull("A fact is null", fct.fetchFact().getValue());
			System.out.println(String.format("%s: %s", fct.fetchLabel().getValue(), fct.fetchFact().getValue()));
		}

		// TODO: validate mapping???
		Object object=Environment.getInstance().getDataStore().getSchema(Event.class);
		if (object instanceof JsonData)
		{
			JsonData data=JsonData.create(object);
			JsonData dEvent=data.getFromPath("event_event", "mappings", "event_event");
			Assert.assertNotNull("Event data is null.", dEvent);

			JsonData dItineratires=dEvent.getFromPath("properties", "/event/itinerary/itineraries");
			Assert.assertNotNull("Event data is null.", dItineratires);

			JsonData dFacts=dItineratires.getFromPath("properties", "/event/event_fact/facts");
			Assert.assertNotNull("Event data is null.", dFacts);

			JsonData dFact=dFacts.getFromPath("properties", "fact");
			Assert.assertNotNull("Event data is null.", dFact);

			JsonData dKey=dFacts.getFromPath("properties", "key");
			Assert.assertNotNull("Event data is null.", dKey);

			JsonData fLabel=dFacts.getFromPath("properties", "label");
			Assert.assertNotNull("Event data is null.", fLabel);

			JsonData clsType=dFacts.getFromPath("properties", "clsType");
			Assert.assertNotNull("Event data is null.", clsType);
		}
		else
		{
			Assert.fail("Mapping not found.");
		}
	}
}
