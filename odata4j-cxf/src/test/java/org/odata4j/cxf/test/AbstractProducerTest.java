package org.odata4j.cxf.test;

import org.junit.Before;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.resources.RootApplication;

public abstract class AbstractProducerTest extends AbstractCxfRuntimeTest {

  public AbstractProducerTest() {
    super();

    // overwrite this
    this.getServer().setODataApplication(DefaultODataApplication.class);
    this.getServer().setRootApplication(RootApplication.class);
  }

  @Before
  public void setup() {
    InMemoryProducer producer = this.createProducer();
    DefaultODataProducerProvider.setInstance(producer);
    this.registerEntities(producer);
    super.setup();
  }

  protected abstract void registerEntities(InMemoryProducer producer);

  protected InMemoryProducer createProducer() {
    InMemoryProducer producer = this.createDefaultProducer();
    return producer;
  }

  private InMemoryProducer createDefaultProducer() {
    InMemoryProducer producer = new InMemoryProducer("Example") {
      final QueryInfo[] lastQuery = new QueryInfo[1];

      public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
        lastQuery[0] = queryInfo;
        return super.getEntities(entitySetName, queryInfo);
      }
    };
    return producer;
  }

}
