package org.odata4j.test;

import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Run all JUnit test cases twice. Once for Jersey and then for CXF runtime.
 */
@RunWith(Parameterized.class)
public abstract class AbstractRuntimeTest {

  public enum RuntimeFacadeType {
    JERSEY, CXF
  }

  public AbstractRuntimeTest(RuntimeFacadeType type) {
    System.out.println("constructor: " + type);
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
  }

  
  @Parameterized.Parameters
  public static List<Object[]> data() {
    Object[][] a = new Object[][] { { RuntimeFacadeType.JERSEY } /*, { RuntimeFacadeType.CXF } */ };
    return Arrays.asList(a);
  }
  
  protected RuntimeFacade rtFacade;

}
