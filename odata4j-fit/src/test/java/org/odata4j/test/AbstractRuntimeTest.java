package org.odata4j.test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Run all JUnit test cases twice. Once for Jersey and once for CXF runtime.
 */
@RunWith(Parameterized.class)
public abstract class AbstractRuntimeTest {

  private final Logger log = Logger.getLogger(this.getClass().getName());

  protected Logger getLog() {
    return this.log;
  }

  protected enum RuntimeFacadeType {
    JERSEY, CXF
  }

  public AbstractRuntimeTest(RuntimeFacadeType type) {
    switch (type) {
    case JERSEY:
      this.rtFacade = new JerseyRuntimeFacade();
      break;
    case CXF:
      this.rtFacade = new CxfRuntimeFacade();
      break;
    default:
      throw new RuntimeException("JAX-RS runtime type not supported: " + type);
    }
    this.getLog().info("Activated Runtime Facade: " + type);
  }

  @Parameterized.Parameters
  public static List<Object[]> data() {
    // TODO enable CXF as soon as implementation is completed and all test cases are green
    Object[][] a = new Object[][] { { RuntimeFacadeType.JERSEY } /*, { RuntimeFacadeType.CXF } */};
    return Arrays.asList(a);
  }

  protected RuntimeFacade rtFacade;

}
