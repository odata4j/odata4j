package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class EBayConsumerExample extends BaseExample {

    public static void main(String[] args) {
        
        ODataConsumer c = ODataConsumer.create(ODataEndpoints.EBAY);
       
        OEntity firstCategory = c.getEntities("Categories").top(1).execute().first();
        reportEntities(firstCategory.getProperty("Name").getValue().toString(),
                c.getEntities(firstCategory.getLink("Items",ORelatedEntitiesLink.class))
                    .execute()
                    .take(5));
        
         
    }

}
