package org.odata4j.producer.jpa.northwind.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class ConsumerExpandTest extends JPAProducerTestBase {

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(20);
  }

  @Test
  public void testConsumerExpandAndFilter() {
    ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).build();

    // Northwind.svc/Order_Details?$expand=Order&$select=Order&$filter=Order/CustomerID%20eq%20'VINET'
    List<OEntity> orderDetails = consumer.getEntities("Order_Details")
        .expand("Order")
        .select("Order")
        .filter("Order/CustomerID eq 'VINET'")
        .execute().toList();

    for (OEntity orderDetail : orderDetails) {
      OEntity order = orderDetail.getLink("Order", ORelatedEntityLinkInline.class).getRelatedEntity();
      Assert.assertEquals("VINET", order.getProperty("CustomerID").getValue());
    }

  }

}
