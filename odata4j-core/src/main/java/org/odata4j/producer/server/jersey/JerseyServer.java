package org.odata4j.producer.server.jersey;

import static com.sun.jersey.api.core.ResourceConfig.FEATURE_TRACE;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.core4j.CoreUtils;
import org.core4j.Enumerable;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * OData server using the Jersey JAX-RS and Sun's HTTP server implementation.
 */
public class JerseyServer implements ODataServer {

  private static final Logger LOG = Logger.getLogger(JerseyServer.class.getName());

  private final String appBaseUri;
  private Class<? extends Application> odataApp;
  private Class<? extends Application> rootApp;
  private final List<String> jerseyRequestFilters = new ArrayList<String>();
  private final List<String> jerseyResponseFilters = new ArrayList<String>();
  private final List<String> jerseyResourceFilters = new ArrayList<String>();
  private final Map<String, Boolean> jerseyFeatures = new HashMap<String, Boolean>();
  private final List<Filter> httpServerFilters = new ArrayList<Filter>();
  private Authenticator httpServerAuthenticator;

  private HttpServer server;

  public JerseyServer(String appBaseUri) {
    this.appBaseUri = appBaseUri;
  }

  public JerseyServer(String appBaseUri, Class<? extends Application> odataApp, Class<? extends Application> rootApp) {
    this.appBaseUri = appBaseUri;
    this.odataApp = odataApp;
    this.rootApp = rootApp;
  }

  @Override
  public ODataServer setODataApplication(Class<? extends Application> odataApp) {
    this.odataApp = odataApp;
    return this;
  }

  @Override
  public ODataServer setRootApplication(Class<? extends Application> rootApp) {
    this.rootApp = rootApp;
    return this;
  }

  public <T extends ContainerRequestFilter> JerseyServer addJerseyRequestFilter(Class<T> filter) {
    jerseyRequestFilters.add(filter.getName());
    return this;
  }

  public <T extends ContainerResponseFilter> JerseyServer addJerseyResponseFilter(Class<T> filter) {
    jerseyResponseFilters.add(filter.getName());
    return this;
  }

  public <T extends ResourceFilterFactory> JerseyServer addJerseyResourceFilter(Class<T> filter) {
    jerseyResourceFilters.add(filter.getName());
    return this;
  }

  /** Enabling this feature can be useful in tracking down issues related to selecting the resource class */
  public JerseyServer setJerseyTrace(boolean enabled) {
    return setJerseyFeature(FEATURE_TRACE, enabled);
  }

  public JerseyServer setJerseyFeature(String feature, boolean value) {
    jerseyFeatures.put(feature, value);
    return this;
  }

  public JerseyServer addHttpServerFilter(Filter filter) {
    httpServerFilters.add(filter);
    return this;
  }

  public JerseyServer setHttpServerAuthenticator(Authenticator authenticator) {
    httpServerAuthenticator = authenticator;
    return this;
  }

  @Override
  public ODataServer stop() {
    return stop(0);
  }
  
  /**
   * stop synchronously, handy for unit test scenarios.
   * @param delaySeconds
   * @return 
   */
  public JerseyServer stop(int delaySeconds) {
    server.stop(delaySeconds);
    Executor serverExecutor = server.getExecutor();
    if (serverExecutor instanceof ThreadPoolExecutor) {
      ((ThreadPoolExecutor) serverExecutor).shutdown();
      if (delaySeconds > 0) {
        try {
          ((ThreadPoolExecutor) serverExecutor).awaitTermination(delaySeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
          // oh well..
        }
      }
    }
    
    return this;
  }

  @Override
  public ODataServer start() {
    if (odataApp == null)
      throw new RuntimeException("ODataApplication not set");

    try {
      // create resourceConfig for app context
      server = HttpServerFactory.create(appBaseUri, new ApplicationAdapter(odataApp.newInstance()));

      // create resourceConfig for root context (if necessary)
      if (rootApp != null) {
        HttpHandler rootHttpHandler = ContainerFactory.createContainer(HttpHandler.class, new ApplicationAdapter(rootApp.newInstance()));
        server.createContext("/", rootHttpHandler);
      }

      // initialize all contexts
      for (HttpContext context : getHttpContexts())
        initHttpContext(context);

      // fire up the HttpServer
      server.start();

      LOG.info(String.format("Jersey app started with WADL available at %sapplication.wadl\n", appBaseUri));
      return this;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  protected HttpServer getHttpServer() {
    return server;
  }

  protected ResourceConfig buildResourceConfig(List<?> resourceClasses) {
    DefaultResourceConfig resourceConfig = new DefaultResourceConfig(
        Enumerable.create(resourceClasses)
            .cast(Object.class)
            .cast(Class.class)
            .toArray(Class.class));

    resourceConfig.setPropertiesAndFeatures(buildPropertiesAndFeatures());
    return resourceConfig;
  }

  protected Map<String, Object> buildPropertiesAndFeatures() {
    Map<String, Object> propertiesAndFeatures = new HashMap<String, Object>();
    propertiesAndFeatures.put(PROPERTY_CONTAINER_REQUEST_FILTERS, Enumerable.create(jerseyRequestFilters).toArray(String.class));
    propertiesAndFeatures.put(PROPERTY_CONTAINER_RESPONSE_FILTERS, Enumerable.create(jerseyResponseFilters).toArray(String.class));
    propertiesAndFeatures.put(PROPERTY_RESOURCE_FILTER_FACTORIES, Enumerable.create(jerseyResourceFilters).toArray(String.class));
    propertiesAndFeatures.putAll(jerseyFeatures);
    return propertiesAndFeatures;
  }

  protected void initHttpContext(HttpContext context) {
    context.getFilters().addAll(httpServerFilters);
    if (httpServerAuthenticator != null)
      context.setAuthenticator(httpServerAuthenticator);
  }

  @SuppressWarnings("unchecked")
  protected Iterable<HttpContext> getHttpContexts() {
    // would love to know if there is another way to do this...
    Object tmp = CoreUtils.getFieldValue(server, "server", Object.class);
    tmp = CoreUtils.getFieldValue(tmp, "contexts", Object.class);
    tmp = CoreUtils.getFieldValue(tmp, "list", Object.class);
    return (List<HttpContext>) tmp;
  }
}
