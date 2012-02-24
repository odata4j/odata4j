package org.odata4j.test.producer;

import org.junit.After;
import org.junit.Before;
import org.odata4j.test.AbstractTest;

/**
 * Base test class that:
 * <ol><li>creates a server,</li>
 * <li>creates a test scenario,</li>
 * <li>starts the server,</li>
 * <li>creates a client,</li>
 * <li>and starts the client</li></ol>
 */
public abstract class AbstractFitTest extends AbstractTest {

  protected abstract void createServer() throws Exception;

  protected abstract void startServer() throws Exception;

  protected abstract void stopServer() throws Exception;

  protected abstract void createClient() throws Exception;

  protected abstract void startClient() throws Exception;

  protected abstract void stopClient() throws Exception;

  protected abstract void createTestScenario();

  @Before
  public void setup() throws Exception {
    createServer();
    createTestScenario();
    startServer();
    createClient();
    startClient();
  }

  @After
  public void teardown() throws Exception {
    stopClient();
    stopServer();
  }
}
