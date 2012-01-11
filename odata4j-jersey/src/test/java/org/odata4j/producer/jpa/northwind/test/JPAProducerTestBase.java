package org.odata4j.producer.jpa.northwind.test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.core4j.Func1;
import org.junit.AfterClass;
import org.odata4j.examples.jersey.producer.JerseyProducerUtil;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.JPAProvider;

public abstract class JPAProducerTestBase {
  protected static final String endpointUri =
      "http://localhost:8810/northwind/Northwind.svc/";

  protected static EntityManagerFactory emf;
  protected static ODataServer server;

  public static void setUpClass(int maxResults) throws Exception {
    setUpClass(maxResults, null);
  }

  public static void setUpClass(int maxResults, Func1<ODataProducer, ODataProducer> producerModification) throws Exception {
    String persistenceUnitName = "NorthwindService" + JPAProvider.JPA_PROVIDER.caption;
    String namespace = "Northwind";

    emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    ODataProducer producer = new JPAProducer(
        emf,
        namespace,
        maxResults); // http://services.odata.org/northwind/Northwind.svc/ is
    // using 20 as maxResult in almost any case but not
    // for every

    NorthwindTestDataUtil.fillDatabase(emf);

    if (producerModification != null)
      producer = producerModification.apply(producer);

    DefaultODataProducerProvider.setInstance(producer);
    server = JerseyProducerUtil.startODataServer(endpointUri);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
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
