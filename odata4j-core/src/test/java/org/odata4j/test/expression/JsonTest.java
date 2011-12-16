package org.odata4j.test.expression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.core4j.Funcs;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.Guid;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.client.Client;

public class JsonTest {

  @Test
  public void testJson() {

    String uri = "http://localhost:18890/";

    InMemoryProducer producer = new InMemoryProducer("JsonTest");
    DefaultODataProducerProvider.setInstance(producer);

    ODataServer server = ProducerUtil.startODataServer(uri);

    try {
      ODataConsumer c = ODataConsumer.create(uri);
      Assert.assertEquals(0, c.getEntitySets().count());

      List<PojoWithAllTypes> pojos = new ArrayList<PojoWithAllTypes>();
      producer.register(PojoWithAllTypes.class, Integer.TYPE, "Pojo", Funcs.constant((Iterable<PojoWithAllTypes>) pojos), "Int32");

      pojos.add(new PojoWithAllTypes(new byte[] { 0x01, 0x02, 0x03 }, true, (byte) 0x05, new LocalDateTime(), new BigDecimal("123.456"), 123.456,
          Guid.randomGuid(), (short) 123, 1, Long.MAX_VALUE, 123.456F, "John", new LocalTime(), new DateTime()
          ));

      Client httpClient = Client.create();
      String output = httpClient.resource(uri + "Pojo?$format=json").get(String.class);
      System.out.println(output);

    } finally {
      server.stop();
    }

  }
}
