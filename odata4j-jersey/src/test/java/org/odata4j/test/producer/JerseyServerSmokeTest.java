package org.odata4j.test.producer;

import org.odata4j.fit.producer.AbstractServerSmokeTest;
import org.odata4j.jersey.server.JerseyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;

public class JerseyServerSmokeTest extends AbstractServerSmokeTest {

  @Override
  protected void createServer() {
    server = new JerseyServer(SVC_URL, DefaultODataApplication.class, RootApplication.class);
  }
}
