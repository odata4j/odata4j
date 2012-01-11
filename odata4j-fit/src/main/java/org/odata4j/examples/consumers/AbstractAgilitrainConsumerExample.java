package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ConsumerSupport;
import org.odata4j.examples.ODataEndpoints;
import org.odata4j.examples.RunSupport;

public abstract class AbstractAgilitrainConsumerExample extends AbstractExample implements ConsumerSupport, RunSupport {

  @Override
  public void run(String[] args) {
    ODataConsumer c = this.create(ODataEndpoints.AGILITRAIN, null);

    OEntity event = c.getEntity("Events", 225).execute();
    ORelatedEntityLink link = event.getLink("Workshop", ORelatedEntityLink.class);
    OEntity entity = c.getEntity(link).execute();
    reportEntity("Workshop", entity);
  }
}
