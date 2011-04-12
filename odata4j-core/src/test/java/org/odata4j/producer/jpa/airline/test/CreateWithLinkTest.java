package org.odata4j.producer.jpa.airline.test;

import javax.persistence.Persistence;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.ODataProducerProvider;

public class CreateWithLinkTest extends AirlineJPAProducerTestBase {
	
	@BeforeClass
	public static void setUp() throws Exception {
		String persistenceUnitName = "AirlineService";
		String namespace = "Airline";

		emf = Persistence.createEntityManagerFactory(persistenceUnitName);

		JPAProducer producer = new JPAProducer(emf, namespace, 20);

		ODataProducerProvider.setInstance(producer);
		server = ProducerUtil.startODataServer(endpointUri);
	}

	@Test
	/**
	 * Linking to entities with string key
	 */
	public void linkToStringId() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		OEntity muc = consumer.getEntities("Airport")
			.filter("code eq 'MUC'")
			.execute()
			.firstOrNull();
		
		OEntity sfo = consumer.getEntities("Airport")
			.filter("code eq 'SFO'")
			.execute()
			.firstOrNull();

		OEntity flightSchedule = consumer.createEntity("FlightSchedule")
			.properties(OProperties.string("flightNo", "LH458"))
			.properties(OProperties.time("departureTime", new LocalTime(16, 15, 0)))
			.properties(OProperties.time("arrivalTime", new LocalTime(19, 10, 0)))
			.properties(OProperties.datetime("firstDeparture", new LocalDateTime(2011, 03, 27, 16, 15)))
			.properties(OProperties.datetime("lastDeparture", new LocalDateTime(2011, 10, 29, 0, 0)))
			.link("departureAirport", muc)
			.link("arrivalAirport", sfo)
			.execute();
		
		Assert.assertEquals("MUC", flightSchedule.getProperty("departureAirportCode").getValue());
		Assert.assertEquals("SFO", flightSchedule.getProperty("arrivalAirportCode").getValue());
		
		muc = consumer.getEntity("FlightSchedule", flightSchedule.getEntityKey())
			.nav("departureAirport")
			.execute();
		Assert.assertEquals("Franz Josef Strauß", muc.getProperty("name").getValue());

		sfo = consumer.getEntity("FlightSchedule", flightSchedule.getProperty("flightScheduleID").getValue())
			.nav("arrivalAirport")
			.execute();
		Assert.assertEquals("San Francisco International", sfo.getProperty("name").getValue());	
		
	}
	
	@Test
	/**
	 * Linking to entities with long key
	 */
	public void linkToLongId() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		OEntity flightSchedule = consumer.getEntities("FlightSchedule")
			.filter("flightNo eq 'LH410'")
			.execute()
			.firstOrNull();

		OEntity flight = consumer.createEntity("Flight")
			.properties(OProperties.datetime("takeoffTime", new LocalDateTime(2011, 03, 28, 11, 38, 15)))
			.link("flightSchedule", flightSchedule)
			.execute();
		
		flightSchedule = consumer.getEntity("Flight", flight.getProperty("flightID").getValue())
			.nav("flightSchedule")
			.execute();
		Assert.assertEquals("LH410", flightSchedule.getProperty("flightNo").getValue());		
	}
}
