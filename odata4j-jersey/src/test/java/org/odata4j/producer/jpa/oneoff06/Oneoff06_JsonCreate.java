package org.odata4j.producer.jpa.oneoff06;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.producer.jpa.oneoff.OneoffTestBase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class Oneoff06_JsonCreate extends OneoffTestBase {
  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(Oneoff06_JsonCreate.class, 20);
  }

  @Test
  public void createCountry() {
    ODataConsumer c = ODataJerseyConsumer.create(endpointUri);
    Assert.assertEquals(0, c.getEntities("Country").execute().count());
    Client client = Client.create();
    ClientResponse response = client.resource(endpointUri)
        .path("Country")
        .accept("application/json") // will fail without this line
        .type("application/json;charset=utf-8")
        .post(ClientResponse.class, "{ \"name\":\"Ireland\"}");
    System.out.println(response.getEntity(String.class));
    Assert.assertEquals(1, c.getEntities("Country").execute().count());
    Assert.assertEquals(201, response.getStatus());
    Assert.assertEquals("application/json;charset=utf-8", response.getType().toString());
  }

}
