package org.odata4j.producer.jpa.northwind.test;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;

public class ExceptionTest extends JPAProducerTestBase {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(20);
  }

  @Before
  public void setUp() {}

  @After
  public void tearDown() {}

  @Test
  public void test404NoEntityType() {
    ODataConsumer consumer = ODataConsumer.create(endpointUri);
    OEntity customer = null;
    try {
      customer = consumer.getEntity("UnknownEntity", 1).execute();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertNull(customer);
  }

  @Test
  public void test404NoEntity() {
    ODataConsumer consumer = ODataConsumer.create(endpointUri);
    OEntity customer = null;
    try {
      customer = consumer.getEntity("Customers", "NOUSER").execute();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertNull(customer);
  }

  @Test
  public void test500InvalidKey() {

    thrown.expect(RuntimeException.class);
    thrown.expectMessage(JUnitMatchers.containsString("found 500"));

    ODataConsumer consumer = ODataConsumer.create(endpointUri);
    consumer.getEntity("Customers", 1).execute();
  }
}
