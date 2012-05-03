package org.odata4j.test.unit.format.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.format.FormatType;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonParseException;
import org.odata4j.test.unit.format.AbstractEntryFormatParserTest;

public class JsonEntryFormatParserTest extends AbstractEntryFormatParserTest {

  @BeforeClass
  public static void setupClass() throws Exception {
    createFormatParser(FormatType.JSON);
  }

  @Test
  public void dateTime() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildJson("\"DateTime\" : \"\\/Date(1112490120000)\\/\"")), DATETIME);
  }

  @Test
  public void dateTimeWithSeconds() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildJson("\"DateTime\" : \"\\/Date(1146704523000)\\/\"")), DATETIME_WITH_SECONDS);
  }

  @Test
  public void dateTimeWithMillis() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildJson("\"DateTime\" : \"\\/Date(1181005323004)\\/\"")), DATETIME_WITH_MILLIS);
    verifyDateTimePropertyValue(formatParser.parse(buildJson("\"DateTime\" : \"\\/Date(1181005323010)\\/\"")), DATETIME_WITH_MILLIS.withMillisOfSecond(10));
  }

  @Test
  public void dateTimeWithOffset() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildJson("\"DateTime\" : \"\\/Date(-12682440000+0000)\\/\"")), DATETIME_BEFORE_1970_NO_OFFSET.toLocalDateTime());
  }

  @Test
  public void dateTimeInXmlFormat() throws Exception {
    verifyDateTimePropertyValue(formatParser.parse(buildJson("\"DateTime\" : \"2005-04-03T01:02\"")), DATETIME);
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalDateTime() throws Exception {
    formatParser.parse(buildJson("\"DateTime\" : \"1969-08-07T05:06:00Z\""));
  }

  @Test
  public void dateTimeNoOffset() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildJson("\"DateTimeOffset\" : \"\\/Date(-12682440000+0000)\\/\"")), DATETIME_BEFORE_1970_NO_OFFSET);
  }

  @Test
  public void dateTimeWithSecondsPositiveOffset() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildJson("\"DateTimeOffset\" : \"\\/Date(1146654123000+0420)\\/\"")), DATETIME_WITH_SECONDS_POSITIVE_OFFSET);
  }

  @Test
  public void dateTimeWithMillisNegativeOffset() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildJson("\"DateTimeOffset\" : \"\\/Date(1181062923004-0480)\\/\"")), DATETIME_WITH_MILLIS_NEGATIVE_OFFSET);
  }

  @Test
  public void dateTimeWithoutOffset() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildJson("\"DateTimeOffset\" : \"\\/Date(1112490120000)\\/\"")), DATETIME.toDateTime(DateTimeZone.UTC));
  }

  @Test
  public void dateTimeNoOffsetInXmlFormat() throws Exception {
    verifyDateTimeOffsetPropertyValue(formatParser.parse(buildJson("\"DateTimeOffset\" : \"1969-08-07T05:06:00Z\"")), DATETIME_BEFORE_1970_NO_OFFSET);
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalDateTimeOffset() throws Exception {
    formatParser.parse(buildJson("\"DateTimeOffset\" : \"2005-04-03T01:02\""));
  }

  @Test
  public void time() throws Exception {
    verifyTimePropertyValue(formatParser.parse(buildJson("\"Time\" : \"PT1H2M3S\"")), TIME);
  }

  @Test
  public void timeWithMillis() throws Exception {
    verifyTimePropertyValue(formatParser.parse(buildJson("\"Time\" : \"PT1H2M3.004S\"")), TIME_WITH_MILLIS);
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalTime() throws Exception {
    formatParser.parse(buildJson("\"Time\" : \"01:02:03\""));
  }

  @Test
  public void bool() throws Exception {
    assertThat((Boolean) formatParser.parse(buildJson("\"Boolean\": true")).getEntity().getProperty(BOOLEAN_NAME).getValue(), is(BOOLEAN));
  }

  @Test(expected=JsonParseException.class)
  public void illegalBoolean() throws Exception {
    formatParser.parse(buildJson("\"Boolean\": undefined"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalBooleanFormat() throws Exception {
    formatParser.parse(buildJson("\"Boolean\": \"falsetrue\""));
  }

  private StringReader buildJson(String property) {
    return new StringReader("" +
        "{" +
        "\"d\" : {" + property + "}" +
        "}");
  }
}
