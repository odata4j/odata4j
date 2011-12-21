package org.odata4j.test.issues;

import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.format.xml.AtomFeedFormatParser;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.domimpl.DomXMLFactoryProvider2;

@Ignore
// we need to look into this issue again, possible loss of time zone and
// precision information when stored to database
public class Issue15 {

  @Test
  public void issue15() {

    InputStream xml = getClass().getResourceAsStream(
        "/META-INF/issue15.xml");

    XMLEventReader2 reader = DomXMLFactoryProvider2.getInstance()
        .newXMLInputFactory2()
        .createXMLEventReader(new InputStreamReader(xml));
    reader.nextEvent();
    StartElement2 propertiesElement = reader.nextEvent().asStartElement();
    for (OProperty<?> prop : AtomFeedFormatParser.parseProperties(reader,
        propertiesElement, null)) {
      if (prop.getName().equals("update_date")) {
        Assert.assertEquals("2010-11-21T12:21:51.000", prop.getValue()
            .toString());
        return;
      }
    }
    Assert.fail("Expected a property update_date");
  }

  public void repro() {

    ODataConsumer.dump.responseBody(true);
    ODataConsumer c = ODataJerseyConsumer
        .create("http://localhost:6794/WcfDataService2.svc/");

    // @SuppressWarnings("unused")
    // OEntity newEntity = c.createEntity("entity1").properties(
    // OProperties.string("name", "name"+System.currentTimeMillis())
    // ).execute(); // throws

    for (OEntity e : c.getEntities("entity1").execute())
      System.out.println(e);
  }
}
