package org.odata4j.jersey.examples.producer;

import org.odata4j.examples.producer.AbstractXmlDataProducerExample;
import org.odata4j.producer.server.ODataServer;

/**
 * This example shows how to expose xml data as an atom feed.
 */
public class XmlDataProducerExample extends AbstractXmlDataProducerExample {

  public static void main(String[] args) {
    XmlDataProducerExample example = new XmlDataProducerExample();
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
