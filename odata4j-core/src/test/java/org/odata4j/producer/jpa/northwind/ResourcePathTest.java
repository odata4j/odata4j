package org.odata4j.producer.jpa.northwind;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author sergei.grizenok
 */
public class ResourcePathTest extends JPAProducerTestBase {

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
	public void ResourcePathCollectionTest() {
		String inp = "ResourcePathCollectionTest";
		String uri = "Categories";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
		NorthwindTestUtils.testAtomResult(endpointUri, uri, inp);
	}

	@Test
	public void ResourcePathKeyPredicateTest() {
		String inp = "ResourcePathKeyPredicateTest";
		String uri = "Categories(1)";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void ResourcePathNavPropSingleTest() {
		String inp = "ResourcePathNavPropSingleTest";
		String uri = "Categories(1)/CategoryName";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
		NorthwindTestUtils.testAtomResult(endpointUri, uri, inp);
	}

	@Test
	public void ResourcePathNavPropCollectionTest() {
		String inp = "ResourcePathNavPropCollectionTest";
		String uri = "Categories(1)/Products?$filter=ProductID gt 0";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void ResourcePathComplexTypeTest() {
		String inp = "ResourcePathComplexTypeTest";
		String uri = "Categories(1)/Products(1)/Supplier/Address";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}
}
