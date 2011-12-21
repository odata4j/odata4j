package org.odata4j.fit.util;

import org.eclipse.jetty.client.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.odata4j.producer.server.ODataServer;

public abstract class AbstractFitTest {

  protected abstract ODataServer createServer();
  protected abstract void createTestScenario();

  private HttpClient createClient() {
    HttpClient client = new HttpClient();
    client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    return client;
  }

  private ODataServer server;
  private HttpClient client;

  @Before
  public void setup() {
    try {
      this.server = this.createServer();
      this.createTestScenario();
      this.server.start();
      this.client = this.createClient();
      this.client.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void teardown() {
    try {
      this.client.stop();
      this.server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public ODataServer getServer() {
    return server;
  }

  public HttpClient getClient() {
    return client;
  }

}
