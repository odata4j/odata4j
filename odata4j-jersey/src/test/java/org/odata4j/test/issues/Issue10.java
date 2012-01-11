package org.odata4j.test.issues;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.jersey.producer.JerseyProducerUtil;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;

public class Issue10 {

  @Test
  public void issue10() {

    String endpointUri = "http://localhost:8810/Issue10.svc/";

    final QueryInfo[] lastQuery = new QueryInfo[1];
    InMemoryProducer producer = new InMemoryProducer("Issue10") {
      public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
        lastQuery[0] = queryInfo;
        return super.getEntities(entitySetName, queryInfo);
      }
    };
    producer.register(String.class, String.class, "Entity", new Func<Iterable<String>>() {
      public Iterable<String> apply() {
        return Enumerable.create("one", "two", "three");
      }
    }, Funcs.identity(String.class));

    DefaultODataProducerProvider.setInstance(producer);
    ODataServer server = JerseyProducerUtil.startODataServer(endpointUri);

    ODataConsumer c = ODataJerseyConsumer.create(endpointUri);

    lastQuery[0] = null;
    c.getEntities("Entity").execute().toList();
    Assert.assertNotNull(lastQuery[0]);
    Assert.assertEquals(0, lastQuery[0].customOptions.size());

    lastQuery[0] = null;
    c.getEntities("Entity").custom("x", "y").execute().toList();
    Assert.assertNotNull(lastQuery[0]);
    Assert.assertEquals(1, lastQuery[0].customOptions.size());
    Assert.assertEquals("x", lastQuery[0].customOptions.keySet().iterator().next());
    Assert.assertEquals("y", lastQuery[0].customOptions.get("x"));

    lastQuery[0] = null;
    c.getEntities("Entity").custom("x", "y").custom("a", "b").execute().toList();
    Assert.assertNotNull(lastQuery[0]);
    Assert.assertEquals(2, lastQuery[0].customOptions.size());
    Assert.assertTrue(lastQuery[0].customOptions.keySet().contains("a"));
    Assert.assertTrue(lastQuery[0].customOptions.keySet().contains("x"));
    Assert.assertEquals("y", lastQuery[0].customOptions.get("x"));
    Assert.assertEquals("b", lastQuery[0].customOptions.get("a"));

    lastQuery[0] = null;
    c.getEntities("Entity").custom("x", "y").top(10).execute().toList();
    Assert.assertNotNull(lastQuery[0]);
    Assert.assertEquals(1, lastQuery[0].customOptions.size());

    server.stop();

  }

}
