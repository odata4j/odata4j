package org.odata4j.fit.support;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.producer.server.ODataServer;

public class CxfRuntimeSupport implements RuntimeSupport {

  public void hostODataServer(String baseUri) {
    throw new RuntimeException("not implemented");
  }

  public ODataServer startODataServer(String baseUri) {
    throw new RuntimeException("not implemented");
  }

  public ODataConsumer create(String endpointUri) {
    throw new RuntimeException("not implemented");
  }
}
