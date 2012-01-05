package org.odata4j.fit.util;

import java.io.IOException;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

/**
 * Base test class that uses an ODataServer as server and a Jetty HttpClient as client.
 * <p>Method {@code createServer} needs to be implemented to create a concrete ODataServer
 * implementation.</p>
 */
public abstract class AbstractODataServerHttpClientTest extends AbstractODataServerTest {

  protected HttpClient client;

  @Override
  protected void createClient() throws Exception {
    client = new HttpClient();
    client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
  }

  @Override
  protected void startClient() throws Exception {
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
