package org.odata4j.test.producer;

import org.odata4j.cxf.producer.server.CxfJettyServer;
import org.odata4j.fit.producer.AbstractServerSmokeTest;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;


public class CxfServerSmokeTest extends AbstractServerSmokeTest {

  @Override
  protected ODataServer createServer() {
    return new CxfJettyServer(this.getBaseUri(), DefaultODataApplication.class, RootApplication.class);
  }

}
