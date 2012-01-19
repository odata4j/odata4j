package org.odata4j.producer.jpa.northwind.test;

import org.junit.Before;
import org.junit.Test;

public class QueryOptionAtomTest50 extends JPAProducerTest {

  public QueryOptionAtomTest50(RuntimeFacadeType type) {
    super(type);
  }

  @Before
  public void setUp() {
    super.setUp(50);
  }

  @Test
  // http://services.odata.org/northwind/Northwind.svc/ is using maxResult > 20 here?
  public void systemQueryOptionFilterNotEqualTest() {
    String inp = "SystemQueryOptionFilterNotEqualTest";
    String uri = "Suppliers?$filter=Country ne 'UK'";
    this.utils.testAtomResult(endpointUri, uri, inp);
  }

}
