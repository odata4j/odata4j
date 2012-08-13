package org.odata4j.test.integration.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jetty.client.ContentExchange;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.test.integration.AbstractJettyHttpClientTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.format.xml.EdmxFormatWriter;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.ODataHeadersContext;
import org.odata4j.producer.QueryInfo;
import org.odata4j.test.integration.producer.custom.CustomEdm;
import org.odata4j.test.integration.producer.custom.CustomProducer;

/**
 * test for the new ODataContext producer parameter.
 */
public class ContextTest  extends AbstractJettyHttpClientTest {

  public ContextTest(RuntimeFacadeType type) {
    super(type);
  }
  
  @Override
  protected void registerODataProducer() throws Exception {
    DefaultODataProducerProvider.setInstance(mockProducer());
  }

  ODataProducer producer;
  protected ODataProducer mockProducer() {
    CustomProducer cp = new CustomProducer();
    producer = spy(cp); // mock(ODataProducer.class);
    // when(producer.getEntities(any(ODataContext.class), any(String.class), any(QueryInfo.class))).thenThrow(new NotImplementedException());
   
    return producer;
  }
  
  @BeforeClass
  public static void initClass() {
    myHeaders = new HashMap<String, List<String>>();
    myHeaders.put("X-Foo", Collections.singletonList("Bar"));
    List<String> cookies = new ArrayList<String>();
    cookies.add("Cookie 1");
    cookies.add("Cookie 2");
    myHeaders.put("Cookie", cookies);
  }
  
  private static Map<String, List<String>> myHeaders;
  private ArgumentCaptor<ODataContext> context;
  
  
  @Before
  public void initTest() {
    context = ArgumentCaptor.forClass(ODataContext.class);
  }
  
  @Rule
  public ExpectedException exception = ExpectedException.none();
  
  @Test
  public void testGetEntities() throws IOException, Exception {
    
    ContentExchange exchange = sendRequestWithHeaders(BASE_URI + "Directories", myHeaders);
    
    verify(producer).getEntities(context.capture(), eq("Directories"), any(QueryInfo.class));
    
    assertHeaders();
  }
  
  @Test
  public void testGetEntitiesCount() throws IOException, Exception {
    
    ContentExchange exchange = sendRequestWithHeaders(BASE_URI + "Directories/$count", myHeaders);
    
    verify(producer).getEntitiesCount(context.capture(), eq("Directories"), any(QueryInfo.class));
    
    assertHeaders();
  }
  
  @Test
  public void testGetEntity() throws IOException, Exception {
    
    ContentExchange exchange = sendRequestWithHeaders(BASE_URI + "Directories('Dir-0')", myHeaders);
    
    verify(producer).getEntity(context.capture(), eq("Directories"), any(OEntityKey.class), any(EntityQueryInfo.class));
    
    assertHeaders();
  }
  
  @Test
  public void testGetNavProperty() throws IOException, Exception {
    
    ContentExchange exchange = sendRequestWithHeaders(BASE_URI + "Directories('Dir-0')/Files", myHeaders);
    
    verify(producer).getNavProperty(context.capture(), eq("Directories"), any(OEntityKey.class), eq("Files"), any(QueryInfo.class));
    
    assertHeaders();
  }
  
  @Test
  public void testGetNavPropertyCount() throws IOException, Exception {
    
    ContentExchange exchange = sendRequestWithHeaders(BASE_URI + "Directories('Dir-0')/Files/$count", myHeaders);
    
    verify(producer).getNavPropertyCount(context.capture(), eq("Directories"), any(OEntityKey.class), eq("Files"), any(QueryInfo.class));
    
    assertHeaders();
  }
  
  // TODO: finish the rest of the ODataProducer methods....
  
  private void assertHeaders() {
    ODataHeadersContext got = context.getValue().getRequestHeadersContext();
    for (Entry<String, List<String>> e : myHeaders.entrySet()) {
      Iterable<String> gotVals = got.getRequestHeaderValues(e.getKey());
      int n = 0;
      String firstGotVal = null;
      for (String gotV : gotVals) {
        if (n == 0) { firstGotVal = gotV; }
        assertTrue(e.getValue().contains(gotV));
        n += 1;
      }
      assertEquals(e.getValue().size(), n);
      if (n == 1) {
        assertEquals(firstGotVal, got.getRequestHeaderValue(e.getKey()));
      }
    }
  }
  
  private ContentExchange sendRequestWithHeaders(String url, Map<String, List<String>> headers) throws IOException, InterruptedException {
    return sendRequestWithHeaders(url, headers, null, null);
  }
  
  private ContentExchange sendRequestWithHeaders(String url, Map<String, List<String>> headers, String method, String payload) throws IOException, InterruptedException {
    ContentExchange exchange = new ContentExchange(true);
    exchange.setURL(url);
    if (null != method) {
      exchange.setMethod(method);
    }
    if (null != payload) {
      exchange.setRequestContentSource(new ByteArrayInputStream(payload.getBytes()));
    }
    client.send(exchange);
    for (Entry<String, List<String>> e : headers.entrySet()) {
      for (String val : e.getValue()) {
        exchange.addRequestHeader(e.getKey(), val);
      }
    }
    exchange.waitForDone();
    return exchange;
  }
  
}
