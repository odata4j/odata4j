package org.odata4j.jersey.producer.jpa.airline.test;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.examples.producer.JerseyProducerUtil;
import org.odata4j.producer.jpa.airline.test.AbstractEdmTimeTemporalTest;
import org.odata4j.producer.server.ODataServer;

public class EdmTimeTemporalTest extends AbstractEdmTimeTemporalTest {

  @Override
  public void hostODataServer(String baseUri) {
    JerseyProducerUtil.hostODataServer(baseUri);
  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return JerseyProducerUtil.startODataServer(baseUri);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType) {
    return ODataJerseyConsumer.newBuilder(endpointUri).build();
  }

}
