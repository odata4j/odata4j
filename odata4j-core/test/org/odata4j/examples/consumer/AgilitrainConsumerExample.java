package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class AgilitrainConsumerExample extends BaseExample {

    
    public static void main(String... args) {
        
        ODataConsumer c = ODataConsumer.create(ODataEndpoints.AGILITRAIN);
        
        OEntity event = c.getEntity("Events", 225).execute();
        ORelatedEntityLink link = event.getLink("Workshop", ORelatedEntityLink.class);
        OEntity entity = c.getEntity(link).execute();
        reportEntity("Workshop", entity);

    }

}
