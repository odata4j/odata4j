package org.odata4j.producer.jpa.oneoff06;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.fit.support.ConsumerSupport;
import org.odata4j.producer.jpa.oneoff.AbstractOneoffTestBase;

public abstract class AbstractOneoff06JsonCreate extends AbstractOneoffTestBase implements ConsumerSupport {

  @Test
  @Ignore
  public void createCountry() {
    ODataConsumer c = this.create(endpointUri, null, null);
    Assert.assertEquals(0, c.getEntities("Country").execute().count());

    this.requestPost();

    System.out.println(this.getResponseEntity());
    Assert.assertEquals(1, c.getEntities("Country").execute().count());
    Assert.assertEquals(201, this.getResponseStatus());
    Assert.assertEquals("application/json;charset=utf-8", this.getResponseType());
  }

  protected abstract void requestPost();

  protected abstract String getResponseEntity();

  protected abstract int getResponseStatus();

  protected abstract String getResponseType();

}
