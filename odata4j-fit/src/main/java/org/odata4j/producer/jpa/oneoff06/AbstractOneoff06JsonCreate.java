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
    ODataConsumer c = this.create(endpointUri,null,null);
    Assert.assertEquals(0, c.getEntities("Country").execute().count());

    // TODO SKL interface extension required or re-think builder pattern
//    Client client = Client.create();
//    ClientResponse response = client.resource(endpointUri)
//        .path("Country")
//        .accept("application/json") // will fail without this line
//        .type("application/json;charset=utf-8")
//        .post(ClientResponse.class, "{ \"name\":\"Ireland\"}");

//    System.out.println(response.getEntity(String.class));
//    Assert.assertEquals(1, c.getEntities("Country").execute().count());
//    Assert.assertEquals(201, response.getStatus());
//    Assert.assertEquals("application/json;charset=utf-8", response.getType().toString());
  }

}
