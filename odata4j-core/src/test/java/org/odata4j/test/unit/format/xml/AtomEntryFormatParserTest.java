package org.odata4j.test.unit.format.xml;

import java.io.StringReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.format.FormatType;
import org.odata4j.test.unit.format.AbstractEntryFormatParserTest;

public class AtomEntryFormatParserTest extends AbstractEntryFormatParserTest {

  @BeforeClass
  public static void setupClass() throws Exception {
    createFormatParser(FormatType.ATOM);
  }

  @Test
  public void dateTime() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildAtom("<d:DateTime m:type=\"Edm.DateTime\">2003-07-01T00:00:00</d:DateTime>")));
  }

  private StringReader buildAtom(String property) {
    return new StringReader("" +
        "<entry" +
        " xmlns=\"http://www.w3.org/2005/Atom\"" +
        " xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"" +
        " xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\">" +
        "<content type=\"application/xml\">" +
        "<m:properties>" + property + "</m:properties>" +
        "</content>" +
        "</entry>");
  }
}
