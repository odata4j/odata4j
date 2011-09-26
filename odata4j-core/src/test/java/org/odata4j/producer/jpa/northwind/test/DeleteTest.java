package org.odata4j.producer.jpa.northwind.test;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OClientBehaviors;
import org.odata4j.core.OEntity;
import org.odata4j.core.OPredicates;

public class DeleteTest extends JPAProducerTestBase {

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(20);
  }

  @Test
  public void tunneledDeleteEntity() {
    ODataConsumer consumer = ODataConsumer.newBuilder(endpointUri).setClientBehaviors(OClientBehaviors.methodTunneling("PUT")).build();

    deleteEntityAndTest(consumer, "QUEEN");
  }

  @Test
  public void deleteEntity() {
    ODataConsumer consumer = ODataConsumer.create(endpointUri);

    deleteEntityAndTest(consumer, "ALFKI");
  }

  protected void deleteEntityAndTest(ODataConsumer consumer, String customerID) {

    OEntity customer = consumer.getEntity("Customers", customerID).execute();
    Assert.assertNotNull(customer);
    Assert.assertEquals(customerID, customer.getEntityKey().asSingleValue());

    Assert.assertNotNull(consumer.getEntities("Customers").execute().firstOrNull(OPredicates.entityPropertyValueEquals("CustomerID", customerID)));

    consumer.deleteEntity("Customers", customer.getEntityKey()).execute();

    Assert.assertNull(consumer.getEntities("Customers").execute().firstOrNull(OPredicates.entityPropertyValueEquals("CustomerID", customerID)));

  }

}
