package org.odata4j.producer.jpa.northwind.test;

import java.util.Set;

import junit.framework.Assert;

import org.core4j.Func1;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.examples.ODataEndpoints;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.jpa.northwind.test.InterceptLinkModificationCalls.LinksMethod;

public class LinksTest extends JPAProducerTestBase {

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(20, new Func1<ODataProducer, ODataProducer>() {
      public ODataProducer apply(final ODataProducer jpa) {
        interceptor = new InterceptLinkModificationCalls(jpa);
        return interceptor;
      }
    });
  }
  
  private static InterceptLinkModificationCalls interceptor;

  @Test
  public void linksClientApi() {
    //ODataConsumer.dump.all(true);
    ODataConsumer consumer = ODataConsumer.create(endpointUri);
    OEntity product1 = consumer.getEntity("Products", 1).execute();

    // get
    OEntityId category = consumer.getLinks(product1, "Category").execute().first();
    Assert.assertEquals(1, category.getEntityKey().asSingleValue());

    Set<OEntityId> orders = consumer.getLinks(product1, "Order_Details").execute().toSet();
    Assert.assertEquals(20, orders.size());

    // delete
    Assert.assertNull(interceptor.lastCall);
    consumer.deleteLink(product1, "Order_Details", "OrderID", 10285, "ProductID", 1).execute();
    Assert.assertEquals(LinksMethod.DELETE, interceptor.lastCall.methodCalled);

  }

  public static void main(String[] args) {
    ODataConsumer.dump.all(true);
    ODataConsumer consumer = ODataConsumer.create(ODataEndpoints.ODATA_TEST_SERVICE_READWRITE1);

    OEntityId product0 = OEntityIds.create("Products", 0);
    OEntityId category0 = OEntityIds.create("Category", 0);
    OEntityId category1 = OEntityIds.create("Category", 1);
    consumer.updateLink(product0, category1, "Category").execute();

    // consumer.updateLink(supplier1, products2, "Products", products0).execute();

    OEntityId supplier1 = OEntityIds.create("Suppliers", 1);

    //    consumer.deleteLink(supplier1, "Products", 0).execute();
    OEntityId products0 = OEntityIds.create("Products", 0);

    //    consumer.createLink(supplier1, "Products", products0).execute();
    //    
    //    for (OEntityId product : consumer.getLinks(supplier1, "Products").execute()) {
    //      System.out.println(product);
    //    }
  }

}
