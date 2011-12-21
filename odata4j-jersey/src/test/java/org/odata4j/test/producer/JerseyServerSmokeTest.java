package org.odata4j.test.producer;

import org.odata4j.fit.producer.AbstractServerSmokeTest;
import org.odata4j.jersey.server.JerseyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.container.filter.LoggingFilter;

public class JerseyServerSmokeTest extends AbstractServerSmokeTest {

  @Override
  protected ODataServer createServer() {
    return new JerseyServer(this.getBaseUri(), DefaultODataApplication.class, RootApplication.class)
        .addJerseyRequestFilter(LoggingFilter.class);
  }

}
