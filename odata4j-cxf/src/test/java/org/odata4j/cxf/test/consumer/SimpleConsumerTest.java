package org.odata4j.cxf.test.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map.Entry;

import org.core4j.Func;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.cxf.test.AbstractProducerTest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.inmemory.InMemoryProducer;

public class SimpleConsumerTest extends AbstractProducerTest {

  public SimpleConsumerTest() {
    super();
  }

  @Test
  public void consumerUri() {
    ODataConsumer consumer = this.getODataConsumer();

    String uri = consumer.getServiceRootUri();
    assertEquals(this.getBaseUri().toString(), uri);
  }

  @Test
  public void consumerMetaData() {
    ODataConsumer consumer = this.getODataConsumer();

    EdmDataServices eds = consumer.getMetadata();
    assertNotNull(eds);

    String version = eds.getVersion();
    assertNotNull(version);
    assertEquals("1.0", version);

    EdmType type1 = eds.findEdmEntityType("Example.SystemProperties");
    assertNotNull(type1);
    assertEquals("Example.SystemProperties", type1.getFullyQualifiedTypeName());

    EdmType type2 = eds.findEdmEntityType("Example.EnvironmentVariables");
    assertNotNull(type2);
    assertEquals("Example.EnvironmentVariables", type2.getFullyQualifiedTypeName());
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void registerEntities(InMemoryProducer producer) {
    // expose current system properties (Map.Entry instances) as an entity-set called "SystemProperties"
    producer.register(Entry.class, "SystemProperties", new Func<Iterable<Entry>>() {
      public Iterable<Entry> apply() {
        return (Iterable<Entry>) (Object) System.getProperties().entrySet();
      }
    }, "Key");
    // expose current environment variables (Map.Entry instances) as an entity-set called "EnvironmentVariables"
    producer.register(Entry.class, "EnvironmentVariables", new Func<Iterable<Entry>>() {
      public Iterable<Entry> apply() {
        return (Iterable<Entry>) (Object) System.getenv().entrySet();
      }
    }, "Key");

  }
}
