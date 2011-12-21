package org.odata4j.fit.producer;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.odata4j.fit.util.AbstractFitTest;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

public abstract class AbstractServerSmokeTest extends AbstractFitTest {

  private static final String SVC_URL = "http://localhost:8888/SmokeTest.svc/";
  private static final String META_DATA_URL = SVC_URL + "$metadata";
  private static final String FEED_URL = SVC_URL + "SupportedJaxRsImplementations";
  private static final String COM_SUN_JERSEY = "com.sun.jersey";
  private static final String ORG_APACHE_CXF = "org.apache.cxf";

  @Test
  public void testServer() throws Exception {
    this.requestResponse(SVC_URL);
    this.requestResponse(META_DATA_URL);
    this.requestResponse(FEED_URL);
  }

  @Override
  protected void createTestScenario() {
    InMemoryProducer producer = new InMemoryProducer("SmokeTest");

    producer.register(String.class, String.class, "SupportedJaxRsImplementations", new Func<Iterable<String>>() {
      public Iterable<String> apply() {
        return Enumerable.create(AbstractServerSmokeTest.COM_SUN_JERSEY, AbstractServerSmokeTest.ORG_APACHE_CXF);
      }
    }, Funcs.identity(String.class));

    DefaultODataProducerProvider.setInstance(producer);
  }

  private void requestResponse(String url) throws Exception {
    ContentExchange exchange = new ContentExchange(true);
    exchange.setURL(url);
    this.getClient().send(exchange);

    Assert.assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
    Assert.assertEquals(HttpStatus.OK_200, exchange.getResponseStatus());
    Assert.assertTrue(exchange.getResponseContent().length() > 0);
  }

  protected String getBaseUri() {
    return AbstractServerSmokeTest.SVC_URL;
  }

}
