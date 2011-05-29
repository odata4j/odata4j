package org.odata4j.format.xml;

import java.util.ArrayList;
import java.util.List;

import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;

public class XmlLinkParser extends XmlFormatParser {

  private static final QName2 URI = new QName2(NS_DATASERVICES, "uri");
  
  public static Iterable<String> parseLinkUris(XMLEventReader2 reader) {
    List<String> rt = new ArrayList<String>();
    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();
      if (isStartElement(event, URI)) {
        rt.add(reader.getElementText());
      }
    }
    return rt;
  }
  
}
