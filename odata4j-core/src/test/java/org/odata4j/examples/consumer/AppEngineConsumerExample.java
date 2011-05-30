package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class AppEngineConsumerExample extends BaseExample {

  public static void main(String... args) {

    ODataConsumer c = ODataConsumer.create(ODataEndpoints.ODATA4JSAMPLE_APPSPOT);
    //ODataConsumer.create("http://localhost:8888/datastore.svc");

    String newCategoryName = "NewCategory" + System.currentTimeMillis();

    report("Create a new category");
    OEntity newCategory = c.createEntity("Category")
        .properties(OProperties.string("categoryName", newCategoryName))
        .properties(OProperties.int32("AdditionalProperty", 500)) // appengine datastore entities are open types, add a new property
        .execute();
    reportEntity(newCategoryName, newCategory);
    reportEntities(c, "Category", 100);

    report("Update the new category");
    c.updateEntity(newCategory)
        .properties(OProperties.string("description", "Updated"))
        .execute();
    reportEntities(c, "Category", 100);

    report("Merge the new category");
    c.mergeEntity("Category", newCategory.getProperty("id"))
        .properties(OProperties.string("description", "Merged"))
        .execute();
    reportEntities(c, "Category", 100);

    report("Delete the new category");
    c.deleteEntity("Category", newCategory.getEntityKey()).execute();
    reportEntities(c, "Category", 100);

    reportEntity("Last category by category name (excluding seafood): ",
        c.getEntities("Category")
        .filter("categoryName ne 'Seafood'")
        .orderBy("categoryName desc")
        .top(1)
        .execute().first());

    reportEntity("\nNon-discontinued product with reorderLevel > 25 (two filter predicates): ",
        c.getEntities("Product")
        .filter("reorderLevel gt 25 and discontinued eq false")
        .top(1)
        .execute().first());

  }

}
