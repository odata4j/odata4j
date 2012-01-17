package org.odata4j.test.producer;

import org.eclipse.jetty.client.ContentExchange;
import org.junit.Test;

public abstract class AbstractServerFilteringTest extends AbstractODataServerHttpClientSimpleInMemoryProducerTest {

  @Test
  public void testRequestFilter() throws Exception {
    ContentExchange exchange = sendRequest(SVC_URL);
    exchange.waitForDone();
    verifyRequestFilterIsCalled();
  }

  protected abstract void verifyRequestFilterIsCalled();
}
