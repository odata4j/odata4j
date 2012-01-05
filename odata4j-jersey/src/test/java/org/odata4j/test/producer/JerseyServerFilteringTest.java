package org.odata4j.test.producer;

import junit.framework.Assert;

import org.odata4j.fit.producer.AbstractServerFilteringTest;
import org.odata4j.jersey.server.JerseyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class JerseyServerFilteringTest extends AbstractServerFilteringTest {

  public static class RequestFilterStub implements ContainerRequestFilter {
    static boolean isCalled = false;

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      isCalled = true;
      return request;
    }
  }

  @Override
  protected void createServer() {
    server = new JerseyServer(SVC_URL, DefaultODataApplication.class, RootApplication.class)
               .addJerseyRequestFilter(RequestFilterStub.class);
  }

  @Override
  protected void verifyRequestFilterIsCalled() {
    Assert.assertTrue("Request filter has not been called", RequestFilterStub.isCalled);
  }
}
