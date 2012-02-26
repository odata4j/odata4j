package org.odata4j.test;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.odata4j.core.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTest {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  static {
    try { // configure CXF logging
      System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Slf4jLogger");

      // configure log4j
      Properties p = new Properties();
      p.load(AbstractTest.class.getResourceAsStream("/log4j.properties"));
      PropertyConfigurator.configure(p);

      // configure JUL
      java.util.logging.LogManager.getLogManager().readConfiguration(AbstractTest.class.getResourceAsStream("/logging.properties"));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * trace each junit error
   */
  @Rule
  public MethodRule watch = new TestWatchman() {
    @Override
    public void failed(Throwable e, FrameworkMethod method) {
      super.failed(e, method);
      AbstractTest.this.logger.error(method.getName(), e);
    }

    @Override
    public void starting(FrameworkMethod method) {
      super.starting(method);

      AbstractTest.this.logTestClassContext(AbstractTest.this.getClass(), method);
    }
  };

  private <T> void logTestClassContext(Class<T> c, FrameworkMethod method) {
    this.logger.info("---------------------------------------------------------------");
    this.logger.info("test class:         " + c.getName());
    this.logger.info("test method:        " + method.getName());
    this.logger.info("---------------------------------------------------------------");
  }
}
