package org.odata4j.test.integration;

import org.odata4j.consumer.ODataConsumer;

/**
 * Base integration test class that uses an ODataConsumer as client.
 */
public abstract class AbstractODataConsumerTest extends AbstractIntegrationTest {

  /**
   * The ODataConsumer instance.
   */
  protected ODataConsumer consumer;

  public AbstractODataConsumerTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void startClient() throws Exception {
    consumer = rtFacade.createODataConsumer(BASE_URI, null, null);
  }

  @Override
  protected void stopClient() throws Exception {
    consumer = null;
  }
}
