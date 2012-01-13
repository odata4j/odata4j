package org.odata4j.producer.jpa.northwind.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractQueryOptionAtomTest extends AbstractJPAProducerTest {

  @Before
  public void setUp() throws Exception {
    super.setUp(20);
  }

  @Test
  public void SystemQueryOptionOrderByTest() {
    String inp = "SystemQueryOptionOrderByTest";
    String uri = "Products?$top=20&$orderby=ProductID";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionOrderByDescTest() {
    String inp = "SystemQueryOptionOrderByDescTest";
    String uri = "Products?$top=10&$orderby=ProductID desc";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionTopTest() {
    String inp = "SystemQueryOptionTopTest";
    String uri = "Products?$top=5";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionOrderByTopTest() {
    String inp = "SystemQueryOptionOrderByTopTest";
    String uri = "Products?$top=5&$orderby=ProductName desc";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionSkipTest() {
    String inp = "SystemQueryOptionSkipTest";
    String uri = "Categories(1)/Products?$skip=2";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionOrderBySkipTest() {
    String inp = "SystemQueryOptionOrderBySkipTest";
    String uri = "Products?$skip=2&$top=2&$orderby=ProductName";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  // @Test
  // public void SystemQueryOptionTop5000Test() {
  // String inp = "SystemQueryOptionTop5000Test";
  // String uri = "Products?$top=5000";
  // TestUtils.testAtomResult(endpointUri, uri, inp);
  // }

  @Test
  public void SystemQueryOptionSkipTokenTest() {
    String inp = "SystemQueryOptionSkipTokenTest";
    String uri = "Customers?$top=5&$skiptoken='ANATR'";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionSkipTokenComplexKeyTest() {
    String inp = "SystemQueryOptionSkipTokenComplexKeyTest";
    String uri = "Order_Details?$top=5&$skiptoken=OrderID=10248,ProductID=11";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionSkipTokenWithOrderByTest() {
    String inp = "SystemQueryOptionSkipTokenWithOrderByTest";
    String uri = "Products?$orderby=SupplierID desc, ProductName&$skiptoken=20,'Gula Malacca',44";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterEqualTest() {
    String inp = "SystemQueryOptionFilterEqualTest";
    String uri = "Suppliers?$filter=Country eq 'Brazil'";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterGreaterThanTest() {
    String inp = "SystemQueryOptionFilterGreaterThanTest";
    String uri = "Products?$top=20&$filter=UnitPrice gt 20";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterGreaterThanOrEqualTest() {
    String inp = "SystemQueryOptionFilterGreaterThanOrEqualTest";
    String uri = "Products?$top=20&$filter=UnitPrice ge 10";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterLessThanTest() {
    String inp = "SystemQueryOptionFilterLessThanTest";
    String uri = "Products?$top=20&$filter=UnitPrice lt 20";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterLessThanOrEqualTest() {
    String inp = "SystemQueryOptionFilterLessThanOrEqualTest";
    String uri = "Products?$top=20&$filter=UnitPrice le 100";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterLogicalAndTest() {
    String inp = "SystemQueryOptionFilterLogicalAndTest";
    String uri = "Products?$top=20&$filter=UnitPrice le 200 and UnitPrice gt 3.5";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterLogicalOrTest() {
    String inp = "SystemQueryOptionFilterLogicalOrTest";
    String uri = "Products?$filter=UnitPrice le 3.5 or UnitPrice gt 200";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterGroupingLogicalAndTest() {
    String inp = "SystemQueryOptionFilterGroupingLogicalAndTest";
    String uri = "Products?$top=10&$filter=(UnitPrice gt 5) and (UnitPrice lt 20)";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionOrderByTop21Test() {
    String inp = "SystemQueryOptionOrderByTop21";
    String uri = "Products?$top=21&$orderby=ProductID";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionExpand1Test() {
    String inp = "SystemQueryOptionExpand1Test";
    String uri = "Categories?$expand=Products";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  // @Test
  // public void SystemQueryOptionExpand2Test() {
  // String inp = "SystemQueryOptionExpand2Test";
  // String uri = "Categories?$expand=Products/Supplier";
  // NorthwindTestUtils.testAtomResult(endpointUri, uri, inp);
  // }

  @Test
  public void SystemQueryOptionExpand3Test() {
    String inp = "SystemQueryOptionExpand3Test";
    String uri = "Products?$expand=Category,Supplier";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionExpand4Test() {
    String inp = "SystemQueryOptionExpand4Test";
    String uri = "Orders?$top=10&$orderby=OrderID&$expand=OrderDetails/Product";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionComplexKeyNavTest() {
    String inp = "SystemQueryOptionComplexKeyNavTest";
    String uri = "Order_Details(OrderID=10248,ProductID=11)/Product";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SelectOnSingleEntityTest() {
    String inp = "SelectOnSingleEntityTest";
    String uri = "Products(1)?$select=ProductName";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void MultiSelectOnSingleEntityTest() {
    String inp = "MultiSelectOnSingleEntityTest";
    String uri = "Products(1)?$select=ProductName,UnitPrice";
    this.getUtils().testJSONResult(endpointUri, uri, inp);
  }

  @Test
  public void ExpandOnSingleEntityTest() {
    String inp = "ExpandOnSingleEntityTest";
    String uri = "Products(1)?$expand=Category";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SelectExpandOnSingleEntityTest() {
    String inp = "SelectExpandOnSingleEntityTest";
    String uri = "Products(1)?$select=ProductName,Category&$expand=Category";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

}
