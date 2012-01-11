package org.odata4j.examples.jersey.consumer;

import java.io.ByteArrayInputStream;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.consumers.AbstractJsonGrabbingConsumerExample;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.consumer.behaviors.JerseyClientBehavior;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.Filterable;

public class JsonGrabbingJerseyConsumerExample extends AbstractJsonGrabbingConsumerExample {

  public static void main(String[] args) {
    TwitPicJerseyConsumerExample example = new TwitPicJerseyConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri) {
    ResponseGrabbingClientBehavior responseGrabbingBehavior = new ResponseGrabbingClientBehavior();

    ODataConsumer c = ODataJerseyConsumer.newBuilder(endpointUri)
        .setFormatType(FormatType.JSON)
        .setClientBehaviors(responseGrabbingBehavior)
        .build();

    return c;
  }

  private static class ResponseGrabbingClientBehavior implements JerseyClientBehavior {

    public String lastResponse;

    @Override
    public void modifyClientFilters(Filterable client) {
      client.addFilter(new ClientFilter() {
        @Override
        public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
          ClientResponse response = getNext().handle(clientRequest);
          lastResponse = response.getEntity(String.class);
          // we consumed the response stream, replace it to avoid breaking downstream processing
          response.setEntityInputStream(new ByteArrayInputStream(lastResponse.getBytes()));
          return response;
        }
      });
    }

    @Override
    public ODataClientRequest transform(ODataClientRequest request) {
      return request;
    }

    @Override
    public void modify(ClientConfig clientConfig) {
      // nop
    }

    @Override
    public void modifyWebResourceFilters(Filterable webResource) {
      // nop
    }

  }

}
