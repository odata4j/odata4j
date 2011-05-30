package org.odata4j.producer.jpa.northwind.test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;
import org.odata4j.test.OData4jTestSuite;

public abstract class JPAProducerTestBase {
  protected static final String endpointUri =
      "http://localhost:8810/northwind/Northwind.svc/";

  protected static EntityManagerFactory emf;
  protected static JerseyServer server;

  public static void setUpClass(int maxResults) throws Exception {
    String persistenceUnitName = "NorthwindService" + OData4jTestSuite.JPA_PROVIDER.caption;
    String namespace = "Northwind";

    emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    JPAProducer producer = new JPAProducer(
        emf,
        namespace,
        maxResults); // http://services.odata.org/northwind/Northwind.svc/ is
    // using 20 as maxResult in almost any case but not
    // for every

    NorthwindTestUtils.fillDatabase(emf);

    ODataProducerProvider.setInstance(producer);
    server = ProducerUtil.startODataServer(endpointUri);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    if (server != null) {
      server.stop();
    }

    if (emf != null) {
      emf.close();
    }
  }

}
