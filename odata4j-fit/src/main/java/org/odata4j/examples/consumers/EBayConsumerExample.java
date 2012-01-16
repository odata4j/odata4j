package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataEndpoints;

public class EBayConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    EBayConsumerExample example = new EBayConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {

    ODataConsumer c = this.runtime.create(ODataEndpoints.EBAY);

    OEntity firstCategory = c.getEntities("Categories").top(1).execute().first();
    reportEntities(firstCategory.getProperty("Name").getValue().toString(),
        c.getEntities(firstCategory.getLink("Items", ORelatedEntitiesLink.class))
            .execute()
            .take(5));

  }

}
