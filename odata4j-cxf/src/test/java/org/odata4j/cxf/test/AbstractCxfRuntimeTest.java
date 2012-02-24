package org.odata4j.cxf.test;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.cxf.consumer.ODataCxfConsumer;
import org.odata4j.producer.server.ODataServer;

public abstract class AbstractCxfRuntimeTest extends AbstractTest {

  private URI baseUri = URI.create("http://localhost:8810/test/test.svc/");

  private ODataServer server;
  private ODataConsumer consumer;

  protected ODataServer getServer() {
    return this.server;
  }

  protected URI getBaseUri() {
    return this.baseUri;
  }

  protected ODataConsumer getODataConsumer() {
    return this.consumer;
  }

  public AbstractCxfRuntimeTest() {
    this.server = new CxfTestServer(this.baseUri);
    this.consumer = ODataCxfConsumer.create(this.baseUri.toString());

    //    this.server.setODataApplication(TestApplication.class);

    this.logger.info("******************************************************************");
    this.logger.info("Activated Server Type = CXF");
    this.logger.info("******************************************************************");
  }

  @Before
  public void setup() {
    this.server.start();
  }

  @After
  public void teardown() {
    try {

    } finally {
      this.server.stop();
    }
  }
}
