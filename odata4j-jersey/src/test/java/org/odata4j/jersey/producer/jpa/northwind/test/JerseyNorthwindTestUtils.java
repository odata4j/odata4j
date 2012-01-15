package org.odata4j.jersey.producer.jpa.northwind.test;

import javax.ws.rs.core.MediaType;

import org.odata4j.producer.jpa.northwind.test.AbstractNorthwindTestUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class JerseyNorthwindTestUtils extends AbstractNorthwindTestUtils {

  @Override
  public String getWebResource(String uri, String accept, Class<String> c) {
    Client client = Client.create();
    String resource = client.resource(uri).accept(accept).get(c);
    return resource;
  }

  @Override
  public String getWebResource(String uri, Class<String> c) {
    Client client = Client.create();
    String resource = client.resource(uri).get(c);
    return resource;
  }

  @Override
  public void accept(String uri, MediaType mediaType) {

  }

  @Override
  public String getCount(String endpointUri, String uri) {
    uri = uri.replace(" ", "%20");
    Client client = Client.create();
    WebResource webResource = client.resource(endpointUri + uri);
    return webResource.accept("application/atom+xml").get(String.class);
  }
}
