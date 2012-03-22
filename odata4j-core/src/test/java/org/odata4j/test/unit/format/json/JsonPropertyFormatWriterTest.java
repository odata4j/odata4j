package org.odata4j.test.unit.format.json;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

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
    formatWriter.write(null, stringWriter, Responses.property(DATE_TIME_PROPERTY));
    assertThat(stringWriter.toString(), containsString("\\/Date(1057017600000)\\/"));
  }
}
