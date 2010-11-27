package org.odata4j.test.issues;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.NavPropertyResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;

public class Issue16 {

    @Test
    public void issue16(){
        
        String endpointUri = "http://localhost:8816/Issue16.svc/";
        
        final String[] actualNavProp = new String[1];
        InMemoryProducer producer = new InMemoryProducer("Issue16"){
          @Override
          public NavPropertyResponse getNavProperty(String entitySetName, Object entityKey, String navProp, QueryInfo queryInfo) {
              
              actualNavProp[0] = navProp;
            return Responses.navProperty(Enumerable.<OEntity>create().toList(), new EdmEntitySet("messageLog", null), null, null, null);
          } 
        };

        ODataProducerProvider.setInstance(producer);
        JerseyServer server = ProducerUtil.startODataServer(endpointUri);
        ODataConsumer c = ODataConsumer.create(endpointUri);
        c.getEntities("Message").nav(124L, "messageLog()").execute().count();
        Assert.assertNotNull(actualNavProp[0] );
        Assert.assertEquals("messageLog", actualNavProp[0]);
      
        server.stop();
        
    }
    
   
    
}
