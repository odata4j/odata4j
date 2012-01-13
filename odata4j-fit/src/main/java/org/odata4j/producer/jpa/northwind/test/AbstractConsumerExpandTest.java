package org.odata4j.producer.jpa.northwind.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.fit.support.ConsumerSupport;

public abstract class AbstractConsumerExpandTest extends AbstractJPAProducerTest implements ConsumerSupport {

  @Before
  public void setUp() throws Exception {
    super.setUp(20);
  }

  @Test
  public void testConsumerExpandAndFilter() {
    ODataConsumer consumer = this.create(endpointUri, null, null);

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
