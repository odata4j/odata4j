package org.odata4j.test.unit.edm;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;

public class EdmDataServicesTest {

  @Test
  public void testFindFullyQualifiedEntityNames() {
    String edmxFile = "/META-INF/edmx.xml";
    XMLEventReader2 reader = StaxUtil.newXMLEventReader(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(edmxFile))));
    EdmDataServices d = new EdmxFormatParser().parseMetadata(reader);

    Assert.assertNotNull(d.findEdmEntitySet("Flight"));
    Assert.assertNotNull(d.findEdmEntitySet("AirlineEntities.Flight"));
  }
}
