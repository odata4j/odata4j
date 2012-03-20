package org.odata4j.examples.consumer;

import static org.odata4j.examples.JaxRsImplementation.JERSEY;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataConsumerFactory;

public class AgilitrainConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    AgilitrainConsumerExample example = new AgilitrainConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {
    ODataConsumer c = new ODataConsumerFactory(JERSEY).createODataConsumer(ODataEndpoints.AGILITRAIN, null, null);

    OEntity event = c.getEntity("Events", 225).execute();
    ORelatedEntityLink link = event.getLink("Workshop", ORelatedEntityLink.class);
    OEntity entity = c.getEntity(link).execute();
    reportEntity("Workshop", entity);
  }
}
