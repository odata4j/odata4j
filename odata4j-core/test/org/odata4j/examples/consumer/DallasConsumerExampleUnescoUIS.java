package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.core.OEntity;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class DallasConsumerExampleUnescoUIS extends BaseExample {

    public static void main(String[] args) {

        String[] dallasCreds = System.getenv("DALLAS").split(":");
        String accountKey = dallasCreds[0];
        String uniqueUserId = dallasCreds[1];
        
        ODataConsumer c = ODataConsumers.dallas(ODataEndpoints.DALLAS_UNESCO_UIS,accountKey,uniqueUserId);
        
        // Public expenditure on education as % of GDP [XGDP_FSGOV]
        for(OEntity entity : c.getEntities("UNESCO/XGDP_FSGOV").execute()){
            report("Public expenditure on education as pct of GDP: %s %s, %.4f",
                    entity.getProperty("referenceArea").getValue(),
                    entity.getProperty("timePeriod").getValue(),
                    entity.getProperty("observationValue").getValue());
        }
        
        // Number of national feature films produced [C_F_220006]
        for(OEntity entity : c.getEntities("UNESCO/C_F_220006").execute()){
            report("Number of national feature films produced: %s %s, %.0f",
                    entity.getProperty("referenceArea").getValue(),
                    entity.getProperty("timePeriod").getValue(),
                    entity.getProperty("observationValue").getValue());
        }
        
       
       
  
    }



}
