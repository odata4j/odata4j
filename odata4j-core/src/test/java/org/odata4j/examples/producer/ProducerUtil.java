package org.odata4j.examples.producer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.producer.server.cxf.CxfJettyServer;
import org.odata4j.producer.server.jersey.JerseyServer;

import com.sun.jersey.api.container.filter.LoggingFilter;

public class ProducerUtil {

  public enum ServerType {
    JERSEY, CXF_JETTY
  }

  private static ServerType DEFAULT = ServerType.JERSEY;

  public static void hostODataServer(String baseUri, ServerType serverType) {
    ODataServer server = startODataServer(baseUri, serverType);
    System.out.println("Press any key to exit");
    readLine();
    server.stop();
  }

  public static ODataServer startODataServer(String baseUri, ServerType serverType) {
    return createODataServer(baseUri, serverType).start();
  }
  
  public static ODataServer createODataServer(String baseUri, ServerType serverType) {
    switch (serverType) {
    case JERSEY:
      return new JerseyServer(baseUri, DefaultODataApplication.class, RootApplication.class)
      .addJerseyRequestFilter(LoggingFilter.class) // log all requests
      // .addHttpServerFilter(new WhitelistFilter("127.0.0.1","0:0:0:0:0:0:0:1%0")) // only allow local requests
      ;
    case CXF_JETTY:
      return new CxfJettyServer(baseUri, DefaultODataApplication.class, RootApplication.class);
    default:
      return null;
    }
  }

  public static void hostODataServer(String baseUri) {
    hostODataServer(baseUri, DEFAULT);
  }

  public static ODataServer startODataServer(String baseUri) {
    return startODataServer(baseUri, DEFAULT);
  }

  public static ODataServer createODataServer(String baseUri) {
    return createODataServer(baseUri, DEFAULT);
  }

  public static void readLine() {
    try {
      new BufferedReader(new InputStreamReader(System.in)).readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
