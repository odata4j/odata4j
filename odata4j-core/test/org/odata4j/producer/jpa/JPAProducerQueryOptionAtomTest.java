package org.odata4j.producer.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;

public class JPAProducerQueryOptionAtomTest {

    private static final String endpointUri =
            "http://localhost:8810/northwind/Northwind.svc/";
    
    private static EntityManagerFactory emf;
    private static JerseyServer server;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String persistenceUnitName = "NorthwindService";
        String namespace = "Northwind";

        emf = Persistence.createEntityManagerFactory(
                persistenceUnitName);

        JPAProducer producer = new JPAProducer(
                emf,
                namespace,
                20);	// http://services.odata.org/northwind/Northwind.svc/ is using 20 as maxResult

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

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void SystemQueryOptionOrderByTest() {
        String inp = "SystemQueryOptionOrderByTest";
        String uri = "Products?$top=20&$orderby=ProductID";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionOrderByTop21Test() {
        String inp = "SystemQueryOptionOrderByTop21";
        String uri = "Products?$top=21&$orderby=ProductID";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionOrderByDescTest() {
        String inp = "SystemQueryOptionOrderByDescTest";
        String uri = "Products?$top=10&$orderby=ProductID desc";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionExpand1Test() {
        String inp = "SystemQueryOptionExpand1Test";
        String uri = "Categories?$expand=Products";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
}
