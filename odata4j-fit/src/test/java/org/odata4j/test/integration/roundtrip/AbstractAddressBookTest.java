package org.odata4j.test.integration.roundtrip;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.eclipse.jetty.client.ContentExchange;
import org.junit.Test;
import org.odata4j.test.integration.AbstractJettyHttpClientTest;

public abstract class AbstractAddressBookTest extends AbstractJettyHttpClientTest {

  public AbstractAddressBookTest(RuntimeFacadeType type) {
    super(type);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void int16PropertyJson() throws Exception {
    ContentExchange exchange = sendRequest(BASE_URI + "Employees('2')/Age?$format=json");
    assertThat(exchange.getResponseContent(), allOf(containsString("\"Age\""), containsString("32")));
  }
}
