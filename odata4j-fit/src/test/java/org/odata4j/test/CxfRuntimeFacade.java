package org.odata4j.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.cxf.consumer.ODataCxfConsumer;
import org.odata4j.cxf.consumer.ODataCxfConsumer.Builder;
import org.odata4j.cxf.producer.server.CxfJettyServer;
import org.odata4j.format.FormatType;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

public class CxfRuntimeFacade implements RuntimeFacade {

  MediaType mediaType = null;

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
    Builder builder = ODataCxfConsumer.newBuilder(endpointUri);

    if (format != null) {
      builder = builder.setFormatType(format);
    }

    //    if (methodToTunnel != null) {
    //      builder = builder.setClientBehaviors(new MethodTunnelingBehavior(methodToTunnel));
    //    }

    return builder.build();
  }

  @Override
  public String acceptAndReturn(String uri, MediaType mediaType) {
    uri = uri.replace(" ", "%20");
    return this.getResource(uri, mediaType.toString());
  }

  @Override
  public String getWebResource(String uri, String accept) {
    uri = uri.replace(" ", "%20");
    return this.getResource(uri, accept);
  }

  @Override
  public void accept(String uri, MediaType mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String getWebResource(String uri) {
    return this.getResource(uri, null);
  }

  private String getResource(String uri, String accept) {
    String resource = null;
    try {
      HttpClient httpClient = new DefaultHttpClient();

        if (System.getProperties().containsKey("http.proxyHost") && System.getProperties().containsKey("http.proxyPort")) {
          // support proxy settings
          String hostName = System.getProperties().getProperty("http.proxyHost");
          String hostPort = System.getProperties().getProperty("http.proxyPort");
          
          HttpHost proxy = new HttpHost(hostName, Integer.parseInt(hostPort));
          httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
      
      // Prepare a request object
      HttpGet httpget = new HttpGet(uri);
      if (accept != null) {
        httpget.addHeader("accept", accept);
      }
      if (this.mediaType != null) {
        String mt = this.mediaType.toString();
        httpget.addHeader("accept", mt);
      }
      // Execute the request
      HttpResponse response = httpClient.execute(httpget);
      // Examine the response status
      System.out.println(response.getStatusLine());
      // Get hold of the response entity
      HttpEntity entity = response.getEntity();
      // If the response does not enclose an entity, there is no need
      // to worry about connection release
      if (entity != null) {
        resource = EntityUtils.toString(entity);
      }
      return resource;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
