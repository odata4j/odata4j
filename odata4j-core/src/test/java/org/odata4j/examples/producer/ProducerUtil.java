package org.odata4j.examples.producer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.odata4j.producer.resources.ODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.JerseyServer;

import com.sun.jersey.api.container.filter.LoggingFilter;

public class ProducerUtil {

  public static void hostODataServer(String baseUri) {
    JerseyServer server = startODataServer(baseUri);
    System.out.println("Press any key to exit");
    readLine();
    server.stop();
  }

  public static JerseyServer createODataServer(String baseUri) {
    return new JerseyServer(baseUri)
        .addAppResourceClasses(new ODataApplication().getClasses())
        .addRootResourceClasses(new RootApplication().getClasses())
        .addJerseyRequestFilter(LoggingFilter.class) // log all requests
//      .addHttpServerFilter(new WhitelistFilter("127.0.0.1","0:0:0:0:0:0:0:1%0")) // only allow local requests
        ;
  }

  public static JerseyServer startODataServer(String baseUri) {
    return createODataServer(baseUri).start();
  }

  public static void readLine() {
    try {
      new BufferedReader(new InputStreamReader(System.in)).readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
