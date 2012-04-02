package org.odata4j.test.integration.roundtrip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.odata4j.core.OProperty;
import org.odata4j.format.FormatType;
import org.odata4j.test.integration.AbstractODataConsumerTest;

public abstract class AbstractAddressBookJsonConsumerTest extends AbstractODataConsumerTest {

  public AbstractAddressBookJsonConsumerTest(RuntimeFacadeType type) {
    super(type, FormatType.JSON);
  }

  @Test
  public void entityInt16Property() throws Exception {
    OProperty<?> property = consumer.getEntity("Employees", "2").execute().getProperty("Age");
    assertThat((Short) property.getValue(), is((short) 32));
  }

  @Test
  public void entityDateTimeProperty() throws Exception {
    OProperty<?> property = consumer.getEntity("Employees", "2").execute().getProperty("EntryDate");
    assertThat((LocalDateTime) property.getValue(), is(new LocalDateTime(2003, 7, 1, 0, 0)));
  }
}
