package org.odata4j.producer.jpa.northwind.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryOptionTest extends JPAProducerTestBase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(20);
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
		String uri = "Products?$orderby=ProductID";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionOrderByDescTest() {
		String inp = "SystemQueryOptionOrderByDescTest";
		String uri = "Products?$orderby=ProductID desc";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionTopTest() {
		String inp = "SystemQueryOptionTopTest";
		String uri = "Products?$top=5";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionOrderByTopTest() {
		String inp = "SystemQueryOptionOrderByTopTest";
		String uri = "Products?$top=5&$orderby=ProductName desc";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionSkipTest() {
		String inp = "SystemQueryOptionSkipTest";
		String uri = "Categories(1)/Products?$skip=2";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionOrderBySkipTest() {
		String inp = "SystemQueryOptionOrderBySkipTest";
		String uri = "Products?$skip=2&$top=2&$orderby=ProductName";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	// @Test
	// public void SystemQueryOptionTop5000Test() {
	// String inp = "SystemQueryOptionTop5000Test";
	// String uri = "Products?$top=5000";
	// TestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	@Test
	public void SystemQueryOptionSkipTokenTest() {
		String inp = "SystemQueryOptionSkipTokenTest";
		String uri = "Customers?$top=5&$skiptoken='ANATR'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}
	
	@Test
	public void SystemQueryOptionSkipTokenComplexKeyTest() {
		String inp = "SystemQueryOptionSkipTokenComplexKeyTest";
		String uri = "Order_Details?$top=5&$skiptoken=OrderID=10248,ProductID=11";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}
	
	@Test
	public void SystemQueryOptionSkipTokenWithOrderByTest() {
		String inp = "SystemQueryOptionSkipTokenWithOrderByTest";
		String uri = "Products?$orderby=SupplierID desc, ProductName&$skiptoken=20,'Gula Malacca',44";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterEqualTest() {
		String inp = "SystemQueryOptionFilterEqualTest";
		String uri = "Suppliers?$filter=Country eq 'Brazil'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterGreaterThanTest() {
		String inp = "SystemQueryOptionFilterGreaterThanTest";
		String uri = "Products?$top=20&$filter=UnitPrice gt 20";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterGreaterThanOrEqualTest() {
		String inp = "SystemQueryOptionFilterGreaterThanOrEqualTest";
		String uri = "Products?$filter=UnitPrice ge 10";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterLessThanOrEqualTest() {
		String inp = "SystemQueryOptionFilterLessThanOrEqualTest";
		String uri = "Products?$top=20&$filter=UnitPrice le 100";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterLessThanTest() {
		String inp = "SystemQueryOptionFilterLessThanTest";
		String uri = "Products?$filter=UnitPrice lt 20";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterLogicalAndTest() {
		String inp = "SystemQueryOptionFilterLogicalAndTest";
		String uri =
				"Products?$top=20&$filter=UnitPrice le 200 and UnitPrice gt 3.5";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterLogicalOrTest() {
		String inp = "SystemQueryOptionFilterLogicalOrTest";
		String uri = "Products?$filter=UnitPrice le 3.5 or UnitPrice gt 200";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterGroupingLogicalAndTest() {
		String inp = "SystemQueryOptionFilterGroupingLogicalAndTest";
		String uri =
				"Products?$top=10&$filter=%28UnitPrice%20gt%205%29%20and%20%28UnitPrice%20lt%2020%29";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterAdditionTest() {
		String inp = "SystemQueryOptionFilterAdditionTest";
		String uri = "Products?$filter=UnitPrice add 5 gt 10";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterSubtractionTest() {
		String inp = "SystemQueryOptionFilterSubtractionTest";
		String uri = "Products?$filter=UnitPrice sub 5 gt 10";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterMultiplicationTest() {
		String inp = "SystemQueryOptionFilterMultiplicationTest";
		String uri = "Products?$filter=UnitPrice mul 2 gt 2000";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterDivisionTest() {
		String inp = "SystemQueryOptionFilterDivisionTest";
		String uri = "Products?$filter=UnitPrice div 2 gt 4";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterModuloTest() {
		String inp = "SystemQueryOptionFilterModuloTest";
		String uri = "Products?$filter=ProductID mod 8 eq 2";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterGroupingArithmeticSubTest() {
		String inp = "SystemQueryOptionFilterGroupingArithmeticSubTest";
		String uri = "Products?$filter=(UnitPrice sub 5) gt 10";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterLogicalNotTest() {
		String inp = "SystemQueryOptionFilterLogicalNotTest";
		String uri = "Products?$filter=not endswith(QuantityPerUnit,'bags')";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterBoolSubstringOfTest() {
		String inp = "SystemQueryOptionFilterBoolSubstringOfTest";
		String uri =
				"Customers?$filter=substringof('Alfreds', CompanyName) eq true";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterBoolEndswithTest() {
		String inp = "SystemQueryOptionFilterBoolEndswithTest";
		String uri =
				"Customers?$filter=endswith(CompanyName, 'Futterkiste') eq true";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterBoolStartswithTest() {
		String inp = "SystemQueryOptionFilterBoolStartswithTest";
		String uri = "Customers?$filter=startswith(CompanyName, 'Alfr') eq true";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterIntLengthTest() {
		String inp = "SystemQueryOptionFilterIntLengthTest";
		String uri = "Customers?$filter=length(CompanyName) eq 19";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterIntIndexofTest() {
		String inp = "SystemQueryOptionFilterIntIndexofTest";
		String uri = "Customers?$filter=indexof(CompanyName, 'lfreds') eq 1";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterStringReplaceTest() {
		String inp = "SystemQueryOptionFilterStringReplaceTest";
		String uri =
				"Customers?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterStringSubstringTest() {
		String inp = "SystemQueryOptionFilterStringSubstringTest";
		String uri =
				"Customers?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterStringSubstring2Test() {
		String inp = "SystemQueryOptionFilterStringSubstring2Test";
		String uri = "Customers?$filter=substring(CompanyName, 1, 2) eq 'lf'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterStringToLowerTest() {
		String inp = "SystemQueryOptionFilterStringToLowerTest";
		String uri =
				"Customers?$filter=tolower(CompanyName) eq 'alfreds futterkiste'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterStringToupperTest() {
		String inp = "SystemQueryOptionFilterStringToupperTest";
		String uri =
				"Customers?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterStringTrimTest() {
		String inp = "SystemQueryOptionFilterStringTrimTest";
		String uri =
				"Customers?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionFilterStringConcatTest() {
		String inp = "SystemQueryOptionFilterStringConcatTest";
		String uri =
				"Customers?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	// TODO: date time

	// @Test
	// public void SystemQueryOptionFilterIntDayTest() {
	// String inp = "SystemQueryOptionFilterIntDayTest";
	// String uri = "Employees?$filter=day(BirthDate) eq 8";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	// @Test
	// public void SystemQueryOptionFilterIntHourTest() {
	// String inp = "SystemQueryOptionFilterIntHourTest";
	// String uri = "Employees?$filter=hour(BirthDate) eq 0";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }
	//
	// @Test
	// public void SystemQueryOptionFilterIntMinuteTest() {
	// String inp = "SystemQueryOptionFilterIntMinuteTest";
	// String uri = "Employees?$filter=minute(BirthDate) eq 0";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }
	//
	// @Test
	// public void SystemQueryOptionFilterIntMonthTest() {
	// String inp = "SystemQueryOptionFilterIntMonthTest";
	// String uri = "Employees?$filter=month(BirthDate) eq 12";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }
	//
	// @Test
	// public void SystemQueryOptionFilterIntSecondTest() {
	// String inp = "SystemQueryOptionFilterIntSecondTest";
	// String uri = "Employees?$filter=second(BirthDate) eq 0";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }
	//
	// @Test
	// public void SystemQueryOptionFilterIntYearTest() {
	// String inp = "SystemQueryOptionFilterIntYearTest";
	// String uri = "Employees?$filter=year(BirthDate) eq 1948";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	// TODO: numeric

	// @Test
	// public void SystemQueryOptionFilterRoundTest() {
	// String inp = "SystemQueryOptionFilterRoundTest";
	// String uri = "Orders?$filter=round(Freight) eq 32";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	// @Test
	// public void SystemQueryOptionFilterDecimalFloorTest() {
	// String inp = "SystemQueryOptionFilterDecimalFloorTest";
	// String uri = "Orders?$filter=floor(Freight) eq 32";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }
	//
	// @Test
	// public void SystemQueryOptionFilterDoubleCeilingTest() {
	// String inp = "SystemQueryOptionFilterDoubleCeilingTest";
	// String uri = "Orders?$filter=ceiling(Freight) eq 33";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }
	//
	// @Test
	// public void SystemQueryOptionFilterDecimalCeilingTest() {
	// String inp = "SystemQueryOptionFilterDecimalCeilingTest";
	// String uri = "Orders?$filter=floor(Freight) eq 33";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	// TODO: type test

	// @Test
	// public void SystemQueryOptionFilterBoolIsOfTest() {
	// String inp = "SystemQueryOptionFilterBoolIsOfTest";
	// String uri = "Orders?$filter=isof('NorthwindModel.Order')";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	// @Test
	// public void SystemQueryOptionFilterBoolIsOf2Test() {
	// String inp = "SystemQueryOptionFilterBoolIsOf2Test";
	// String uri = "Orders?$filter=isof(ShipCountry, 'Edm.String')";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	@Test
	public void SystemQueryOptionExpand1Test() {
		String inp = "SystemQueryOptionExpand1Test";
		String uri = "Categories?$expand=Products";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	// @Test
	// public void SystemQueryOptionExpand2Test() {
	// String inp = "SystemQueryOptionExpand2Test";
	// String uri = "Categories?$expand=Products/Supplier";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	@Test
	public void SystemQueryOptionExpand3Test() {
		String inp = "SystemQueryOptionExpand3Test";
		String uri = "Products?$expand=Category,Supplier";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	// @Test
	// public void SystemQueryOptionFormatAtomTest() {
	// String inp = "SystemQueryOptionFormatAtomTest";
	// String uri = "Products?$format=atom";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	// @Test
	// public void SystemQueryOptionFormatJsonTest() {
	// String inp = "SystemQueryOptionFormatJsonTest";
	// String uri = "Products?$top=20&$format=json";
	// NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	// }

	// TODO: select+$expand
//	@Test
//	public void SystemQueryOptionSelect3Test() {
//		String inp = "SystemQueryOptionSelect3Test";
//		String uri = "Categories?$select=CategoryName,Products&$expand=Products/Supplier";
//		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
//	}

	@Test
	public void SystemQueryOptionSelect1Test() {
		String inp = "SystemQueryOptionSelect1Test";
		String uri = "Products?$select=UnitPrice,ProductName";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionSelect2Test() {
		String inp = "SystemQueryOptionSelect2Test";
		String uri = "Products?$select=ProductName,Category";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionInlinecountTest() {
		String inp = "SystemQueryOptionInlinecountTest";
		String uri = "Products?$inlinecount=allpages";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void SystemQueryOptionInlinecountTopTest() {
		String inp = "SystemQueryOptionInlinecountTopTest";
		String uri = "Products?$top=5&$inlinecount=allpages&Price gt 200";
		NorthwindTestUtils.testJSONResult(endpointUri, uri, inp);
	}
}
