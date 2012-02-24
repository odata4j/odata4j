package org.odata4j.cxf.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.core4j.Enumerable;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.StringUtil;
import org.junit.Assert;
import org.odata4j.producer.server.ODataServer;

public class CxfTestServer implements ODataServer {

  //  private static final Logger LOGGER = LoggerFactory.getLogger(CxfTestServer.class);

  static {
    // ensure that the correct JAX-RS implementation is loaded
    RuntimeDelegate runtimeDelegate = new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl();
    RuntimeDelegate.setInstance(runtimeDelegate);
    Assert.assertEquals(runtimeDelegate, RuntimeDelegate.getInstance());
  }

  private Class<? extends Application> oDataApplication;
  //  private Class<? extends Application> rootApplication;
  private URI baseUri;

  private Server server;
  private final List<Handler> jettyRequestHandlers = new ArrayList<Handler>();

  public CxfTestServer(URI baseUri) {
    this.baseUri = baseUri;
  }

  @Override
  public ODataServer start() {
    try {
      String appName = this.oDataApplication.getCanonicalName();
      CXFNonSpringJaxrsServlet odataServlet = new CXFNonSpringJaxrsServlet();
      ServletHolder odataServletHolder = new ServletHolder(odataServlet);
      odataServletHolder.setInitParameter("javax.ws.rs.Application", appName);

      String path = this.getNormalizedPath(this.baseUri.getPath()) + "/*";
      ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
      contextHandler.addServlet(odataServletHolder, path);

      this.server = new Server(this.baseUri.getPort());
      this.server.setHandler(this.getHandlerCollection(contextHandler));

      this.server.setAttribute("org.eclipse.jetty.util.URI.charset", StringUtil.__UTF8);

      this.server.start();

      return this;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getNormalizedPath(String path) {
    if (path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    } else {
      return path;
    }
  }

  @Override
  public ODataServer stop() {
    try {
      this.server.stop();
      return this;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ODataServer setODataApplication(Class<? extends Application> odataApp) {
    this.oDataApplication = odataApp;
    return this;
  }

  @Override
  public ODataServer setRootApplication(Class<? extends Application> rootApp) {
    //    this.rootApplication = rootApp;
    return this;
  }

  private HandlerCollection getHandlerCollection(ServletContextHandler contextHandler) {
    List<Handler> handlers = this.jettyRequestHandlers;
    handlers.add(contextHandler);

    HandlerCollection handlerCollection = new HandlerCollection();
    handlerCollection.setHandlers(Enumerable.create(handlers).toArray(Handler.class));
    return handlerCollection;
  }

}
