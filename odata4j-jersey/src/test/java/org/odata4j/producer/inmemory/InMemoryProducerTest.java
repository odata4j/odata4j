package org.odata4j.producer.inmemory;

import java.util.List;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.junit.Test;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.QueryInfo;

public class InMemoryProducerTest {

  @Test
  public void inlineCountWithOneShotIterable() {
    InMemoryProducer producer = new InMemoryProducer("InMemoryProducerTest");
    final List<String> testData = Enumerable.create("one", "two", "three").toList();
    Func<Iterable<String>> getTestData = new Func<Iterable<String>>() {
      @Override
      public Iterable<String> apply() {
        // worst case - a one shot iterable
        return Enumerable.createFromIterator(Funcs.constant(testData.iterator()));
      }
    };
    producer.register(String.class, String.class, "TestData", getTestData, Funcs.identity(String.class));

    EntitiesResponse response = producer.getEntities("TestData", null);
    Assert.assertEquals(3, response.getEntities().size());
    Assert.assertNull(response.getInlineCount());

    QueryInfo queryInfo = new QueryInfo(InlineCount.ALLPAGES, null, null, null, null, null, null, null, null);
    response = producer.getEntities("TestData", queryInfo);
    Assert.assertEquals(3, response.getEntities().size());
    Assert.assertEquals(Integer.valueOf(3), response.getInlineCount());
  }

}
