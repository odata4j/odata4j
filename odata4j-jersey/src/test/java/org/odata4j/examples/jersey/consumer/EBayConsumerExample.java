package org.odata4j.examples.jersey.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractEBayConsumerExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class EBayConsumerExample extends AbstractEBayConsumerExample {

  public static void main(String... args) {
    EBayConsumerExample example = new EBayConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }
}
