package org.odata4j.test.producer;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.Assert;

import org.core4j.Func;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.examples.producer.ProducerUtil.ServerType;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;

public class ServerSmokeTest {

  private static class LogClassLoader extends ClassLoader {

    private List<String> log = new ArrayList<String>();

    @Override
    protected synchronized Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
      log.add(className);
      System.out.println("Class loaded: " + className);
      return super.loadClass(className, resolve);
    }
  }

  private static class SupportedJaxRsImplementation {

    private int id;
    private String name;

    private SupportedJaxRsImplementation(int id, String name) {
      this.id = id;
      this.name = name;
    }

    @SuppressWarnings("unused")
    public int getId() {
      return id;
    }

    @SuppressWarnings("unused")
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return String.format("SupportedJaxRsImplementation[Id=%s,Name=%s]", id, name);
    }
  }

  private static final String SVC_URL = "http://localhost:8888/SmokeTest.svc/";
  private static final String META_DATA_URL = SVC_URL + "$metadata";
  private static final String FEED_URL = SVC_URL + "SupportedJaxRsImplementations";
  private static final String COM_SUN_JERSEY = "com.sun.jersey";
  private static final String ORG_APACHE_CXF = "org.apache.cxf";

  private ODataServer server;
  private HttpClient client;
  private LogClassLoader classLoader;

  @Before
  public void setup() {
    classLoader = new LogClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
  }

  @Test
  public void testJerseyServer() throws Exception {
    setRuntimeDelegateImpl(new com.sun.jersey.server.impl.provider.RuntimeDelegateImpl());
    createInMemoryProducer();
    startServer(ServerType.JERSEY);

    performSmokeTest();

    checkClassLoaderLog(COM_SUN_JERSEY, ORG_APACHE_CXF);
  }

  @Test
  public void testCxfJettyServer() throws Exception {
    setRuntimeDelegateImpl(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    createInMemoryProducer();
    startServer(ServerType.CXF_JETTY);

    performSmokeTest();

    checkClassLoaderLog(ORG_APACHE_CXF, COM_SUN_JERSEY);
  }

  @After
  public void teardown() {
    try {
      client.stop();
      server.stop();
    } catch (Exception e) {}
  }

  private void setRuntimeDelegateImpl(RuntimeDelegate runtimeDelegate) {
    // ensure that the correct JAX-RS implementation is loaded
    RuntimeDelegate.setInstance(runtimeDelegate);

    Assert.assertEquals(runtimeDelegate, RuntimeDelegate.getInstance());
  }

  private void createInMemoryProducer() {
    InMemoryProducer producer = new InMemoryProducer("SmokeTest");

    producer.register(SupportedJaxRsImplementation.class, Integer.TYPE, "SupportedJaxRsImplementations",
        new Func<Iterable<SupportedJaxRsImplementation>>() {
          public Iterable<SupportedJaxRsImplementation> apply() {
            List<SupportedJaxRsImplementation> writers = new ArrayList<SupportedJaxRsImplementation>();
            writers.add(new SupportedJaxRsImplementation(1, "Jersey"));
            writers.add(new SupportedJaxRsImplementation(2, "CXF"));
            return writers;
          }
        }, "Id");

    DefaultODataProducerProvider.setInstance(producer);
  }

  private void startServer(ServerType serverType) {
    server = ProducerUtil.startODataServer(SVC_URL, serverType);
  }

  private void performSmokeTest() throws Exception {
    startClient();

    requestResponse(SVC_URL);
    requestResponse(META_DATA_URL);
    requestResponse(FEED_URL);
  }

  private void startClient() throws Exception {
    client = new HttpClient();
    client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    client.start();
  }

  private void requestResponse(String url) throws Exception {
    ContentExchange exchange = new ContentExchange(true);
    exchange.setURL(url);
    client.send(exchange);

    Assert.assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
    Assert.assertEquals(HttpStatus.OK_200, exchange.getResponseStatus());
    Assert.assertTrue(exchange.getResponseContent().length() > 0);
  }

  private void checkClassLoaderLog(String requiredPackagePrefix, String disallowedPackagePrefix) {
    boolean requiredPackagePrefixFound = false;
    for (String c : classLoader.log) {
      if (!requiredPackagePrefixFound && c.startsWith(requiredPackagePrefix))
        requiredPackagePrefixFound = true;
      if (c.startsWith(disallowedPackagePrefix))
        Assert.fail("Loaded class '" + c + "' belongs to disallowed package '" + disallowedPackagePrefix + "'");
    }
    if (!requiredPackagePrefixFound)
      Assert.fail("No class from required package '" + requiredPackagePrefix + "' loaded");
  }
}
