package org.odata4j.examples.consumer;

import static org.odata4j.examples.ODataEndpoints.ODATA_TEST_SERVICE_READWRITE2;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.examples.BaseExample;

public class ODataTestServiceReadWriteExample extends BaseExample {

   
    public static void main(String[] args) {
       
        ODataConsumer c = ODataConsumer.create(ODATA_TEST_SERVICE_READWRITE2);
        
        
        reportMetadata(c.getMetadata());
        
//        for(OEntity product : c.getEntities("Products").execute()){
//            reportEntity("Product " + product.getProperty("ID"),product);
//        }

    }

}
