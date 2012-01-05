package org.odata4j.fit.producer;

import org.eclipse.jetty.client.ContentExchange;
import org.junit.Test;
import org.odata4j.fit.util.AbstractODataServerHttpClientSimpleInMemoryProducerTest;

public abstract class AbstractServerFilteringTest extends AbstractODataServerHttpClientSimpleInMemoryProducerTest {

  @Test
  public void testRequestFilter() throws Exception {
    ContentExchange exchange = sendRequest(SVC_URL);
    exchange.waitForDone();
    verifyRequestFilterIsCalled();
  }

  protected abstract void verifyRequestFilterIsCalled();
}
