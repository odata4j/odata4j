package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.BaseCredentialsExample;
import org.odata4j.examples.ConsumerExample;
import org.odata4j.examples.ODataEndpoints;

public abstract class AbstractDallasConsumerExampleAP extends BaseCredentialsExample implements ConsumerExample {

  @Override
  public void run(String... args) {

    String[] dallasCreds = args.length > 0 ? args : System.getenv("DALLAS").split(":");
    this.setLoginPassword(dallasCreds[0]);
    this.setLoginName(dallasCreds[1]);    

    ODataConsumer c = this.create(ODataEndpoints.DALLAS_CTP3_AP);

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
