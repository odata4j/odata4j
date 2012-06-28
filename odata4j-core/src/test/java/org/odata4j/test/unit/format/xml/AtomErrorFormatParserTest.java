package org.odata4j.test.unit.format.xml;

import java.io.StringReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.core.OError;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.test.unit.format.AbstractErrorFormatParserTest;

public class AtomErrorFormatParserTest extends AbstractErrorFormatParserTest {

  @BeforeClass
  public static void setupClass() throws Exception {
    formatParser = FormatParserFactory.getParser(OError.class, FormatType.ATOM, null);
  }

  @Test(expected = RuntimeException.class)
  public void emptyError() throws Exception {
    formatParser.parse(new StringReader("<error>wrong error format</error>"));
  }

  @Override
  protected StringReader buildError(String code, String message, String innerError) {
    return new StringReader("<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">"
        + (code == null ? "" : "<code>" + code + "</code>")
        + (message == null ? "" : "<message lang=\"en-US\">" + message + "</message>")
        + (innerError == null ? "" : "<innererror>" + innerError + "</innererror>")
        + "</error>");
  }
}
