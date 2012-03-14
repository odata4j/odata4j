package org.odata4j.test.server;

import org.junit.After;
import org.junit.Before;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.AbstractRuntimeTest;

/**
 * Base test class that:
 * <ol><li>starts an ODataServer,</li>
 * <li>starts a client,</li>
 * <li>and creates a test scenario</li></ol>
 */
public abstract class AbstractServerTest extends AbstractRuntimeTest {

  protected static final String SVC_URL = "http://localhost:8888/test.svc/";

  protected ODataServer server;

  public AbstractServerTest(RuntimeFacadeType type) {
    super(type);
  }

  @Before
  public void setup() throws Exception {
    startODataServer();
    startClient();
    createTestScenario();
  }

  @After
  public void teardown() throws Exception {
    stopClient();
    stopODataServer();
  }

  protected void startODataServer() throws Exception {
    server = rtFacade.startODataServer(SVC_URL);
  }

  protected abstract void startClient() throws Exception;

  protected abstract void createTestScenario();

  protected abstract void stopClient() throws Exception;

  protected void stopODataServer() throws Exception {
    server.stop();
  }
}
