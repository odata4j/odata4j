package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ConsumerExample;
import org.odata4j.examples.ODataEndpoints;

public abstract class AbstractAgilitrainConsumerExample extends BaseExample implements ConsumerExample {
  
  @Override
  public void run(String... args) {
    ODataConsumer c = this.create(ODataEndpoints.AGILITRAIN);

    OEntity event = c.getEntity("Events", 225).execute();
    ORelatedEntityLink link = event.getLink("Workshop", ORelatedEntityLink.class);
    OEntity entity = c.getEntity(link).execute();
    reportEntity("Workshop", entity);
  }
}
