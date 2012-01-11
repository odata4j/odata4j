package org.odata4j.fit.util;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Funcs;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

/**
 * Base test class that uses an ODataServer as server, a Jetty HttpClient as client and a simple
 * InMemoryProducer as test scenario.
 * <p>Method {@code createServer} needs to be implemented to create a concrete ODataServer
 * implementation.</p>
 */
public abstract class AbstractODataServerHttpClientSimpleInMemoryProducerTest extends AbstractODataServerHttpClientTest {

  protected static final String ENTITY_SET_NAME = "Alphabet";
  protected static final String[] ENTITIES = { "A", "B", "C" };

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
