package org.odata4j.test.consumer;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.jersey.consumer.ClientFactory;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

public class ClientFactoryTest {

  @Test
  public void testDefaultClientFactory() {
    ODataConsumer consumer = ODataJerseyConsumer.create("");
    Assert.assertNotNull(consumer);
  }

  @Test
  public void testCustomClientFactory() {
    ClientFactoryStub factory = new ClientFactoryStub();
    ODataConsumer consumer = ODataJerseyConsumer.newBuilder("").setClientFactory(factory).build();
    Assert.assertNotNull(consumer);
    Assert.assertTrue(factory.isCalled);
  }
}

class ClientFactoryStub implements ClientFactory {
  boolean isCalled = false;

  @Override
  public Client createClient(ClientConfig clientConfig) {
    isCalled = true;
    return null;
  }
}
