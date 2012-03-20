package org.odata4j.examples.consumer;

import static org.odata4j.examples.JaxRsImplementation.JERSEY;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataConsumerFactory;

public class JsonGrabbingConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    JsonGrabbingConsumerExample example = new JsonGrabbingConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {

    String serviceUri = "http://services.odata.org/Northwind/Northwind.svc";
    ODataConsumer c = new ODataConsumerFactory(JERSEY).createODataConsumer(serviceUri, null, null);

    c.getEntity("Customers", "VICTE").execute();
  }

}
