package org.odata4j.jersey.examples.producer;

import org.odata4j.examples.producer.AbstractInMemoryProducerExample;
import org.odata4j.producer.server.ODataServer;

public class InMemoryProducerExample extends AbstractInMemoryProducerExample {

  public static void main(String[] args) {
    InMemoryProducerExample example = new InMemoryProducerExample();
    example.run(args);
  }

  @Override
  public void hostODataServer(String baseUri) {
    JerseyProducerUtil.hostODataServer(baseUri);

  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return JerseyProducerUtil.startODataServer(baseUri);
  }
}
