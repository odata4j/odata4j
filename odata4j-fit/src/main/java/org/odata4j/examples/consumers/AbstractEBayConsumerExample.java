package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ConsumerExample;
import org.odata4j.examples.ODataEndpoints;

public abstract class AbstractEBayConsumerExample extends BaseExample implements ConsumerExample {

  @Override
  public void run(String... args) {

    ODataConsumer c = this.create(ODataEndpoints.EBAY);

    OEntity firstCategory = c.getEntities("Categories").top(1).execute().first();
    reportEntities(firstCategory.getProperty("Name").getValue().toString(),
        c.getEntities(firstCategory.getLink("Items", ORelatedEntitiesLink.class))
            .execute()
            .take(5));

  }

}
