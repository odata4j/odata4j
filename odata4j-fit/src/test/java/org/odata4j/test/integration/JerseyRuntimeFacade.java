package org.odata4j.test.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;
import org.odata4j.core.Throwables;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.consumer.ODataJerseyConsumer.Builder;
import org.odata4j.jersey.producer.server.ODataJerseyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class JerseyRuntimeFacade implements RuntimeFacade {

  private int lastStatusCode;

  @Override
  public void hostODataServer(String baseUri) {
    try {
      ODataServer server = startODataServer(baseUri);
      System.out.println("Press any key to exit");
      new BufferedReader(new InputStreamReader(System.in)).readLine();
      server.stop();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return this.createODataServer(baseUri).start();
  }

  @Override
  public ODataServer createODataServer(String baseUri) {

    return new ODataJerseyServer(baseUri, DefaultODataApplication.class, RootApplication.class)
        .addJerseyRequestFilter(LoggingFilter.class); // log all requests
  }

  @Override
  public ODataConsumer createODataConsumer(String endpointUri, FormatType format, String methodToTunnel) {
    Builder builder = ODataJerseyConsumer.newBuilder(endpointUri);

    if (format != null) {
      builder = builder.setFormatType(format);
    }

    if (methodToTunnel != null) {
      builder = builder.setClientBehaviors(new MethodTunnelingBehavior(methodToTunnel));
    }

    return builder.build();
  }

  @Override
  public String getWebResource(String uri) {
    WebResource webResource = new Client().resource(uri);

    ClientResponse response = webResource.get(ClientResponse.class);
    this.lastStatusCode = response.getStatus();
    String body = response.getEntity(String.class);

    return body;
  }
  
  @Override
  public int postWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    Client client = new Client();
    WebResource.Builder webResource = client.resource(uri).type(mediaType);

    try {
      if (null != headers) {
        for (Entry<String, Object> entry : headers.entrySet()) {
          webResource = webResource.header(entry.getKey(), entry.getValue());
        }
      }
      ClientResponse response = webResource.post(ClientResponse.class, content);
      this.lastStatusCode = response.getStatus();
    } catch(UniformInterfaceException ex) {
      this.lastStatusCode = ex.getResponse().getStatus();
    }
    return this.lastStatusCode;
  }
  
  public int putWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    Client client = new Client();
    WebResource.Builder webResource = client.resource(uri).type(mediaType);

    try {
      if (null != headers) {
        for (Entry<String, Object> entry : headers.entrySet()) {
          webResource = webResource.header(entry.getKey(), entry.getValue());
        }
      }
      ClientResponse response = webResource.put(ClientResponse.class, content);
      this.lastStatusCode = response.getStatus();
    } catch(UniformInterfaceException ex) {
      this.lastStatusCode = ex.getResponse().getStatus();
    }
    return this.lastStatusCode;
  }

  @Override
  public String acceptAndReturn(String uri, MediaType mediaType) {
    uri = uri.replace(" ", "%20");
    
    WebResource webResource = new Client().resource(uri);

    ClientResponse response = webResource.accept(mediaType).get(ClientResponse.class);
    this.lastStatusCode = response.getStatus();
    String body = response.getEntity(String.class);

    return body;
  }

  @Override
  public String getWebResource(String uri, String accept) {
    WebResource webResource = new Client().resource(uri);

    ClientResponse response = webResource.accept(accept).get(ClientResponse.class);
    this.lastStatusCode = response.getStatus();
    String body = response.getEntity(String.class);

    return body;
  }

  @Override
  public void accept(String uri, MediaType mediaType) {
    uri = uri.replace(" ", "%20");
    WebResource webResource = new Client().resource(uri);
    webResource.accept(mediaType);
  }

  @Override
  public int getLastStatusCode() {
    return this.lastStatusCode;
  }

}
