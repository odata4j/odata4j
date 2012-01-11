package org.odata4j.jersey.examples.producer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.odata4j.jersey.producer.server.JerseyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.container.filter.LoggingFilter;

public class JerseyProducerUtil {

  public static void hostODataServer(String baseUri) {
    try {
      ODataServer server = startODataServer(baseUri);
      System.out.println("Press any key to exit");
      new BufferedReader(new InputStreamReader(System.in)).readLine();
      server.stop();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static ODataServer startODataServer(String baseUri) {
    return createODataServer(baseUri).start();
  }

  private static ODataServer createODataServer(String baseUri) {
    return new JerseyServer(baseUri, DefaultODataApplication.class, RootApplication.class)
        .addJerseyRequestFilter(LoggingFilter.class) // log all requests
    //      .addHttpServerFilter(new WhitelistFilter("127.0.0.1","0:0:0:0:0:0:0:1%0")) // only allow local requests
    ;
  }

}
