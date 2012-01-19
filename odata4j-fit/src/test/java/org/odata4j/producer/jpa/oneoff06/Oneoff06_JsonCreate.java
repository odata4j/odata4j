package org.odata4j.producer.jpa.oneoff06;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.producer.jpa.oneoff.AbstractOneoffTestBase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class Oneoff06_JsonCreate extends AbstractOneoffTestBase {

  public Oneoff06_JsonCreate(RuntimeFacadeType type) {
    super(type);
  }

  /*
   * TODO currently not decoupled from Jersey -> introduce response facade in dependency of CXF options
   */
  private ClientResponse response;

  @Test
  @Ignore
  public void createCountry() {
    ODataConsumer c = this.rtFacade.create(endpointUri, null, null);
    Assert.assertEquals(0, c.getEntities("Country").execute().count());

    this.requestPost();

    System.out.println(this.getResponseEntity());
    Assert.assertEquals(1, c.getEntities("Country").execute().count());
    Assert.assertEquals(201, this.getResponseStatus());
    Assert.assertEquals("application/json;charset=utf-8", this.getResponseType());
  }

  private String getResponseEntity() {
    return this.response.getEntity(String.class);
  }

  private int getResponseStatus() {
    return this.response.getStatus();
  }

  private String getResponseType() {
    return this.response.getType().toString();
  }

  private void requestPost() {
    Client client = Client.create();
    this.response = client.resource(endpointUri)
        .path("Country")
        .accept("application/json") // will fail without this line
        .type("application/json;charset=utf-8")
        .post(ClientResponse.class, "{ \"name\":\"Ireland\"}");
  }

}
