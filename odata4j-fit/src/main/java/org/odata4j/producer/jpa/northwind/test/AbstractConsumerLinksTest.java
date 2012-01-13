package org.odata4j.producer.jpa.northwind.test;

import java.util.Set;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OFuncs;
import org.odata4j.core.OLink;
import org.odata4j.fit.support.ConsumerSupport;

public abstract class AbstractConsumerLinksTest extends AbstractJPAProducerTest implements ConsumerSupport {

  @Before
  public void setUpClass() throws Exception {
    super.setUp(20);
  }

  @Test
  public void linksReturnedToClient() {
    ODataConsumer consumer = this.create(endpointUri, null, null);

    OEntity order = consumer.getEntity("Orders", 10248).execute();
    Assert.assertEquals(3, order.getLinks().size());
    Set<String> linkTitles = Enumerable.create(order.getLinks()).select(OFuncs.title(OLink.class)).toSet();
    Assert.assertEquals(Enumerable.create("Customer", "Employee", "OrderDetails").toSet(), linkTitles);
  }

}
