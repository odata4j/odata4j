package org.odata4j.jersey.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractAgilitrainConsumerExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class AgilitrainJerseyConsumerExample extends AbstractAgilitrainConsumerExample {

  public static void main(String[] args) {
    AgilitrainJerseyConsumerExample example = new AgilitrainJerseyConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }

}
