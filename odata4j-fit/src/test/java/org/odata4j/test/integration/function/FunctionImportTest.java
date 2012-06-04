package org.odata4j.test.integration.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.integration.AbstractRuntimeTest;
import org.xml.sax.SAXException;

public class FunctionImportTest extends AbstractRuntimeTest {

  public FunctionImportTest(RuntimeFacadeType type) {
    super(type);
  }

  private static ArrayList<String> formats;
  static {
    FunctionImportTest.formats = new ArrayList<String>();
    FunctionImportTest.formats.add("$format=json");
    FunctionImportTest.formats.add("$format=atom");
  }

  private ODataServer server;

  private final static String endpointUri = "http://localhost:8810/FunctionImportScenario.svc/";

  private String metadata;

  FunctionImportProducerMock mockProducer;

  private static class TestCase {

    @Override
    public String toString() {
      return this.parameterName + "(" + this.type.getFullyQualifiedTypeName() + ")";
    }

    public TestCase(String parameterName, String valueLiteral, String valueString, EdmSimpleType<?> type) {
      super();
      this.parameterName = parameterName;
      this.valueLiteral = valueLiteral;
      this.valueString = valueString;
      this.type = type;
    }

    String parameterName;
    String valueLiteral;
    String valueString;
    EdmSimpleType<?> type;
  }

  public static ArrayList<TestCase> testCases;
  static {
    FunctionImportTest.testCases = new ArrayList<FunctionImportTest.TestCase>();
    FunctionImportTest.testCases.add(new TestCase("p1", "X'1F'", "0x1f", EdmSimpleType.BINARY));
    FunctionImportTest.testCases.add(new TestCase("p2", "true", "true", EdmSimpleType.BOOLEAN));
    FunctionImportTest.testCases.add(new TestCase("p3", "1", "1", EdmSimpleType.BYTE));
    FunctionImportTest.testCases.add(new TestCase("p4", "datetime'2010-12-12T23:44:57.123'", "2010-12-12T23:44:57.123", EdmSimpleType.DATETIME));
    FunctionImportTest.testCases.add(new TestCase("p5", "22.5m", "22.5", EdmSimpleType.DECIMAL));
    FunctionImportTest.testCases.add(new TestCase("p6", "1d", "1.0", EdmSimpleType.DOUBLE));
    FunctionImportTest.testCases.add(new TestCase("p7", "1f", "1.0", EdmSimpleType.SINGLE));
    FunctionImportTest.testCases.add(new TestCase("p8", "datetimeoffset'2012-12-12T22:07:44.123Z'", "2012-12-12T22:07:44.123Z", EdmSimpleType.DATETIMEOFFSET));
    FunctionImportTest.testCases.add(new TestCase("p9", "guid'11111111-1111-1111-1111-111111111111'", "11111111-1111-1111-1111-111111111111", EdmSimpleType.GUID));
    FunctionImportTest.testCases.add(new TestCase("p10", "1", "1", EdmSimpleType.INT16));
    FunctionImportTest.testCases.add(new TestCase("p11", "1", "1", EdmSimpleType.INT32));
    FunctionImportTest.testCases.add(new TestCase("p12", "1L", "1", EdmSimpleType.INT64));
    FunctionImportTest.testCases.add(new TestCase("p13", "1", "1", EdmSimpleType.SBYTE));
    FunctionImportTest.testCases.add(new TestCase("p14", "'hugo'", "hugo", EdmSimpleType.STRING));
    FunctionImportTest.testCases.add(new TestCase("p15", "time'PT10H30M'", "10:30:00.000", EdmSimpleType.TIME));
  }

  @Before
  public void before() {

    this.mockProducer = new FunctionImportProducerMock();

    DefaultODataProducerProvider.setInstance(this.mockProducer);
    this.server = this.rtFacade.startODataServer(FunctionImportTest.endpointUri);

    this.metadata = MetadataUtil.readMetadataFromFile();
  }

  @After
  public void after() {
    if (this.server != null) {
      this.server.stop();
    }
  }

  @Test
  public void callMetaData() throws SAXException, IOException, ParserConfigurationException {
    String metadata = this.rtFacade.getWebResource(endpointUri + "$metadata/");
    assertEquals(200, this.rtFacade.getLastStatusCode());
    assertEquals(this.metadata, metadata);
  }

