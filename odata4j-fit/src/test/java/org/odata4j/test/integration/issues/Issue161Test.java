package org.odata4j.test.integration.issues;

import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.producer.jpa.JPAProvider;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.test.integration.producer.jpa.airline.AirlineJPAProducerBaseTest;

public class Issue161Test extends AirlineJPAProducerBaseTest {

  public Issue161Test(RuntimeFacadeType type) {
    super(type);
  }

  @Before
  public void setUp() {
    String persistenceUnitName = "AirlineService" + JPAProvider.ECLIPSELINK.caption;
    String namespace = "Airline";

    emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    JPAProducer producer = new JPAProducer(emf, namespace, 20);

    DefaultODataProducerProvider.setInstance(producer);
    server = this.rtFacade.startODataServer(endpointUri);
    this.fillDatabase();
  }

  @Test
  public void ensureColumnNotNullable() throws Exception {
    ODataConsumer consumer = this.rtFacade.createODataConsumer(endpointUri, null);
    Assert.assertTrue(!consumer.getMetadata().findEdmEntitySet("FlightSchedule").getType().findProperty("departureTime").isNullable());
    Assert.assertTrue(consumer.getMetadata().findEdmEntitySet("FlightSchedule").getType().findProperty("departureAirportCode").isNullable());
  }

}
