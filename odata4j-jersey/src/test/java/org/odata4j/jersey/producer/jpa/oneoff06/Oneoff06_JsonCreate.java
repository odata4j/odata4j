package org.odata4j.jersey.producer.jpa.oneoff06;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.examples.producer.JerseyProducerUtil;
import org.odata4j.producer.jpa.oneoff06.AbstractOneoff06JsonCreate;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class Oneoff06_JsonCreate extends AbstractOneoff06JsonCreate {

  private ClientResponse response;

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

  @Override
  protected String getResponseEntity() {
    return this.response.getEntity(String.class);
  }

  @Override
  protected int getResponseStatus() {
    return this.response.getStatus();
  }

  @Override
  protected String getResponseType() {
    return this.response.getType().toString();
  }

  @Override
  protected void requestPost() {
    Client client = Client.create();
    this.response = client.resource(endpointUri)
        .path("Country")
        .accept("application/json") // will fail without this line
        .type("application/json;charset=utf-8")
        .post(ClientResponse.class, "{ \"name\":\"Ireland\"}");
  }
}
