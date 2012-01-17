package org.odata4j.test.core.links;

import org.junit.Test;
import org.odata4j.format.FormatType;

public abstract class AbstractLinksTestInMemory extends LinksTest {

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
