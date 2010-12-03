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

/**
 * 
 * @author sergei.grizenok
 */
public class JPAProducerResourcePathTest {

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
                50);

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
    public void ResourcePathCollectionTest() {
        String inp = "ResourcePathCollectionTest";
        String uri = "Categories";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void ResourcePathKeyPredicateTest() {
        String inp = "ResourcePathKeyPredicateTest";
        String uri = "Categories(1)";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void ResourcePathNavPropSingleTest() {
        String inp = "ResourcePathNavPropSingleTest";
        String uri = "Categories(1)/CategoryName";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void ResourcePathNavPropCollectionTest() {
        String inp = "ResourcePathNavPropCollectionTest";
        String uri = "Categories(1)/Products";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void ResourcePathComplexTypeTest() {
        String inp = "ResourcePathComplexTypeTest";
        String uri = "Categories(1)/Products(1)/Supplier/Address";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }
}
