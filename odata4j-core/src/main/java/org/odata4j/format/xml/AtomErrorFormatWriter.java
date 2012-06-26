package org.odata4j.format.xml;

import java.io.Writer;

import javax.ws.rs.core.UriInfo;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OError;
import org.odata4j.format.FormatWriter;
import org.odata4j.producer.ErrorResponse;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;

public class AtomErrorFormatWriter extends XmlFormatWriter implements FormatWriter<ErrorResponse> {

  public void write(UriInfo uriInfo, Writer w, ErrorResponse target) {
    String debugParameter = uriInfo.getQueryParameters().getFirst("odata4j.debug");
    boolean innerError = debugParameter != null && Boolean.parseBoolean(debugParameter);
    XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
    writer.startDocument();
    writeError(writer, target.getError(), m, innerError);
    writer.endDocument();
  }

  private static void writeError(XMLWriter2 writer, OError error, String xmlns, boolean innerError) {
    writer.startElement(new QName2("error"), xmlns);
    writer.startElement("code");
    writer.writeText(error.getCode());
    writer.endElement("code");
    writer.startElement("message");
    writer.writeAttribute("lang", "en-US");
    writer.writeText(error.getMessage());
    writer.endElement("message");
    if (innerError) {
      writer.startElement("innererror");
      writer.writeText(error.getInnerError());
      writer.endElement("innererror");
    }
    writer.endElement("error");
  }

  @Override
  public String getContentType() {
    return ODataConstants.APPLICATION_XML_CHARSET_UTF8;
  }
}