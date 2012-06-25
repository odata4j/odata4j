package org.odata4j.test.integration.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.test.integration.AbstractJettyHttpClientTest;
import org.odata4j.test.integration.TestInMemoryProducers;

public class ErrorTest extends AbstractJettyHttpClientTest {

  private static final String FEED_URL = BASE_URI + TestInMemoryProducers.SIMPLE_ENTITY_SET_NAME;

  public ErrorTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void registerODataProducer() throws Exception {
    DefaultODataProducerProvider.setInstance(TestInMemoryProducers.simple());
  }

  @Test
  public void notFoundXml() throws Exception {
    ContentExchange exchange = sendRequest(FEED_URL + "('Z')");
    exchange.waitForDone();
    assertThat(exchange.getStatus(), is(HttpExchange.STATUS_COMPLETED));
    assertThat(exchange.getResponseStatus(), is(HttpStatus.NOT_FOUND_404));
    assertThat(exchange.getResponseFields().getStringField(HttpHeaders.CONTENT_TYPE), containsString(MediaType.APPLICATION_XML));
    assertThat(exchange.getResponseContent().length(), greaterThan(0));
    assertTrue(Pattern.compile(".*<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\\s*"
        + "<code>.+</code>\\s*"
        + "<message lang=\".+\">.+</message>\\s*"
        + "(?:<innererror>.+</innererror>)?\\s*"
        + "</error>\\s*", Pattern.DOTALL)
        .matcher(exchange.getResponseContent()).matches());
  }

  @Test
  public void notFoundJson() throws Exception {
    ContentExchange exchange = sendRequest(FEED_URL + "('Z')?$format=json");
    exchange.waitForDone();
    assertThat(exchange.getStatus(), is(HttpExchange.STATUS_COMPLETED));
    assertThat(exchange.getResponseStatus(), is(HttpStatus.NOT_FOUND_404));
    assertThat(exchange.getResponseFields().getStringField(HttpHeaders.CONTENT_TYPE), containsString(MediaType.APPLICATION_JSON));
    assertThat(exchange.getResponseContent().length(), greaterThan(0));
    assertTrue(Pattern.compile("\\{\\s*\"error\"\\s*:\\s*\\{\\s*"
        + "\"code\"\\s*:\\s*\".+\"\\s*,\\s*"
        + "\"message\"\\s*:\\s*\\{\\s*"
        + "\"lang\"\\s*:\\s*\".+\"\\s*,\\s*"
        + "\"value\"\\s*:\\s*\".+\"\\s*"
        + "\\}\\s*"
        + "(?:,\\s*\"innererror\"\\s*:\\s*\".+\"\\s*)?"
        + "\\}\\s*"
        + "\\}", Pattern.DOTALL)
        .matcher(exchange.getResponseContent()).matches());
  }
}
