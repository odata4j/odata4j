package org.odata4j.examples.jersey.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractServiceListingConsumerExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.consumer.behaviors.OClientBehavior;

public class ServiceListingJerseyConsumerExample extends AbstractServiceListingConsumerExample {

  private OClientBehavior[] behaviors = {};

  public static void main(String... args) {
    TwitPicJerseyConsumerExample example = new TwitPicJerseyConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.newBuilder(endpointUri).setClientBehaviors(behaviors).build();
  }
}
