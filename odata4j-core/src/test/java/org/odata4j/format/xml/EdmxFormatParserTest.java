/*
 *  Copyright 2011 rozan04.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.odata4j.format.xml;

import java.io.StringReader;
import java.io.StringWriter;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmEntitySet;
import java.io.InputStreamReader;
import java.io.InputStream;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.stax2.XMLEventReader2;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import org.odata4j.internal.InternalUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rozan04
 */
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

    private void checkTypeHierarchy(EdmDataServices d) {
      EdmEntityType airport = d.findEdmEntitySet("Airport").type;
      EdmEntityType badAirport = d.findEdmEntitySet("BadAirport").type;
      assertTrue(badAirport.getBaseType().equals(airport));
      assertTrue(badAirport.getKeys().equals(airport.getKeys()));
      assertTrue(badAirport.getScopedNavigationProperties().isEmpty());
      assertTrue(badAirport.getAllNavigationProperties().count() == airport.getScopedNavigationProperties().size() +
          badAirport.getScopedNavigationProperties().size());
      assertTrue(badAirport.getScopedProperties().size() == 2);
      assertTrue(badAirport.getScopedProperty("rating") != null);
      assertTrue(badAirport.getScopedProperty("prop2") != null);
      assertTrue(badAirport.getAllProperties().count() == airport.getScopedProperties().size() +
          badAirport.getScopedProperties().size());
      assertTrue(badAirport.getProperty("name") != null);
      assertTrue(badAirport.getProperty("code") != null);
      assertTrue(badAirport.getProperty("country") != null);
      assertTrue(badAirport.getProperty("rating") != null);
      assertTrue(badAirport.getProperty("prop2") != null);

      EdmEntityType schedule = d.findEdmEntitySet("FlightSchedule").type;
      EdmEntityType subSchedule = d.findEdmEntitySet("SubFlightSchedule").type;
      assertTrue(subSchedule.getBaseType().equals(schedule));
      assertTrue(subSchedule.getKeys().equals(schedule.getKeys()));
      assertTrue(subSchedule.getScopedNavigationProperties().isEmpty());
      assertTrue(subSchedule.getAllNavigationProperties().count() == 2);
      assertTrue(subSchedule.getAllNavigationProperties().count() == schedule.getScopedNavigationProperties().size() +
          subSchedule.getScopedNavigationProperties().size());

      assertTrue(subSchedule.getScopedProperties().size() == 3);
      assertTrue(subSchedule.getScopedProperty("prop3") != null);
      assertTrue(subSchedule.getScopedProperty("prop4") != null);
      assertTrue(subSchedule.getScopedProperty("prop5") != null);
      assertTrue(subSchedule.getAllProperties().count() == schedule.getScopedProperties().size() +
          subSchedule.getScopedProperties().size());
      assertTrue(subSchedule.getProperty("arrivalAirportCode") != null);
      assertTrue(subSchedule.getProperty("flightScheduleID") != null);
      assertTrue(subSchedule.getProperty("arrivalTime") != null);
      assertTrue(subSchedule.getProperty("flightNo") != null);
      assertTrue(subSchedule.getProperty("firstDeparture") != null);
      assertTrue(subSchedule.getProperty("departureTime") != null);
      assertTrue(subSchedule.getProperty("departureAirportCode") != null);
      assertTrue(subSchedule.getProperty("lastDeparture") != null);
      assertTrue(subSchedule.getProperty("prop3") != null);
      assertTrue(subSchedule.getProperty("prop4") != null);
      assertTrue(subSchedule.getProperty("prop5") != null);

      EdmEntityType subsubSchedule = d.findEdmEntitySet("SubSubFlightSchedule").type;
      assertTrue(subsubSchedule.getBaseType().equals(subSchedule));
      assertTrue(subsubSchedule.getKeys().equals(subSchedule.getKeys()));
      assertTrue(subsubSchedule.getScopedNavigationProperties().size() == 1);
      assertTrue(subsubSchedule.getAllNavigationProperties().count() ==
          schedule.getScopedNavigationProperties().size() +
          subSchedule.getScopedNavigationProperties().size() +
          subsubSchedule.getScopedNavigationProperties().size());

      assertTrue(subsubSchedule.getScopedProperties().size() == 4);
      assertTrue(subsubSchedule.getAllProperties().count() ==
          subsubSchedule.getScopedProperties().size() +
          subSchedule.getScopedProperties().size() +
          schedule.getScopedProperties().size());
    }

}