  @Test
  public void tesFunctionReturnStringWithAllParameter() {
    String query = "?p1=X'1F'&p2=true&p3=1&p4=datetime'2010-12-12T23:44:57'&p5=22.5m&p6=1d&p7=1f&p8=datetimeoffset'2012-12-12T22:07:44Z'&p9=guid'11111111-1111-1111-1111-111111111111'&p10=1&p11=1&p12=1L&p13=1&p14='hugo'&p15=time'PT10H30M'";

    for (String format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_STRING + query + "&" + format);

      assertEquals(format, 200, this.rtFacade.getLastStatusCode());
      assertTrue(format, resource.contains(FunctionImportProducerMock.SOME_TEXT));

      assertNotNull(format, this.mockProducer.getQueryParameter());
    }
  }

  @Test
  public void tesFunctionReturnBoolean() {
    for (String format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_BOOLEAN + "?" + format);

      assertEquals(format, 200, this.rtFacade.getLastStatusCode());
      assertTrue(format, resource.contains(Boolean.toString(FunctionImportProducerMock.BOOLEAN_VALUE)));
      assertNotNull(format, this.mockProducer.getQueryParameter());
    }
  }

  @Test
  public void tesFunctionReturnString() {
    for (TestCase testCase : FunctionImportTest.testCases) {

      String query = "?" + testCase.parameterName + "=" + testCase.valueLiteral;

      for (String format : FunctionImportTest.formats) {
        String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_STRING + query + "&" + format);

        String msg = format + " | TestCase: " + testCase.toString();

        assertEquals(msg, 200, this.rtFacade.getLastStatusCode());
        assertTrue(msg, resource.contains(FunctionImportProducerMock.SOME_TEXT));

        assertNotNull(msg, this.mockProducer.getQueryParameter());

        assertTrue(msg, this.mockProducer.getQueryParameter().containsKey(testCase.parameterName));
        assertEquals(msg, testCase.parameterName, this.mockProducer.getQueryParameter().get(testCase.parameterName).getName());
        assertEquals(msg, testCase.type, this.mockProducer.getQueryParameter().get(testCase.parameterName).getType());
        assertEquals(msg, testCase.valueString, OSimpleObjects.getValueDisplayString(this.mockProducer.getQueryParameter().get(testCase.parameterName).getValue()));
      }
    }
  }

  @Test
  public void tesFunctionReturnStringWithNoQueryParameter() {
    for (String format : FunctionImportTest.formats) {

      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_STRING + "?" + format);

      assertEquals(format, 200, this.rtFacade.getLastStatusCode());
      assertTrue(format, resource.contains(FunctionImportProducerMock.SOME_TEXT));

      assertNotNull(format, this.mockProducer.getQueryParameter());

      assertFalse(format, this.mockProducer.getQueryParameter().containsKey("p0"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p1"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p2"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p3"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p4"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p5"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p6"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p7"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p8"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p9"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p10"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p11"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p12"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p13"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p14"));
      assertTrue(format, this.mockProducer.getQueryParameter().containsKey("p15"));
      assertFalse(format, this.mockProducer.getQueryParameter().containsKey("p16"));
    }
  }

  @Test
  public void testFunctionReturnEmployee() {
    for (String format : FunctionImportTest.formats) {

      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_EMPLOYEE + "?" + format);
      this.logger.debug(resource);

      assertTrue(format, resource.contains(FunctionImportProducerMock.EMPLOYEE_NAME));
      assertTrue(format, resource.contains(FunctionImportProducerMock.EMPLOYEE_ID));
    }
  }

  @Ignore
  @Test
  public void testFunctionReturnComplexType() {
    for (String format : FunctionImportTest.formats) {

      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_COMPLEX_TYPE + "?" + format);
      this.logger.debug(resource);

      assertTrue(format, resource.contains(FunctionImportProducerMock.CITY));
      assertTrue(format, resource.contains(FunctionImportProducerMock.COUNTRY));
      assertTrue(format, resource.contains(FunctionImportProducerMock.POSTAL_CODE));
    }
  }

}
