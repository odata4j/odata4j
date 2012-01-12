package org.odata4j.producer.jpa.northwind.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperties;
import org.odata4j.fit.support.ConsumerSupport;

public abstract class AbstractCompositeKeyEntityTest extends AbstractJPAProducerTest implements ConsumerSupport {

  protected static final String endpointUri = "http://localhost:8810/northwind/Northwind.svc/";

  @Before
  public void setUpClass() throws Exception {
    setUp(20);
  }

  @Test
  public void updateCompositeKeyEntity() {
    ODataConsumer consumer = this.create(endpointUri, null, null);

    OEntity orderDetails = consumer.getEntities("Order_Details").top(1).execute().first();
    Assert.assertNotNull(orderDetails);

    Assert.assertNotSame(123, orderDetails.getProperty("Quantity", short.class).getValue());
    consumer.updateEntity(orderDetails).properties(OProperties.int16("Quantity", (short) 123)).execute();

    OEntity orderDetailsNew = consumer.getEntity("Order_Details", orderDetails.getEntityKey()).execute();
    Assert.assertEquals((short) 123, (short) orderDetailsNew.getProperty("Quantity", short.class).getValue());
  }

  @Test
  public void mergeCompositeKeyEntity() {
    ODataConsumer consumer = this.create(endpointUri, null, null);

    OEntity orderDetails = consumer.getEntities("Order_Details").top(1).execute().first();
    Assert.assertNotNull(orderDetails);

    Assert.assertNotSame(123, orderDetails.getProperty("Quantity", short.class).getValue());
    consumer.mergeEntity("Order_Details", orderDetails.getEntityKey()).properties(OProperties.int16("Quantity", (short) 123)).execute();

    OEntity orderDetailsNew = consumer.getEntity("Order_Details", orderDetails.getEntityKey()).execute();
    Assert.assertEquals((short) 123, (short) orderDetailsNew.getProperty("Quantity", short.class).getValue());
  }

  @Test
  public void createCompositeKeyEntityUsingLinks() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.create(endpointUri, null, null);

    OEntity product = consumer
        .createEntity("Products")
        .properties(OProperties.string("ProductName", "Product" + now))
        .execute();

    Assert.assertNotNull(product);

    OEntity order = consumer
        .createEntity("Orders")
        .execute();

    Assert.assertNotNull(order);

    Short quantity = 1;
    OEntity orderDetails = consumer
        .createEntity("Order_Details")
        .link("Order", order)
        .link("Product", product)
        .properties(OProperties.decimal("UnitPrice", 1.0))
        .properties(OProperties.int16("Quantity", quantity))
        .properties(OProperties.decimal("Discount", 1.0))
        .execute();

    Assert.assertNotNull(orderDetails);
    Assert.assertEquals(
        OEntityKey.create("OrderID", order.getEntityKey().asSingleValue(), "ProductID", product.getEntityKey().asSingleValue()),
        orderDetails.getEntityKey());
  }

  @Test
  public void createCompositeKeyEntityUsingProperties() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.create(endpointUri, null, null);

    OEntity product = consumer
        .createEntity("Products")
        .properties(OProperties.string("ProductName", "Product" + now))
        .execute();

    Assert.assertNotNull(product);

    OEntity order = consumer
        .createEntity("Orders")
        .execute();

    Assert.assertNotNull(order);

    Short quantity = 1;
    OEntity orderDetails = consumer
        .createEntity("Order_Details")
        .properties(OProperties.decimal("UnitPrice", 1.0))
        .properties(OProperties.int16("Quantity", quantity))
        .properties(OProperties.decimal("Discount", 1.0))
        .properties(OProperties.simple("OrderID", order.getEntityKey().asSingleValue()))
        .properties(OProperties.simple("ProductID", product.getEntityKey().asSingleValue()))
        .execute();

    Assert.assertNotNull(orderDetails);
    Assert.assertEquals(
        OEntityKey.create("OrderID", order.getEntityKey().asSingleValue(), "ProductID", product.getEntityKey().asSingleValue()),
        orderDetails.getEntityKey());
  }

  @Test
  public void deleteCompositeKeyEntityUsingLinks() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.create(endpointUri, null, null);

    OEntity product = consumer
        .createEntity("Products")
        .properties(OProperties.string("ProductName", "Product" + now))
        .execute();

    Assert.assertNotNull(product);

    OEntity order = consumer
        .createEntity("Orders")
        .execute();

    Assert.assertNotNull(order);

    Short quantity = 1;
    OEntity orderDetails = consumer
        .createEntity("Order_Details")
        .link("Order", order)
        .link("Product", product)
        .properties(OProperties.decimal("UnitPrice", 1.0))
        .properties(OProperties.int16("Quantity", quantity))
        .properties(OProperties.decimal("Discount", 1.0))
        .execute();

    Assert.assertNotNull(orderDetails);

    int beforeCount = consumer.getEntities("Order_Details").execute().count();

    consumer.deleteEntity(orderDetails).execute();

    Assert.assertTrue(beforeCount > consumer.getEntities("Order_Details").execute().count() ? true : false);
  }

}
