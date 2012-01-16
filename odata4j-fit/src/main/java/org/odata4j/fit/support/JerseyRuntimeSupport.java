package org.odata4j.fit.support;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.examples.producer.JerseyProducerUtil;
import org.odata4j.producer.server.ODataServer;

public class JerseyRuntimeSupport implements RuntimeSupport {

  public void hostODataServer(String baseUri) {
    JerseyProducerUtil.hostODataServer(baseUri);
  }

  public ODataServer startODataServer(String baseUri) {
    return JerseyProducerUtil.startODataServer(baseUri);
  }

  public ODataConsumer create(String endpointUri) {
    return ODataJerseyConsumer.create(endpointUri);
  }
}
