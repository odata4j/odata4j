package org.odata4j.test.unit.format.json;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.format.FormatType;
import org.odata4j.producer.Responses;
import org.odata4j.test.unit.format.AbstractPropertyFormatWriterTest;

public class JsonPropertyFormatWriterTest extends AbstractPropertyFormatWriterTest {

  @BeforeClass
  public static void setupClass() throws Exception {
    createFormatWriter(FormatType.JSON);
  }

  @Test
  public void dateTime() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(DATETIME));
    assertThat(stringWriter.toString(), containsString("\"\\/Date(1112490120000)\\/\""));
  }

  @Test
  public void dateTimeWithSeconds() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(DATETIME_WITH_SECONDS));
    assertThat(stringWriter.toString(), containsString("\"\\/Date(1146704523000)\\/\""));
  }

  @Test
  public void dateTimeWithMillis() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(DATETIME_WITH_MILLIS));
    assertThat(stringWriter.toString(), containsString("\"\\/Date(1181005323004)\\/\""));
  }

  @Test
  public void dateTimeNoOffset() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(DATETIME_BEFORE_1970_NO_OFFSET));
    assertThat(stringWriter.toString(), containsString("\"\\/Date(-12682440000+0000)\\/\""));
  }

  @Test
  public void dateTimeWithSecondsPositiveOffset() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(DATETIME_WITH_SECONDS_POSITIVE_OFFSET));
    assertThat(stringWriter.toString(), containsString("\"\\/Date(1146654123000+0420)\\/\""));
  }

  @Test
  public void dateTimeWithMillisNegativeOffset() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(DATETIME_WITH_MILLIS_NEGATIVE_OFFSET));
    assertThat(stringWriter.toString(), containsString("\"\\/Date(1181062923004-0480)\\/\""));
  }

  @Test
  public void time() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(TIME));
    assertThat(stringWriter.toString(), containsString("\"PT1H2M3S\""));
  }

  @Test
  public void timeWithMillis() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(TIME_WITH_MILLIS));
    assertThat(stringWriter.toString(), containsString("\"PT1H2M3.004S\""));
  }

  @Test
  public void bool() throws Exception {
    formatWriter.write(null, stringWriter, Responses.property(BOOLEAN_PROPERTY));
    assertTrue(Pattern.compile(".+\\{\\s*\"Boolean\"\\s*:\\s*false\\s*\\}.+", Pattern.DOTALL)
        .matcher(stringWriter.toString()).matches());
  }
}
