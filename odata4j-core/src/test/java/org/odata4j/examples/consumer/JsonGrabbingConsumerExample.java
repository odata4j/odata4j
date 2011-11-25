package org.odata4j.examples.consumer;

import java.io.ByteArrayInputStream;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.BaseClientBehavior;
import org.odata4j.format.FormatType;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.Filterable;

public class JsonGrabbingConsumerExample {

  public static void main(String[] args) {
    ResponseGrabbingClientBehavior responseGrabbingBehavior = new ResponseGrabbingClientBehavior();

    String serviceUri = "http://services.odata.org/Northwind/Northwind.svc";
    ODataConsumer c = ODataConsumer.newBuilder(serviceUri)
        .setFormatType(FormatType.JSON)
        .setClientBehaviors(responseGrabbingBehavior)
        .build();

    c.getEntity("Customers", "VICTE").execute();
    System.out.println(responseGrabbingBehavior.lastResponse);
  }

  private static class ResponseGrabbingClientBehavior extends BaseClientBehavior {

    public String lastResponse;

    @Override
    public void modifyClientFilters(Filterable client) {
      client.addFilter(new ClientFilter(){
        @Override
        public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
          ClientResponse response = getNext().handle(clientRequest);
          lastResponse = response.getEntity(String.class);
          // we consumed the response stream, replace it to avoid breaking downstream processing
          response.setEntityInputStream(new ByteArrayInputStream(lastResponse.getBytes()));
          return response;
        }});
    }

  }

}
