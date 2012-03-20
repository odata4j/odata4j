package org.odata4j.test.integration.producer.jpa.links;

import org.junit.Test;
import org.odata4j.format.FormatType;

public class LinksTestInMemory extends LinksTest {

  public LinksTestInMemory(RuntimeFacadeType type) {
    super(type);
  }

  @Test
  public void testReadDeferredAtom() {
    testReadDeferred(FormatType.ATOM);
  }

  @Test
  public void testReadDeferredJSON() {
    testReadDeferred(FormatType.JSON);
  }

  @Test
  public void testReadEmptyAtom() {
    testReadEmpty(FormatType.ATOM);
  }

  @Test
  public void testReadEmptyJSON() {
    testReadEmpty(FormatType.JSON);
  }

  @Test
  public void testReadPopulatedAtom() {
    testReadPopulated(FormatType.ATOM);
  }

  @Test
  public void testReadPopulatedJSON() {
    testReadPopulated(FormatType.JSON);
  }

}
