package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class DallasConsumerExampleAP extends BaseExample {

  @SuppressWarnings("unused")
  public static void main(String... args) {

    String[] dallasCreds = args.length > 0 ? args : System.getenv("DALLAS").split(":");
    String accountKey = dallasCreds[0];
    String uniqueUserId = dallasCreds[1];

    ODataConsumer c = ODataConsumers.dallas(ODataEndpoints.DALLAS_CTP3_AP, accountKey, uniqueUserId);

    // all breaking news categories
    reportEntities(c, "GetBreakingNewsCategories", 1000);

    // stories by category: top 5 tech stories
    int topTechCategoryId = 31992;
    String mediaOptionNoPictures = "0";
    String mediaOptionPictures = "1";
    String contentOptionLinksOnly = "0";
    String contentOptionFullStoryContent = "2";
    reportEntities("Tech", c.getEntities("GetBreakingNewsContentByCategory")
        .custom("CategoryId", "" + topTechCategoryId)
        .custom("MediaOption", mediaOptionNoPictures)
        .custom("ContentOption", contentOptionLinksOnly)
        .custom("Count", "5")
        .execute());

    // stories by keyword: first story for "obama"
    reportEntities("Search", c.getEntities("SearchNewsByKeyword")
        .custom("MediaOption", mediaOptionNoPictures)
        .custom("SearchTerms", "'obama'")
        .execute()
        .take(1));

  }

}
