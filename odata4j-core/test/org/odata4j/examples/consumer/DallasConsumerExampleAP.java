package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class DallasConsumerExampleAP extends BaseExample {

    public static void main(String... args) {

        String[] dallasCreds = args.length>0?args:System.getenv("DALLAS").split(":");
        String accountKey = dallasCreds[0];
        String uniqueUserId = dallasCreds[1];
        
        ODataConsumer c = ODataConsumers.dallas(ODataEndpoints.DALLAS_AP,accountKey,uniqueUserId);
        
        // first hundred categories
        reportEntities(c,"Categories",100);
        
        // stories by category: first two tech stories
        int topTechCategoryId = 31992;
        String FULL_STORIES = "2";
        reportEntities("Tech",c.getEntities("Categories/"+topTechCategoryId)
                .custom("ContentOption",FULL_STORIES)
                //.custom("NumItems", "2")  // doesn't appear to work
                .execute()
                .take(2)  // limit on the client-side
                );
        
        // stories by keyword: first story for "obama"
        reportEntities("Search", c.getEntities("Search")
                .custom("SearchTerms","obama")
                .execute()
                .take(1));
      

    }



}
