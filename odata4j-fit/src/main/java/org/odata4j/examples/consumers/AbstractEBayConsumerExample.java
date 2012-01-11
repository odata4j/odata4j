package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ConsumerSupport;
import org.odata4j.examples.ODataEndpoints;
import org.odata4j.examples.RunSupport;

public abstract class AbstractEBayConsumerExample extends AbstractExample implements ConsumerSupport, RunSupport {

  @Override
  public void run(String[] args) {

    ODataConsumer c = this.create(ODataEndpoints.EBAY, null);

    OEntity firstCategory = c.getEntities("Categories").top(1).execute().first();
    reportEntities(firstCategory.getProperty("Name").getValue().toString(),
        c.getEntities(firstCategory.getLink("Items", ORelatedEntitiesLink.class))
            .execute()
            .take(5));

  }

}
