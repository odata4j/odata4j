package org.odata4j.producer.jpa.northwind.test;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class CreateWithLink2Test extends JPAProducerTestBase {

  protected static final String endpointUri = "http://localhost:8810/northwind/Northwind.svc/";

  @BeforeClass
  public static void setUpClass() throws Exception {

    setUpClass(20);
  }

  @Test
  public void passEntityRefFromFilter() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = ODataJerseyConsumer.create(endpointUri);

    OEntity customer = consumer
        .createEntity("Customers")
        .properties(OProperties.string("CustomerID", "ID" + now))
        .properties(OProperties.string("CompanyName", "Company" + now))
        .execute();
    Assert.assertNotNull(customer);

    String filterQuery = "CompanyName eq 'Company" + now + "'";
    OEntity customerRet = consumer.getEntities("Customers").top(1).filter(filterQuery).execute().first();

    Assert.assertNotNull(customerRet);

    OEntity order = consumer
        .createEntity("Orders")
        .properties(OProperties.string("ShipName", "Ship" + now))
        .link("Customer", customerRet)
        .execute();

    Assert.assertNotNull(order);

    ORelatedEntityLink link = order.getLink("Customer", ORelatedEntityLink.class);
    OEntity customerValid = consumer.getEntity(link).execute();

    Assert.assertNotNull(customerValid);
    Assert.assertEquals("Company" + now, customerValid.getProperty("CompanyName").getValue());

  }

}
