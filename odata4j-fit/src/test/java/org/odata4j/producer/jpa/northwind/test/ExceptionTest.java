package org.odata4j.producer.jpa.northwind.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;

public class ExceptionTest extends JPAProducerTest {

  public ExceptionTest(RuntimeFacadeType type) {
    super(type);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    super.setUp(20);
  }

  @Test
  public void test404NoEntityType() {
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);
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
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);
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

    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);
    consumer.getEntity("Customers", 1).execute();
  }
}
