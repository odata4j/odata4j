package org.odata4j.test;

import org.odata4j.producer.server.ODataServer;

public interface ProducerSupport {

  void hostODataServer(String baseUri);

  ODataServer startODataServer(String baseUri);
}
