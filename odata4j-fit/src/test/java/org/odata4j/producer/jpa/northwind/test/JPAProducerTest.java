package org.odata4j.producer.jpa.northwind.test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.core4j.Func1;
import org.junit.After;
import org.junit.Ignore;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.AbstractRuntimeTest;
import org.odata4j.test.JPAProvider;

@Ignore
public class JPAProducerTest extends AbstractRuntimeTest {

  public JPAProducerTest(RuntimeFacadeType type) {
    super(type);
    this.utils = new NorthwindTestUtils(this.rtFacade);
  }

  protected static final String endpointUri =
      "http://localhost:8810/northwind/Northwind.svc/";

  protected EntityManagerFactory emf;
  protected ODataServer server;

  protected NorthwindTestUtils utils; 

  public void setUp(int maxResults) {
    setUp(maxResults, null);
  }

  public void setUp(int maxResults, Func1<ODataProducer, ODataProducer> producerModification) {
    String persistenceUnitName = "NorthwindService" + JPAProvider.JPA_PROVIDER.caption;
    String namespace = "Northwind";

    emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    ODataProducer producer = new JPAProducer(
        emf,
        namespace,
        maxResults); // http://services.odata.org/northwind/Northwind.svc/ is
    // using 20 as maxResult in almost any case but not
    // for every

    NorthwindTestUtils.fillDatabase(emf);

    if (producerModification != null)
      producer = producerModification.apply(producer);

    DefaultODataProducerProvider.setInstance(producer);
    server = this.rtFacade.startODataServer(endpointUri);
  }

  @After
  public void tearDown() throws Exception {
    if (server != null) {
      server.stop();
      server = null;
    }

    if (emf != null) {
      emf.close();
      emf = null;
    }
  }

}
