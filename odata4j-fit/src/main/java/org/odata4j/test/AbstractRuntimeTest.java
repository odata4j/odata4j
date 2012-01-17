package org.odata4j.test;

import org.odata4j.fit.support.CxfRuntimeFacade;
import org.odata4j.fit.support.JerseyRuntimeFacade;
import org.odata4j.fit.support.RuntimeFacade;

public abstract class AbstractRuntimeTest {

  public enum RuntimeFacadeType {
    JERSEY, CXF
  }

  protected RuntimeFacade rtFacade;

  {
    RuntimeFacadeType type = this.determineRuntime();
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

  private RuntimeFacadeType determineRuntime() {

    // TODO implement runtime detection (Jersey or CXF)

    return RuntimeFacadeType.JERSEY;
  }

}
