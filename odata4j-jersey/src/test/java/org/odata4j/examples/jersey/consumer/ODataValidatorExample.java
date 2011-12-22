package org.odata4j.examples.jersey.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractODataValidatorExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class ODataValidatorExample extends AbstractODataValidatorExample {

  public static void main(String... args) {
    ODataValidatorExample example = new ODataValidatorExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }

}
