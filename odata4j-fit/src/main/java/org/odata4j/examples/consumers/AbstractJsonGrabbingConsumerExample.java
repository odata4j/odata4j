package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ConsumerExample;

public abstract class AbstractJsonGrabbingConsumerExample extends BaseExample  implements ConsumerExample {

  @Override
  public void run(String... args) {

    String serviceUri = "http://services.odata.org/Northwind/Northwind.svc";
    ODataConsumer c = this.create(serviceUri);

    c.getEntity("Customers", "VICTE").execute();
  }

}
