package org.odata4j.jersey.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractODataTestServiceReadWriteExample;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class ODataTestServiceReadWriteJerseyExample extends AbstractODataTestServiceReadWriteExample {

  public static void main(String[] args) {
    ODataTestServiceReadWriteJerseyExample example = new ODataTestServiceReadWriteJerseyExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType) {
    return ODataJerseyConsumer.create(endpointUri);
  }

}
