package org.odata4j.test.unit.issues;

import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.XMLFactoryProvider2;

// http://code.google.com/p/odata4j/issues/detail?id=212
public class Issue212Test {

  @Test
  public void issue212() {
    InputStream metadataStream = getClass().getResourceAsStream("/META-INF/issue212_wxcex_metadata.xml");
    XMLEventReader2 reader = XMLFactoryProvider2.getInstance().newXMLInputFactory2().createXMLEventReader(new InputStreamReader(metadataStream));
    EdmDataServices metadata = new EdmxFormatParser().parseMetadata(reader);
    Assert.assertNotNull(metadata);
  }

}
