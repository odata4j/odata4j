package org.odata4j.jersey.consumer;

import javax.ws.rs.core.MultivaluedMap;

import org.odata4j.consumer.Response;

import com.sun.jersey.api.client.ClientResponse;

public class JerseyResponse implements Response {

  private ClientResponse clientResponse;

  public JerseyResponse(ClientResponse clientResponse) {
    this.clientResponse = clientResponse;
  }

  public ClientResponse getClientResponse() {
    return clientResponse;
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return clientResponse.getHeaders();
  }

  @Override
  public void close() {}
}
