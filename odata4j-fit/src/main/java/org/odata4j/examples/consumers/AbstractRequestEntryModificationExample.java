package org.odata4j.examples.consumers;

import java.util.Date;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OProperties;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ConsumerSupport;
import org.odata4j.examples.RunSupport;

public abstract class AbstractRequestEntryModificationExample extends AbstractExample implements ConsumerSupport, RunSupport {

  @Override
  public void run(String[] args) {
    ODataConsumer.dump.all(true);

    // create a consumer with additional behavior
    String serviceUri = "http://services.odata.org/Northwind/Northwind.svc";
    ODataConsumer consumer = this.create(serviceUri);
    
    consumer.createEntity("Categories")
        .properties(OProperties.string("CategoryName", "Category " + new Date()))
        .execute();
  }
}
