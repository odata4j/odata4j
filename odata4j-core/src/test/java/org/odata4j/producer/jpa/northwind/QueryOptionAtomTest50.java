package org.odata4j.producer.jpa.northwind;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryOptionAtomTest50 extends JPAProducerTestBase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(50);
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
    
    @Test
    //	http://services.odata.org/northwind/Northwind.svc/ is using maxResult > 20 here?
    public void systemQueryOptionFilterNotEqualTest() {
        String inp = "SystemQueryOptionFilterNotEqualTest";
        String uri = "Suppliers?$filter=Country ne 'UK'";
        NorthwindTestUtils.testAtomResult(endpointUri, uri, inp);
    }
    
}
