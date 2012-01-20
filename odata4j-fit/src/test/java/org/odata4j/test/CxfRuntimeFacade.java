package org.odata4j.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Assert;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.cxf.consumer.ODataCxfConsumer;
import org.odata4j.cxf.producer.server.CxfJettyServer;
import org.odata4j.format.FormatType;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

public class CxfRuntimeFacade implements RuntimeFacade {

  private static final long REQUEST_TIMEOUT = 10 * 60 * 1000; // 10 minutes for debugging

  static {
    // ensure that the correct JAX-RS implementation is loaded
    RuntimeDelegate runtimeDelegate = new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl();
    RuntimeDelegate.setInstance(runtimeDelegate);
    Assert.assertEquals(runtimeDelegate, RuntimeDelegate.getInstance());
  }

  @Override
  public void hostODataServer(String baseUri) {
    try {
      ODataServer server = this.startODataServer(baseUri);
      System.out.println("Press any key to exit");
      new BufferedReader(new InputStreamReader(System.in)).readLine();
      server.stop();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return new CxfJettyServer(baseUri, DefaultODataApplication.class, RootApplication.class).start();
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType format, String methodToTunnel) {
    return ODataCxfConsumer.create(endpointUri);
  }

  @Override
  public String acceptAndReturn(String uri, MediaType mediaType) {
    uri = uri.replace(" ", "%20");
    WebClient client = creatWebClient(uri);

    String resource = client.accept(mediaType).get(String.class);
    return resource;
  }

  @Override
  public String getWebResource(String uri, String accept) {
    uri = uri.replace(" ", "%20");
    WebClient client = creatWebClient(uri);

    String resource = client.accept(accept).get(String.class);
    return resource;
  }

  @Override
  public void accept(String uri, MediaType mediaType) {
    uri = uri.replace(" ", "%20");
    WebClient client = creatWebClient(uri);
    client.accept(mediaType);
  }
  
  @Override
  public String getWebResource(String uri) {
    WebClient client = this.creatWebClient(uri);
    
    String resource = client.get(String.class);
    return resource;
  }

  private WebClient creatWebClient(String uri) {
    WebClient client = WebClient.create(uri);
    // request timeout for debugging
    WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(CxfRuntimeFacade.REQUEST_TIMEOUT);
    return client;
  }
}
