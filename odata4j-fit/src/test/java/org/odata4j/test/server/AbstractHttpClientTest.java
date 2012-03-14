package org.odata4j.test.server;

import java.io.IOException;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

/**
 * Base test class that uses a Jetty HttpClient as client.
 */
public abstract class AbstractHttpClientTest extends AbstractServerTest {

  protected HttpClient client;

  public AbstractHttpClientTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void startClient() throws Exception {
    client = new HttpClient();
    client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    client.start();
  }

  @Override
  protected void stopClient() throws Exception {
    client.stop();
  }

  protected ContentExchange sendRequest(String url) throws IOException {
    ContentExchange exchange = new ContentExchange(true);
    exchange.setURL(url);
    client.send(exchange);
    return exchange;
  }
}
