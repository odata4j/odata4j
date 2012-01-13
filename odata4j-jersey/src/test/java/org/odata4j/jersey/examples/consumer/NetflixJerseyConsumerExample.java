package org.odata4j.jersey.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractNetflixConsumerExample;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class NetflixJerseyConsumerExample extends AbstractNetflixConsumerExample {

  public static void main(String[] args) {
    NetflixJerseyConsumerExample example = new NetflixJerseyConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    return ODataJerseyConsumer.create(endpointUri);
  }

}
