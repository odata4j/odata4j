package org.odata4j.examples.jersey.producer;

import org.odata4j.examples.producer.AbstractJPAProducerExample;
import org.odata4j.producer.server.ODataServer;

public class JPAProducerExample extends AbstractJPAProducerExample {

  public static void main(String[] args) {
    JPAProducerExample example = new JPAProducerExample();
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
