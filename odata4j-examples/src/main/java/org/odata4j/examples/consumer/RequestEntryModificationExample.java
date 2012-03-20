package org.odata4j.examples.consumer;

import static org.odata4j.examples.JaxRsImplementation.JERSEY;

import java.util.Date;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OProperties;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataConsumerFactory;

public class RequestEntryModificationExample extends AbstractExample {

  public static void main(String[] args) {
    RequestEntryModificationExample example = new RequestEntryModificationExample();
    example.run(args);
  }

  private void run(String[] args) {
    ODataConsumer.dump.all(true);

    // create a consumer with additional behavior
    String serviceUri = "http://services.odata.org/Northwind/Northwind.svc";
    ODataConsumer consumer = new ODataConsumerFactory(JERSEY).createODataConsumer(serviceUri, null, null);

    consumer.createEntity("Categories")
        .properties(OProperties.string("CategoryName", "Category " + new Date()))
        .execute();
  }
}
