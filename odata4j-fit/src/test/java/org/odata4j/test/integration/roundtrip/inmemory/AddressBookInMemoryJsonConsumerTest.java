package org.odata4j.test.integration.roundtrip.inmemory;

import org.odata4j.examples.producer.inmemory.AddressBookInMemoryExample;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.test.integration.roundtrip.AbstractAddressBookJsonConsumerTest;

public class AddressBookInMemoryJsonConsumerTest extends AbstractAddressBookJsonConsumerTest {

  public AddressBookInMemoryJsonConsumerTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void registerODataProducer() throws Exception {
    DefaultODataProducerProvider.setInstance(AddressBookInMemoryExample.createProducer());
  }
}
