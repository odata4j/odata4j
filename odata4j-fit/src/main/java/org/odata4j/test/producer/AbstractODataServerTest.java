package org.odata4j.test.producer;

import org.odata4j.producer.server.ODataServer;

/**
 * Base test class that uses an ODataServer as server.
 * <p>Method {@code createServer} needs to be implemented to create a concrete ODataServer
 * implementation.</p>
 */
public abstract class AbstractODataServerTest extends AbstractFitTest {

  protected static final String SVC_URL = "http://localhost:8888/test.svc/";

  protected ODataServer server;

  @Override
  protected void startServer() throws Exception {
    server.start();
  }

  @Override
  protected void stopServer() throws Exception {
    server.stop();
  }
}
