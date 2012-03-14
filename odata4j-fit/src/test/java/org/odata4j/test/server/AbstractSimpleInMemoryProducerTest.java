package org.odata4j.test.server;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

/**
 * Base test class that uses a simple InMemoryProducer as test scenario.
 */
public abstract class AbstractSimpleInMemoryProducerTest extends AbstractHttpClientTest {

  protected static final String ENTITY_SET_NAME = "Alphabet";
  protected static final String[] ENTITIES = { "A", "B", "C" };

  public AbstractSimpleInMemoryProducerTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void createTestScenario() {
    InMemoryProducer producer = new InMemoryProducer("Simple");

    producer.register(String.class, String.class, ENTITY_SET_NAME, new Func<Iterable<String>>() {
      public Iterable<String> apply() {
        return Enumerable.create(ENTITIES);
      }
    }, Funcs.identity(String.class));

    DefaultODataProducerProvider.setInstance(producer);
  }
}
