package org.odata4j.format.xml;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;

public class EdmxFormatParserTest {

    public EdmxFormatParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // a "unit" edmx
    private String edmxFile = "/META-INF/edmx.xml";
    // an SAP Data Services sample edmx
    private String sapDsSampleEdmxFile = "/META-INF/sap_ds_sample_edmx.xml";

    @Test
    public void testInheritance() throws FileNotFoundException, InterruptedException {

      // do the raw xml first...
      XMLEventReader2 reader =  InternalUtil.newXMLEventReader(new BufferedReader(
          new InputStreamReader(getClass().getResourceAsStream(edmxFile))));
      EdmDataServices d = EdmxFormatParser.parseMetadata(reader);
      assertTrue("parsed", null != d);

      checkTypeHierarchy(d);

      // now take the parsed result, back to xml, re-parse, check that...
      StringWriter sw = new StringWriter();
      EdmxFormatWriter.write(d, sw);

      EdmDataServices d2 = EdmxFormatParser.parseMetadata(InternalUtil.newXMLEventReader(new StringReader(sw.toString())));
      assertTrue("parsed", null != d2);

      checkTypeHierarchy(d2);
    }

    @Test
    public void parseSapDsSample() {
      XMLEventReader2 reader =  InternalUtil.newXMLEventReader(new BufferedReader(
          new InputStreamReader(getClass().getResourceAsStream(sapDsSampleEdmxFile))));
      EdmDataServices d = EdmxFormatParser.parseMetadata(reader);
      assertTrue("parsed", null != d);
    }

    private void checkTypeHierarchy(EdmDataServices d) {
      EdmEntityType airport = d.findEdmEntitySet("Airport").getType();
      EdmEntityType badAirport = d.findEdmEntitySet("BadAirport").getType();
      assertTrue(badAirport.getBaseType().equals(airport));
      assertTrue(badAirport.getKeys().equals(airport.getKeys()));
      assertTrue(badAirport.getDeclaredNavigationProperties().count() == 0);
      assertTrue(badAirport.getNavigationProperties().count() == airport.getDeclaredNavigationProperties().count() +
          badAirport.getDeclaredNavigationProperties().count());
      assertTrue(badAirport.getDeclaredProperties().count() == 2);
      assertTrue(badAirport.findDeclaredProperty("rating") != null);
      assertTrue(badAirport.findDeclaredProperty("prop2") != null);
      assertTrue(badAirport.getProperties().count() == airport.getDeclaredProperties().count() +
          badAirport.getDeclaredProperties().count());
      assertTrue(badAirport.findProperty("name") != null);
      assertTrue(badAirport.findProperty("code") != null);
      assertTrue(badAirport.findProperty("country") != null);
      assertTrue(badAirport.findProperty("rating") != null);
      assertTrue(badAirport.findProperty("prop2") != null);

      EdmEntityType schedule = d.findEdmEntitySet("FlightSchedule").getType();
      EdmEntityType subSchedule = d.findEdmEntitySet("SubFlightSchedule").getType();
      assertTrue(subSchedule.getBaseType().equals(schedule));
      assertTrue(subSchedule.getKeys().equals(schedule.getKeys()));
      assertTrue(subSchedule.getDeclaredNavigationProperties().count() == 0);
      assertTrue(subSchedule.getNavigationProperties().count() == 2);
      assertTrue(subSchedule.getNavigationProperties().count() == schedule.getDeclaredNavigationProperties().count() +
          subSchedule.getDeclaredNavigationProperties().count());

      assertTrue(subSchedule.getDeclaredProperties().count() == 3);
      assertTrue(subSchedule.findDeclaredProperty("prop3") != null);
      assertTrue(subSchedule.findDeclaredProperty("prop4") != null);
      assertTrue(subSchedule.findDeclaredProperty("prop5") != null);
      assertTrue(subSchedule.getProperties().count() == schedule.getDeclaredProperties().count() +
          subSchedule.getDeclaredProperties().count());
      assertTrue(subSchedule.findProperty("arrivalAirportCode") != null);
      assertTrue(subSchedule.findProperty("flightScheduleID") != null);
      assertTrue(subSchedule.findProperty("arrivalTime") != null);
      assertTrue(subSchedule.findProperty("flightNo") != null);
      assertTrue(subSchedule.findProperty("firstDeparture") != null);
      assertTrue(subSchedule.findProperty("departureTime") != null);
      assertTrue(subSchedule.findProperty("departureAirportCode") != null);
      assertTrue(subSchedule.findProperty("lastDeparture") != null);
      assertTrue(subSchedule.findProperty("prop3") != null);
      assertTrue(subSchedule.findProperty("prop4") != null);
      assertTrue(subSchedule.findProperty("prop5") != null);

      EdmEntityType subsubSchedule = d.findEdmEntitySet("SubSubFlightSchedule").getType();
      assertTrue(subsubSchedule.getBaseType().equals(subSchedule));
      assertTrue(subsubSchedule.getKeys().equals(subSchedule.getKeys()));
      assertTrue(subsubSchedule.getDeclaredNavigationProperties().count() == 1);
      assertTrue(subsubSchedule.getNavigationProperties().count() ==
          schedule.getDeclaredNavigationProperties().count() +
          subSchedule.getDeclaredNavigationProperties().count() +
          subsubSchedule.getDeclaredNavigationProperties().count());

      assertTrue(subsubSchedule.getDeclaredProperties().count() == 4);
      assertTrue(subsubSchedule.getProperties().count() ==
          subsubSchedule.getDeclaredProperties().count() +
          subSchedule.getDeclaredProperties().count() +
          schedule.getDeclaredProperties().count());
    }

}