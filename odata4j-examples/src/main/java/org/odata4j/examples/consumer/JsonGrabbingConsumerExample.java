package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataClientException;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.consumer.ODataServerException;
import org.odata4j.examples.AbstractExample;

public class JsonGrabbingConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    JsonGrabbingConsumerExample example = new JsonGrabbingConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {

    String serviceUri = "http://services.odata.org/Northwind/Northwind.svc";
    ODataConsumer c = ODataConsumers.create(serviceUri);

    try {
      c.getEntity("Customers", "VICTE").execute();
    } catch (ODataServerException e) {
      reportError(e);
    } catch (ODataClientException e) {
      report("Client error: " + e.getMessage());
    }
  }

}
