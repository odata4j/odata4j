package org.odata4j.examples.jersey.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractTwitPicConsumerExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class TwitPicConsumerExample extends AbstractTwitPicConsumerExample {

  public static void main(String... args) {
    TwitPicConsumerExample example = new TwitPicConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }


}
