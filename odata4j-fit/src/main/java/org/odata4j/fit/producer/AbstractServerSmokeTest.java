package org.odata4j.fit.producer;

import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.odata4j.fit.util.AbstractODataServerHttpClientSimpleInMemoryProducerTest;

public abstract class AbstractServerSmokeTest extends AbstractODataServerHttpClientSimpleInMemoryProducerTest {

  private static final String META_DATA_URL = SVC_URL + "$metadata";
  private static final String FEED_URL = SVC_URL + ENTITY_SET_NAME;

  @Test
  public void testServiceUrl() throws Exception {
    ContentExchange exchange = sendRequest(SVC_URL);
    exchange.waitForDone();
    verifyResponseIsReturned(exchange);
  }

  @Test
  public void testMetaDataUrl() throws Exception {
    ContentExchange exchange = sendRequest(META_DATA_URL);
    exchange.waitForDone();
    verifyResponseIsReturned(exchange);
  }

  @Test
  public void testFeedUrl() throws Exception {
    ContentExchange exchange = sendRequest(FEED_URL);
    exchange.waitForDone();
    verifyResponseIsReturned(exchange);
  }

  private void verifyResponseIsReturned(ContentExchange exchange) throws InterruptedException, UnsupportedEncodingException {
    Assert.assertEquals(HttpExchange.STATUS_COMPLETED, exchange.getStatus());
    Assert.assertEquals(HttpStatus.OK_200, exchange.getResponseStatus());
    Assert.assertTrue(exchange.getResponseContent().length() > 0);
  }
}
