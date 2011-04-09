package org.odata4j.producer.jpa.northwind;

import org.junit.BeforeClass;
import org.junit.Test;

public class JPAProducerQueryOptionTest50 extends JPAProducerTestBase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(50);
	}

	@Test
	public void systemQueryOptionFilterNotEqualTest() {
		String inp = "SystemQueryOptionFilterNotEqualTest";
		String uri = "Suppliers?$filter=Country ne 'UK'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void resourcePathComplexFilterEqualTest() {
		String inp = "ResourcePathComplexFilterEqualTest";
		String uri = "Categories(1)/Products?$filter=Supplier/Address eq '49 Gilbert St.'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}
}
