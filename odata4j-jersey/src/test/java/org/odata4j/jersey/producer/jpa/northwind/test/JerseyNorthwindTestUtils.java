package org.odata4j.jersey.producer.jpa.northwind.test;

import javax.ws.rs.core.MediaType;

import org.odata4j.producer.jpa.northwind.test.AbstractNorthwindTestUtils;

import com.sun.jersey.api.client.Client;

public class JerseyNorthwindTestUtils extends AbstractNorthwindTestUtils {

  @Override
  public String getWebResource(String uri, String accept, Class<String> c) {
    Client client = Client.create();
    String resource = client.resource(uri).accept(accept).get(c);
    ;
    return resource;
  }

  @Override
  public String getWebResource(String uri, Class<String> c) {
    Client client = Client.create();
    String resource = client.resource(uri).get(c);
    ;
    return resource;
  }

  @Override
  public void accept(String uri, MediaType mediaType) {

  }
}
