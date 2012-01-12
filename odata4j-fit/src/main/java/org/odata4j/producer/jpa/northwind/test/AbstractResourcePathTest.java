package org.odata4j.producer.jpa.northwind.test;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractResourcePathTest extends AbstractJPAProducerTest {

  @Before
  public void setUpClass() throws Exception {
    super.setUp(50);
  }

  @Test
  public void ResourcePathCollectionTest() {
    String inp = "ResourcePathCollectionTest";
    String uri = "Categories";
    this.getUtils().testJSONResult(endpointUri, uri, inp);
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void ResourcePathKeyPredicateTest() {
    String inp = "ResourcePathKeyPredicateTest";
    String uri = "Categories(1)";
    this.getUtils().testJSONResult(endpointUri, uri, inp);
  }

  @Test
  public void ResourcePathNavPropSingleTest() {
    String inp = "ResourcePathNavPropSingleTest";
    String uri = "Categories(1)/CategoryName";
    this.getUtils().testJSONResult(endpointUri, uri, inp);
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void ResourcePathNavPropCollectionTest() {
    String inp = "ResourcePathNavPropCollectionTest";
    String uri = "Categories(1)/Products?$filter=ProductID gt 0";
    this.getUtils().testJSONResult(endpointUri, uri, inp);
  }

  @Test
  public void ResourcePathComplexTypeTest() {
    String inp = "ResourcePathComplexTypeTest";
    String uri = "Categories(1)/Products(1)/Supplier/Address";
    this.getUtils().testJSONResult(endpointUri, uri, inp);
  }
}
