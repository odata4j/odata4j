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
    public void SystemQueryOptionOrderByDescTest() {
        String inp = "SystemQueryOptionOrderByDescTest";
        String uri = "Products?$top=10&$orderby=ProductID desc";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionTopTest() {
        String inp = "SystemQueryOptionTopTest";
        String uri = "Products?$top=5";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionOrderByTopTest() {
        String inp = "SystemQueryOptionOrderByTopTest";
        String uri = "Products?$top=5&$orderby=ProductName desc";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionSkipTest() {
        String inp = "SystemQueryOptionSkipTest";
        String uri = "Categories(1)/Products?$skip=2";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionOrderBySkipTest() {
        String inp = "SystemQueryOptionOrderBySkipTest";
        String uri = "Products?$skip=2&$top=2&$orderby=ProductName";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionFilterEqualTest() {
        String inp = "SystemQueryOptionFilterEqualTest";
        String uri = "Suppliers?$filter=Country eq 'Brazil'";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
//    @Test
//    //	http://services.odata.org/northwind/Northwind.svc/ is using maxResult > 20 here?
//    public void SystemQueryOptionFilterNotEqualTest() {
//        String inp = "SystemQueryOptionFilterNotEqualTest";
//        String uri = "Suppliers?$filter=Country ne 'UK'";
//        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
//    }
    
    @Test
    public void SystemQueryOptionFilterGreaterThanTest() {
        String inp = "SystemQueryOptionFilterGreaterThanTest";
        String uri = "Products?$top=20&$filter=UnitPrice gt 20";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionFilterGreaterThanOrEqualTest() {
        String inp = "SystemQueryOptionFilterGreaterThanOrEqualTest";
        String uri = "Products?$top=20&$filter=UnitPrice ge 10";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionFilterLessThanTest() {
        String inp = "SystemQueryOptionFilterLessThanTest";
        String uri = "Products?$top=20&$filter=UnitPrice lt 20";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionFilterLessThanOrEqualTest() {
        String inp = "SystemQueryOptionFilterLessThanOrEqualTest";
        String uri = "Products?$top=20&$filter=UnitPrice le 100";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionFilterLogicalAndTest() {
        String inp = "SystemQueryOptionFilterLogicalAndTest";
        String uri =
                "Products?$top=20&$filter=UnitPrice le 200 and UnitPrice gt 3.5";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionFilterLogicalOrTest() {
        String inp = "SystemQueryOptionFilterLogicalOrTest";
        String uri = "Products?$filter=UnitPrice le 3.5 or UnitPrice gt 200";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    @Test
    public void SystemQueryOptionFilterGroupingLogicalAndTest() {
        String inp = "SystemQueryOptionFilterGroupingLogicalAndTest";
        String uri = "Products?$top=10&$filter=(UnitPrice gt 5) and (UnitPrice lt 20)";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
    
    @Test
    public void SystemQueryOptionOrderByTop21Test() {
        String inp = "SystemQueryOptionOrderByTop21";
        String uri = "Products?$top=21&$orderby=ProductID";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionExpand1Test() {
        String inp = "SystemQueryOptionExpand1Test";
        String uri = "Categories?$expand=Products";
        NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
    }
    
//     @Test
//     public void SystemQueryOptionExpand2Test() {
//	     String inp = "SystemQueryOptionExpand2Test";
//	     String uri = "Categories?$expand=Products/Supplier";
//	     NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
//     }
    
     @Test
     public void SystemQueryOptionExpand3Test() {
	     String inp = "SystemQueryOptionExpand3Test";
	     String uri = "Products?$expand=Category,Supplier";
	     NorthwindTestUtils.TestAtomResult(endpointUri, uri, inp);
     }
    
}
