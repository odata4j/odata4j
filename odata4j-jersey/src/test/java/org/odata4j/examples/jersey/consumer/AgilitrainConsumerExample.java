package org.odata4j.examples.jersey.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractAgilitrainConsumerExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class AgilitrainConsumerExample extends AbstractAgilitrainConsumerExample {

  public static void main(String... args) {
    AgilitrainConsumerExample example = new AgilitrainConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }

}
