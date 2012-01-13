package org.odata4j.jersey.examples.producer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.producer.AbstractRoundtripExample;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.producer.server.ODataServer;

public class RoundtripExample extends AbstractRoundtripExample {
  public static void main(String[] args) {
    RoundtripExample example = new RoundtripExample();
    example.run(args);
  }

  @Override
  public void hostODataServer(String baseUri) {
    JerseyProducerUtil.hostODataServer(baseUri);

  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return JerseyProducerUtil.startODataServer(baseUri);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    return ODataJerseyConsumer.create(endpointUri);
  }
}