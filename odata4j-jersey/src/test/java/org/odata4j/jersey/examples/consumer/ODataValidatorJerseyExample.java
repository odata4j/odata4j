package org.odata4j.jersey.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractODataValidatorExample;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class ODataValidatorJerseyExample extends AbstractODataValidatorExample {

  public static void main(String[] args) {
    ODataValidatorJerseyExample example = new ODataValidatorJerseyExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }

}
