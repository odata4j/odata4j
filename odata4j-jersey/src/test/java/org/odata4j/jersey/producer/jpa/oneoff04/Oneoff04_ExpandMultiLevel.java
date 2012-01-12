package org.odata4j.jersey.producer.jpa.oneoff04;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.examples.producer.JerseyProducerUtil;
import org.odata4j.producer.jpa.oneoff04.AbstractOneoff04ExpandMultiLevel;
import org.odata4j.producer.server.ODataServer;

public class Oneoff04_ExpandMultiLevel extends AbstractOneoff04ExpandMultiLevel {

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    return ODataJerseyConsumer.newBuilder(endpointUri).build();
  }

  @Override
  public void hostODataServer(String baseUri) {
    JerseyProducerUtil.hostODataServer(baseUri);
  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return JerseyProducerUtil.startODataServer(baseUri);
  }
}
