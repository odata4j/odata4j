package org.odata4j.producer.server;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.core4j.CoreUtils;
import org.core4j.Enumerable;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
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
import java.util.concurrent.TimeUnit;

public class JerseyServer {

  private static final Logger LOG = Logger.getLogger(JerseyServer.class.getName());

  private final String appBaseUri;
  private final List<Class<?>> appResourceClasses = new ArrayList<Class<?>>();
  private final List<Class<?>> rootResourceClasses = new ArrayList<Class<?>>();
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

  public JerseyServer addAppResourceClass(Class<?> resourceClass) {
    appResourceClasses.add(resourceClass);
    return this;
  }

  public JerseyServer addAppResourceClasses(Iterable<Class<?>> resourceClasses) {
    for (Class<?> clazz : resourceClasses)
      appResourceClasses.add(clazz);
    return this;
  }

  public JerseyServer addRootResourceClass(Class<?> resourceClass) {
    rootResourceClasses.add(resourceClass);
    return this;
  }

  public JerseyServer addRootResourceClasses(Iterable<Class<?>> resourceClasses) {
    for (Class<?> resourceClass : resourceClasses)
      rootResourceClasses.add(resourceClass);
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

  public JerseyServer stop() {
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

  public JerseyServer start() {
    try {
      // create resourceConfig for app context
      ResourceConfig appResourceConfig = buildResourceConfig(appResourceClasses);
      server = HttpServerFactory.create(appBaseUri, appResourceConfig);

      // create resourceConfig for root context (if necessary)
      if (!rootResourceClasses.isEmpty()) {
        ResourceConfig rootResourceConfig = buildResourceConfig(rootResourceClasses);
        HttpHandler rootHttpHandler = ContainerFactory.createContainer(HttpHandler.class, rootResourceConfig);
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
