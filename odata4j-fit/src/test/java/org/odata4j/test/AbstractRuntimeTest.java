package org.odata4j.test;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Run all JUnit test cases twice. Once for Jersey and once for CXF runtime.
 */
@RunWith(Parameterized.class)
public abstract class AbstractRuntimeTest extends AbstractTest {

  private final static String RUNTIME_ENVIRONMENT_PROPERTY = "org.odata4j.jaxrs.runtime";

  protected enum RuntimeFacadeType {
    JERSEY, CXF;

    public static RuntimeFacadeType fromString(String value) {
      RuntimeFacadeType st = JERSEY; // default

      value = value.trim().toUpperCase();
      if ("JERSEY".equals(value)) {
        st = JERSEY;
      } else if ("CXF".equals(value)) {
        st = CXF;
      } else {
        throw new IllegalArgumentException("Wrong value for " + AbstractRuntimeTest.RUNTIME_ENVIRONMENT_PROPERTY + " = " + value + ". Allowed is [JERSEY|CXF]");
      }

      return st;
    }
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
    this.logger.info("******************************************************************");
    this.logger.info("Activated Runtime Facade: " + type);
    this.logger.info("******************************************************************");
  }

  @Parameterized.Parameters
  public static List<Object[]> data() {

    /*
       TODO Ideally the test suite runs twice using CXF and Jersey as runtime. 
       Unfortunately CXF has issues and throws errors if Jersey did run first. This 
       issues are addressed to CXF mailing list: users@cxf.apache.org
       
       Intermediate wise the runtime is selected from environment variable. Allowed configurations:
       
       org.odata4j.jaxrs.runtime = jersey
       org.odata4j.jaxrs.runtime = cxf
       org.odata4j.jaxrs.runtime = jersey, cxf // currently not supported by CXF
       
     */

    Object[][] a;
    String value = System.getProperty(AbstractRuntimeTest.RUNTIME_ENVIRONMENT_PROPERTY);
    if (null == value) {
      a = new Object[][] { { RuntimeFacadeType.JERSEY } }; // default
    }
    else {
      StringTokenizer strt = new StringTokenizer(value, " ,;", false);
      a = new Object[strt.countTokens()][1];
      for (int i = 0; strt.hasMoreTokens(); i++) {
        a[i][0] = RuntimeFacadeType.fromString(strt.nextToken());
      }
    }

    return Arrays.asList(a);
  }

  protected RuntimeFacade rtFacade;

}
