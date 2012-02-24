package org.odata4j.producer.jpa.northwind.test;

import org.junit.Before;
import org.junit.Test;

public class QueryOptionTest50 extends JPAProducerTest {

  public QueryOptionTest50(RuntimeFacadeType type) {
    super(type);
  }

  @Before
  public void setUpClass() throws Exception {
    super.setUp(50);
  }

  @Test
  public void systemQueryOptionFilterNotEqualTest() {
    String inp = "SystemQueryOptionFilterNotEqualTest";
    String uri = "Suppliers?$filter=Country ne 'UK'";
    this.utils.testJSONResult(endpointUri, uri, inp);
  }

  @Test
  public void resourcePathComplexFilterEqualTest() {
    String inp = "ResourcePathComplexFilterEqualTest";
    String uri = "Categories(1)/Products?$filter=Supplier/Address eq '49 Gilbert St.'";
    this.utils.testJSONResult(endpointUri, uri, inp);
  }
}
