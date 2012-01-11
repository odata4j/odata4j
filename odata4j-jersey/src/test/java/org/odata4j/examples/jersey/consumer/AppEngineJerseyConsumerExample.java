package org.odata4j.examples.jersey.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractAppEngineConsumerExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class AppEngineJerseyConsumerExample extends AbstractAppEngineConsumerExample {

  public static void main(String[] args) {
    AppEngineJerseyConsumerExample example = new AppEngineJerseyConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }

}
