package org.odata4j.test.unit.format;

import java.io.StringWriter;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatType;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.PropertyResponse;

public abstract class AbstractPropertyFormatWriterTest {

  protected static final OProperty<LocalDateTime> DATE_TIME_PROPERTY = OProperties.simple("DateTime", EdmSimpleType.DATETIME, new LocalDateTime(2003, 7, 1, 0, 0));

  protected static FormatWriter<PropertyResponse> formatWriter;

  protected StringWriter stringWriter;

  protected static void createFormatWriter(FormatType format) {
    formatWriter = FormatWriterFactory.getFormatWriter(PropertyResponse.class, null, format.toString(), null);
  }

  @Before
  public void setup() throws Exception {
    stringWriter = new StringWriter();
  }
}
