package org.odata4j.test.issues;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;
import org.odata4j.core.OEntityKey;

public class Issue13 {

  @Test
  public void issue13() {

    String endpointUri = "http://localhost:8813/Issue13.svc/";

    final OEntityKey[] lastEntityKey = new OEntityKey[1];
    InMemoryProducer producer = new InMemoryProducer("Issue13") {
      @Override
      public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, QueryInfo queryInfo) {
        lastEntityKey[0] = entityKey;
        return super.getEntity(entitySetName, entityKey, queryInfo);
      }
    };
    producer.register(Long.class, Long.class, "Entity", new Func<Iterable<Long>>() {
      public Iterable<Long> apply() {
        return Enumerable.create(1L, 2L, 3L);
      }
    }, Funcs.identity(Long.class));

    ODataProducerProvider.setInstance(producer);
    JerseyServer server = ProducerUtil.startODataServer(endpointUri);

    ODataConsumer c = ODataConsumer.create(endpointUri);

    lastEntityKey[0] = null;
    Assert.assertNotNull(c.getEntity("Entity", 2L).execute());
    Assert.assertNotNull(lastEntityKey[0]);
    Assert.assertEquals(OEntityKey.create(2L), lastEntityKey[0]);

    server.stop();

  }

}
