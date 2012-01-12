package org.odata4j.jersey.test.expression;

import javax.ws.rs.core.MediaType;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.examples.producer.JerseyProducerUtil;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.expressions.AbstractJsonTest;

import com.sun.jersey.api.client.Client;

public class JsonTest extends AbstractJsonTest {

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    return ODataJerseyConsumer.newBuilder(endpointUri).setFormatType(formatType).build();
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
  public String getWebResource(String uri, String accept, Class<String> c) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public String getWebResource(String uri, Class<String> c) {
    Client client = new Client();
    return client.resource(uri).get(c);
  }

  @Override
  public void accept(String uri, MediaType mediaType) {
    throw new RuntimeException("not implemented");
  }

}
