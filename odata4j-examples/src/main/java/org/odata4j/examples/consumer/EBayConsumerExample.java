package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.examples.AbstractExample;

public class EBayConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    EBayConsumerExample example = new EBayConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {

    ODataConsumer c = ODataConsumers.create(ODataEndpoints.EBAY);

    OEntity firstCategory = c.getEntities("Categories").top(1).execute().first();
    reportEntities(firstCategory.getProperty("Name").getValue().toString(),
        c.getEntities(firstCategory.getLink("Items", ORelatedEntitiesLink.class))
            .execute()
            .take(5));
  }

}
