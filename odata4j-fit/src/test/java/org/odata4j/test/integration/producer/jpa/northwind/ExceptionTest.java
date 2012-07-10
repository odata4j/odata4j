package org.odata4j.test.integration.producer.jpa.northwind;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataServerException;
import org.odata4j.core.OEntityIds;

public class ExceptionTest extends NorthwindJpaProducerTest {

  protected ODataConsumer consumer;

  public ExceptionTest(RuntimeFacadeType type) {
    super(type);
  }

  @Before
  public void setUp() {
    super.setUp(20);
    consumer = this.rtFacade.createODataConsumer(endpointUri, null, null);
  }

  @Test
  public void noEntityType() throws Exception {
    try {
      consumer.getEntities("UnknownEntity").execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
      assertThat(e.getMessage(), containsString("UnknownEntity"));
    }
  }

  @Test
  public void noEntity() throws Exception {
    try {
      consumer.getEntity("Customers", "NOUSER").execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
      assertThat(e.getCode(), containsString("NotFound"));
      assertThat(e.getMessage(), containsString("NOUSER"));
    }
  }

  @Test
  public void invalidKey() throws Exception {
    try {
      consumer.getEntity("Customers", 1).execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.BAD_REQUEST.getStatusCode()));
    }

    try {
      consumer.getEntity("Employees", "WrongKey").execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.BAD_REQUEST.getStatusCode()));
    }
  }

  @Test
  public void noNavigation() throws Exception {
    try {
      consumer.getEntity("Customers", "QUEEN").nav("NoNavigation").execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void noLinks() throws Exception {
    try {
      consumer.getLinks(OEntityIds.create("Customers", "QUEEN"), "NoNavigation").execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void noFunction() throws Exception {
    try {
      consumer.callFunction("NoFunction").execute();
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
      assertThat(e.getMessage(), containsString("NoFunction"));
    }
  }

  @Test
  public void deleteNotExistingEntity() throws Exception {
    try {
      consumer.deleteEntity("Customers", "NOUSER").execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getStatus().getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
      assertThat(e.getMessage(), containsString("NOUSER"));
    }
  }

  @Test
  public void deleteLink() throws Exception {
    try {
      consumer.deleteLink(OEntityIds.create("Customers", "CENTC"), "Orders", 10259).execute();
      fail("Expected exception missing");
    } catch (ODataServerException e) {
      assertThat(e.getCode(), containsString("NotImplemented"));
    }
  }
}
