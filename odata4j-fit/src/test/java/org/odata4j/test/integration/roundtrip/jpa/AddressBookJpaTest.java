package org.odata4j.test.integration.roundtrip.jpa;

import org.odata4j.examples.producer.jpa.AddressBookJpaExample;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.test.integration.roundtrip.AbstractAddressBookTest;

public class AddressBookJpaTest extends AbstractAddressBookTest {

  public AddressBookJpaTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void registerODataProducer() throws Exception {
    DefaultODataProducerProvider.setInstance(AddressBookJpaExample.createProducer());
  }
}
