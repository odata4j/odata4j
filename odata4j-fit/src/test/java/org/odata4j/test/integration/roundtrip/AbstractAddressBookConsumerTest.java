package org.odata4j.test.integration.roundtrip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.odata4j.core.OProperty;
import org.odata4j.test.integration.AbstractODataConsumerTest;

public abstract class AbstractAddressBookConsumerTest extends AbstractODataConsumerTest {

  public AbstractAddressBookConsumerTest(RuntimeFacadeType type) {
    super(type);
  }

  @Test
  public void entityInt16Property() throws Exception {
    OProperty<?> property = consumer.getEntity("Employees", "2").execute().getProperty("Age");
    assertThat((Short) property.getValue(), is((short) 32));
  }
}
