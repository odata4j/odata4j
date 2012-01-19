package org.odata4j.test.producer;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.odata4j.cxf.producer.server.CxfJettyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;

public class CxfServerFilteringTest extends AbstractServerFilteringTest {

  public static class RequestFilterStub extends AbstractHandler {
    static boolean isCalled = false;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      isCalled = true;
    }
  }

  @Override
  protected void createServer() {
    server = new CxfJettyServer(SVC_URL, DefaultODataApplication.class, RootApplication.class)
               .addJettyRequestHandler(new RequestFilterStub());
  }

  @Override
  protected void verifyRequestFilterIsCalled() {
    Assert.assertTrue("Request filter has not been called", RequestFilterStub.isCalled);
  }
}

