package org.odata4j.format.xml;

import java.io.Writer;

import org.odata4j.core.ODataConstants;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.SingleLink;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class AtomSingleLinkFormatWriter extends XmlFormatWriter implements FormatWriter<SingleLink> {

  @Override
  public void write(ExtendedUriInfo uriInfo, Writer w, SingleLink link) {
    XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
    writer.startDocument();

    String xmlns = d;
    writer.startElement(new QName2("uri"), xmlns);
    writer.writeText(link.getUri());
    writer.endElement("uri");
    writer.endDocument();
  }

  @Override
  public String getContentType() {
    return ODataConstants.APPLICATION_XML_CHARSET_UTF8;
  }

}
