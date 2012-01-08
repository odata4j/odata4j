package org.odata4j.producer.inmemory;

import java.util.List;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.junit.Test;
import org.odata4j.core.OAtomStreamEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.QueryInfo;

public class InMemoryProducerTest {
  private final QueryInfo NULL_QUERY = new QueryInfo(InlineCount.ALLPAGES, null, null, null, null, null, null, null, null);

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

    response = producer.getEntities("TestData", NULL_QUERY);
    Assert.assertEquals(3, response.getEntities().size());
    Assert.assertEquals(Integer.valueOf(3), response.getInlineCount());
  }

  @Test
  public void testStreamEntity() {
    final InMemoryProducer p = new InMemoryProducer("AAA");
    p.register(StreamEntity.class, "setName", new Func<Iterable<StreamEntity>>() {
      @Override
      public Iterable<StreamEntity> apply() {
        return Enumerable.create(new StreamEntity());
      }
    }, "Id");
    p.register(String.class, String.class, "ss", new Func<Iterable<String>>() {
      @Override
      public Iterable<String> apply() {
        return Enumerable.create("aaa");
      }
    }, Funcs.identity(String.class));

    final EdmEntitySet setName = p.getMetadata().findEdmEntitySet("setName");
    Assert.assertNotNull(setName);
    Assert.assertTrue(setName.getType().getHasStream());

    final EdmEntitySet ss = p.getMetadata().findEdmEntitySet("ss");
    Assert.assertNotNull(ss);
    Assert.assertFalse(ss.getType().getHasStream());
  }


  @Test
  public void testSetNameAndType() {
    final SimpleEntity e1 = new SimpleEntity();
    final InMemoryProducer p = new InMemoryProducer("AAA");
    p.register(SimpleEntity.class, "setName", "typeName", new Func<Iterable<SimpleEntity>>() {
      @Override
      public Iterable<SimpleEntity> apply() {
        return Enumerable.create(e1, new SimpleEntity());
      }
    }, "Id");

    Assert.assertEquals(2, p.getEntities("setName", NULL_QUERY).getEntities().size());
    Assert.assertNotNull(p.getEntity("setName", OEntityKey.create(e1.getId()), NULL_QUERY).getEntity());

    Assert.assertNotNull(p.getMetadata().findEdmEntitySet("setName"));
    Assert.assertNotNull(p.getMetadata().findEdmEntityType("AAA.typeName"));
  }

  static class SimpleEntity {
    public String getId() {
      return String.valueOf(System.identityHashCode(this));
    }

    public String getString() {
      return "string-" + getId();
    }

    public boolean getBool() {
      return false;
    }
  }

  private static class StreamEntity extends SimpleEntity implements OAtomStreamEntity {
    @Override
    public String getAtomEntityType() {
      return "application/zip";
    }

    @Override
    public String getAtomEntitySource() {
      return "somewhere";
    }
  }

}
