package org.odata4j.test.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;
import org.odata4j.cxf.producer.server.CxfJettyServer;
import org.odata4j.jersey.producer.server.JerseyServer;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class ServerFilteringTest extends AbstractSimpleInMemoryProducerTest {

  public ServerFilteringTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void startODataServer() throws Exception {
    server = rtFacade.createODataServer(SVC_URL);
    addRequestFilter();
    server.start();
  }

  @Test
  public void testRequestFilter() throws Exception {
    ContentExchange exchange = sendRequest(SVC_URL);
    exchange.waitForDone();
    verifyRequestFilterIsCalled();
  }

  public static class CxfJettyRequestFilterStub extends AbstractHandler {
    static boolean isCalled = false;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      isCalled = true;
    }
  }

  public static class JerseyRequestFilterStub implements ContainerRequestFilter {
    static boolean isCalled = false;

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      isCalled = true;
      return request;
    }
  }

  private void addRequestFilter() {
    if (server instanceof CxfJettyServer)
      ((CxfJettyServer) server).addJettyRequestHandler(new CxfJettyRequestFilterStub());
    else if (server instanceof JerseyServer)
      ((JerseyServer) server).addJerseyRequestFilter(JerseyRequestFilterStub.class);
  }

  private void verifyRequestFilterIsCalled() {
    if (server instanceof CxfJettyServer)
      Assert.assertTrue("Request filter has not been called", CxfJettyRequestFilterStub.isCalled);
    else if (server instanceof JerseyServer)
      Assert.assertTrue("Request filter has not been called", JerseyRequestFilterStub.isCalled);
  }
}
