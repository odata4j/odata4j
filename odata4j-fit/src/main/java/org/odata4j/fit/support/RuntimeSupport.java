package org.odata4j.fit.support;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.producer.server.ODataServer;

public interface RuntimeSupport {

  public void hostODataServer(String baseUri);

  public ODataServer startODataServer(String baseUri);

  public ODataConsumer create(String endpointUri);

}
