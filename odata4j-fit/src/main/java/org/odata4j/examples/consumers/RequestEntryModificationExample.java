package org.odata4j.examples.consumers;

import java.util.Date;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OProperties;
import org.odata4j.examples.AbstractExample;

public class RequestEntryModificationExample extends AbstractExample {

  public static void main(String[] args) {
    RequestEntryModificationExample example = new RequestEntryModificationExample();
    example.run(args);
  }

  private void run(String[] args) {
    ODataConsumer.dump.all(true);

    // create a consumer with additional behavior
    String serviceUri = "http://services.odata.org/Northwind/Northwind.svc";
    ODataConsumer consumer = this.rtFacde.create(serviceUri, null, null);

    consumer.createEntity("Categories")
        .properties(OProperties.string("CategoryName", "Category " + new Date()))
        .execute();
  }
}
