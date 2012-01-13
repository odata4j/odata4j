package org.odata4j.producer.jpa.northwind.test;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractQueryOptionAtomTest50 extends AbstractJPAProducerTest {

  @Before
  public void setUp() throws Exception {
    super.setUp(50);
  }

  @Test
  // http://services.odata.org/northwind/Northwind.svc/ is using maxResult > 20 here?
  public void systemQueryOptionFilterNotEqualTest() {
    String inp = "SystemQueryOptionFilterNotEqualTest";
    String uri = "Suppliers?$filter=Country ne 'UK'";
    this.getUtils().testAtomResult(endpointUri, uri, inp);
  }

}
