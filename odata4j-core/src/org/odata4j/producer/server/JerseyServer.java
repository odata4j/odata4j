package org.odata4j.producer.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import core4j.CoreUtils;
import core4j.Enumerable;

public class JerseyServer {

    private static final Logger log = Logger.getLogger(JerseyServer.class.getName());

    private HttpServer server;
    private final String appBaseUri;
    private final List<Filter> httpServerFilters = new ArrayList<Filter>();
    private final List<String> jerseyRequestFilters = new ArrayList<String>();
    private final List<String> jerseyResponseFilters = new ArrayList<String>();
    private final List<String> jerseyResourceFilters = new ArrayList<String>();
    private final List<Class<?>> appResourceClasses = new ArrayList<Class<?>>();
    private final List<Class<?>> rootResourceClasses = new ArrayList<Class<?>>();

    public JerseyServer(String appBaseUri) {
        this.appBaseUri = appBaseUri;
    }

    public void addAppResourceClass(Class<?> clazz) {
        appResourceClasses.add(clazz);
    }

    public void addAppResourceClasses(Iterable<Class<?>> classes) {
        for(Class<?> clazz : classes)
            appResourceClasses.add(clazz);
    }

    public void addRootResourceClass(Class<?> clazz) {
        rootResourceClasses.add(clazz);
    }

    public void addRootResourceClasses(Iterable<Class<?>> classes) {
        for(Class<?> clazz : classes)
            rootResourceClasses.add(clazz);
    }

    public <T extends ContainerRequestFilter> void addJerseyRequestFilter(Class<T> filter) {
        jerseyRequestFilters.add(filter.getName());
    }

    public <T extends ContainerResponseFilter> void addJerseyResponseFilter(Class<T> filter) {
        jerseyResponseFilters.add(filter.getName());
    }

    public <T extends ResourceFilterFactory> void addJerseyResourceFilter(Class<T> filter) {
        jerseyResourceFilters.add(filter.getName());
    }

    public void addHttpServerFilter(Filter filter) {
        httpServerFilters.add(filter);

    }

    public void stop() {
        server.stop(0);
    }

    @SuppressWarnings("unchecked")
    public void start() {

        try {
            ResourceConfig appResourceConfig = buildConfig(this.appResourceClasses);
            server = HttpServerFactory.create(appBaseUri, appResourceConfig);

            if (this.rootResourceClasses.size() > 0) {
                ResourceConfig rootResourceConfig = buildConfig(this.rootResourceClasses);
                HttpHandler rootHttpHandler = ContainerFactory.createContainer(HttpHandler.class, rootResourceConfig);
                server.createContext("/", rootHttpHandler);
            }

            // add httpserver filters to all contexts
            Object tmp = CoreUtils.getFieldValue(server, "server", Object.class);
            tmp = CoreUtils.getFieldValue(tmp, "contexts", Object.class);
            tmp = CoreUtils.getFieldValue(tmp, "list", Object.class);
            for(HttpContext context : ((List<HttpContext>) tmp))
                context.getFilters().addAll(httpServerFilters);

            server.start();

            log.info(String.format("Jersey app started with WADL available at %sapplication.wadl\n", appBaseUri));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private ResourceConfig buildConfig(List<?> classes) {
        DefaultResourceConfig c = new DefaultResourceConfig(Enumerable.create(classes).cast(Object.class).cast(Class.class).toArray(Class.class));

        Map<String, Object> paf = new HashMap<String, Object>();
        paf.put("com.sun.jersey.spi.container.ContainerRequestFilters", Enumerable.create(jerseyRequestFilters).toArray(String.class));
        paf.put("com.sun.jersey.spi.container.ContainerResponseFilters", Enumerable.create(jerseyResponseFilters).toArray(String.class));
        paf.put("com.sun.jersey.spi.container.ResourceFilters", Enumerable.create(jerseyResourceFilters).toArray(String.class));
        c.setPropertiesAndFeatures(paf);
        return c;
    }
}
