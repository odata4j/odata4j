package org.odata4j.test.producer;

import org.odata4j.cxf.producer.server.CxfJettyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;

public class CxfServerSmokeTest extends AbstractServerSmokeTest {

  @Override
  protected void createServer() {
    server = new CxfJettyServer(SVC_URL, DefaultODataApplication.class, RootApplication.class);
  }
}
