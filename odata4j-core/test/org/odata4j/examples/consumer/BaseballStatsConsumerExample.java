package org.odata4j.examples.consumer;


import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class BaseballStatsConsumerExample extends BaseExample {

    
    public static void main() {
        
        ODataConsumer c = ODataConsumer.create(ODataEndpoints.BASEBALL_STATS);
        ODataConsumer.DUMP_REQUEST_HEADERS = true;
        
        // print out the first record in each entity set exposed by this (massive) service
        for(String entitySet : c.getEntitySets()){
            reportEntities(entitySet,c.getEntities(entitySet).top(1).execute());
        }
        
    }
}
