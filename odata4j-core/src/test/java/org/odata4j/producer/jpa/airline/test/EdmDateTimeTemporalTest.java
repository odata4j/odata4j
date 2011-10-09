package org.odata4j.producer.jpa.airline.test;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Persistence;
import javax.persistence.metamodel.SingularAttribute;

import org.core4j.Enumerable;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.jpa.JPAEdmGenerator;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.test.OData4jTestSuite;

/**
 * Unfortunately not all OData consumer support the Edm.Time type yet(?).
 * Using a customized JPAEdmGenerator as shown in this test case allows
 * using a Edm.DateTime instead of Edm.Time for types annotated with
 * TemporalType.TIME (Date, Calendar) or java.sql.Time.
 */
public class EdmDateTimeTemporalTest extends AirlineJPAProducerTestBase {

  @BeforeClass
  public static void setUp() throws Exception {
    String persistenceUnitName = "AirlineService" + OData4jTestSuite.JPA_PROVIDER.caption;
    String namespace = "Airline";

    emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    JPAProducer producer = new JPAProducer(emf, new JPAEdmGenerator() {
      protected EdmSimpleType<?> toEdmType(SingularAttribute<?, ?> sa) {
        Class<?> javaType = sa.getType().getJavaType();
        if (javaType.equals(Date.class)
            || javaType.equals(Calendar.class)
            || javaType.equals(Time.class)) {
          return EdmSimpleType.DATETIME;
        } else {
          return super.toEdmType(sa);
        }
      }
    }.buildEdm(emf, namespace), 20);

    ODataProducerProvider.setInstance(producer);
    server = ProducerUtil.startODataServer(endpointUri);
  }

  @Test
  public void testMetadata() {
    ODataConsumer consumer = ODataConsumer.create(endpointUri);

    EdmDataServices metadata = consumer.getMetadata();

    Assert.assertEquals(EdmSimpleType.DATETIME, metadata.findEdmEntitySet("FlightSchedule").getType().findProperty("departureTime").getType());
    Assert.assertEquals(EdmSimpleType.DATETIME, metadata.findEdmEntitySet("FlightSchedule").getType().findProperty("arrivalTime").getType());
  }

  @Test
  /**
   *handling of Date fields with different @Temporal
   */
  public void createWithDifferentTemporal() throws Exception {
    ODataConsumer consumer = ODataConsumer.create(endpointUri);

    OEntity flightSchedule = consumer.createEntity("FlightSchedule")
        .properties(OProperties.string("flightNo", "LH460"))
        .properties(OProperties.time("departureTime", new LocalTime(9, 30, 0)))
        .properties(OProperties.time("arrivalTime", DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).parse("2:10 pm")))
        .properties(OProperties.datetime("firstDeparture", new LocalDateTime(2011, 03, 28, 9, 30)))
        .properties(OProperties.datetime("lastDeparture", DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse("07/05/2011")))
        .execute();

    Long id = (Long) flightSchedule.getProperty("flightScheduleID").getValue();
    Assert.assertEquals(new LocalDateTime(1970, 1, 1, 9, 30, 0), flightSchedule.getProperty("departureTime").getValue());
    Assert.assertEquals(new LocalDateTime(1970, 1, 1, 14, 10, 0), flightSchedule.getProperty("arrivalTime").getValue());
    Assert.assertEquals(new LocalDateTime(2011, 03, 28, 9, 30), flightSchedule.getProperty("firstDeparture").getValue());
    Assert.assertEquals(new LocalDateTime(2011, 07, 05, 0, 0), flightSchedule.getProperty("lastDeparture").getValue());

    flightSchedule = consumer.getEntity("FlightSchedule", id).execute();
    Assert.assertEquals(new LocalDateTime(1970, 1, 1, 9, 30, 0), flightSchedule.getProperty("departureTime").getValue());
    Assert.assertEquals(new LocalDateTime(1970, 1, 1, 14, 10, 0), flightSchedule.getProperty("arrivalTime").getValue());
    Assert.assertEquals(new LocalDateTime(2011, 03, 28, 9, 30), flightSchedule.getProperty("firstDeparture").getValue());
    Assert.assertEquals(new LocalDateTime(2011, 07, 05, 0, 0), flightSchedule.getProperty("lastDeparture").getValue());
  }

  @Test
  public void filterTime() {
    ODataConsumer consumer = ODataConsumer.create(endpointUri);

    Enumerable<OEntity> schedules = consumer.getEntities("FlightSchedule")
        .filter("departureTime ge datetime'1970-01-01T11:00:00' and departureTime lt datetime'1970-01-01T12:00:00'")
        .execute();

    Assert.assertEquals(1, schedules.count());

  }
}
