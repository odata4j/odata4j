package org.odata4j.test.integration.function;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatType;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.integration.AbstractRuntimeTest;
import org.xml.sax.SAXException;

public class FunctionImportTest extends AbstractRuntimeTest {

  public FunctionImportTest(RuntimeFacadeType type) {
    super(type);
  }

  private static ArrayList<FormatType> formats;
  static {
    FunctionImportTest.formats = new ArrayList<FormatType>();
    FunctionImportTest.formats.add(FormatType.JSON);
    FunctionImportTest.formats.add(FormatType.ATOM);
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

  private String formatQuery(FormatType type) {
    String query;

    switch (type) {
    case ATOM:
      query = "$format=atom";
      break;
    case JSON:
      query = "$format=json";
      break;
    default:
      throw new RuntimeException("Unknown Format Type: " + type);
    }

    return query;
  }

  private void initializeXmlUnit() {
    HashMap<String, String> m = new HashMap<String, String>();
    m.put("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
    m.put("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
    m.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    m.put("", "http://www.w3.org/2005/Atom");

    NamespaceContext ctx = new SimpleNamespaceContext(m);
    XMLUnit.setXpathNamespaceContext(ctx);
    XpathEngine engine = XMLUnit.newXpathEngine();
    engine.setNamespaceContext(ctx);
  }

  @Before
  public void before() {

    this.initializeXmlUnit();

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
    String metadataByService = this.rtFacade.getWebResource(endpointUri + "$metadata/");
    assertEquals(200, this.rtFacade.getLastStatusCode());
    assertXMLEqual(this.metadata, metadataByService);
  }

  @Test
  public void tesFunctionReturnStringWithAllParameter() throws XpathException, SAXException, IOException {
    String query = "?p1=X'1F'&p2=true&p3=1&p4=datetime'2010-12-12T23:44:57'&p5=22.5m&p6=1d&p7=1f&p8=datetimeoffset'2012-12-12T22:07:44Z'&p9=guid'11111111-1111-1111-1111-111111111111'&p10=1&p11=1&p12=1L&p13=1&p14='hugo'&p15=time'PT10H30M'";

    for (FormatType format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_STRING + query + "&" + this.formatQuery(format));

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());
      assertNotNull(format.toString(), this.mockProducer.getQueryParameter());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnString", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.SOME_TEXT, "/d:TestFunctionReturnString/text()", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(MetadataUtil.TEST_FUNCTION_RETURN_STRING));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.SOME_TEXT));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }

    }
  }

  @Test
  public void tesFunctionReturnBoolean() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_BOOLEAN + "?" + this.formatQuery(format));

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());
      assertNotNull(format.toString(), this.mockProducer.getQueryParameter());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnBoolean", resource);
        assertXpathEvaluatesTo(Boolean.toString(FunctionImportProducerMock.BOOLEAN_VALUE), "/d:TestFunctionReturnBoolean/text()", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(MetadataUtil.TEST_FUNCTION_RETURN_BOOLEAN));
        assertTrue(format.toString(), resource.contains(Boolean.toString(FunctionImportProducerMock.BOOLEAN_VALUE)));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void tesFunctionReturnString() throws XpathException, IOException, SAXException {
    for (TestCase testCase : FunctionImportTest.testCases) {

      String query = "?" + testCase.parameterName + "=" + testCase.valueLiteral;

      for (FormatType format : FunctionImportTest.formats) {
        String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_STRING + query + "&" + this.formatQuery(format));

        String msg = format + " | TestCase: " + testCase.toString();

        assertEquals(msg, 200, this.rtFacade.getLastStatusCode());
        assertNotNull(msg, this.mockProducer.getQueryParameter());
        assertTrue(msg, this.mockProducer.getQueryParameter().containsKey(testCase.parameterName));

        assertEquals(msg, testCase.parameterName, this.mockProducer.getQueryParameter().get(testCase.parameterName).getName());
        assertEquals(msg, testCase.type, this.mockProducer.getQueryParameter().get(testCase.parameterName).getType());
        assertEquals(msg, testCase.valueString, OSimpleObjects.getValueDisplayString(this.mockProducer.getQueryParameter().get(testCase.parameterName).getValue()));

        switch (format) {
        case ATOM:
          assertXpathExists("/d:TestFunctionReturnString", resource);
          assertXpathEvaluatesTo(FunctionImportProducerMock.SOME_TEXT, "/d:TestFunctionReturnString/text()", resource);
          break;
        case JSON:
          assertTrue(format.toString(), resource.contains(MetadataUtil.TEST_FUNCTION_RETURN_STRING));
          assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.SOME_TEXT));
          break;
        default:
          throw new RuntimeException("Unknown Format Type: " + format);
        }
      }
    }
  }

  @Test
  public void tesFunctionReturnInt16() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_INT16 + "?" + this.formatQuery(format));

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());
      assertNotNull(format.toString(), this.mockProducer.getQueryParameter());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnInt16", resource);
        assertXpathEvaluatesTo(Integer.toString(FunctionImportProducerMock.INT16_VALUE), "/d:TestFunctionReturnInt16/text()", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(MetadataUtil.TEST_FUNCTION_RETURN_INT16));
        assertTrue(format.toString(), resource.contains(Integer.toString(FunctionImportProducerMock.INT16_VALUE)));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void tesFunctionReturnStringWithNoQueryParameter() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {

      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_STRING + "?" + this.formatQuery(format));

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());

      assertNotNull(format.toString(), this.mockProducer.getQueryParameter());

      assertFalse(format.toString(), this.mockProducer.getQueryParameter().containsKey("p0"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p1"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p2"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p3"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p4"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p5"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p6"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p7"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p8"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p9"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p10"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p11"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p12"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p13"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p14"));
      assertTrue(format.toString(), this.mockProducer.getQueryParameter().containsKey("p15"));
      assertFalse(format.toString(), this.mockProducer.getQueryParameter().containsKey("p16"));

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnString", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.SOME_TEXT, "/d:TestFunctionReturnString/text()", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(MetadataUtil.TEST_FUNCTION_RETURN_STRING));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.SOME_TEXT));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }

    }
  }

  @Test
  @Ignore
  public void XmlUnitIssue() throws XpathException, IOException, SAXException {
    /*
     * TODO
     * enable this test case with a new version (> 1.3) of XMLUnit
     * - 1.3 has an issue with default namespace (here atom)
     */
    String xml = "<entry xml:base='http://localhost:8811/FunctionImportScenario.svc/' xmlns='http://www.w3.org/2005/Atom' ></entry>";
    assertXpathExists("/entry", xml);
  }

  @Test
  public void testFunctionReturnEntity() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {

      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_ENTITY + "?" + this.formatQuery(format));
      this.logger.debug(resource);

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());

      switch (format) {
      case ATOM:

        /*
         * TODO
         * BUG in XMLUNnit (1.3): Built in XPath engine does not handle default namespace of atom correctly
         * 
         *   assertXpathEvaluatesTo("RefScenario.Employee", "/entry/category/@term", resource);
         *   assertXpathEvaluatesTo(FunctionImportProducerMock.EMPLOYEE_ID, "/entry/content/m:properties/d:EmployeeName/text()", resource);
         *   assertXpathEvaluatesTo(FunctionImportProducerMock.EMPLOYEE_NAME, "/entry/content/m:properties/d:EmployeeId/text()", resource);
         */

        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_NAME));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_ID));
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_NAME));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_ID));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void testFunctionReturnComplexType() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {

      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_COMPLEX_TYPE + "?" + this.formatQuery(format));
      this.logger.debug(resource);

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnComplexType", resource);
        assertXpathEvaluatesTo("RefScenario.c_Location", "/d:TestFunctionReturnComplexType/@m:type", resource);
        assertXpathEvaluatesTo("RefScenario.c_City", "/d:TestFunctionReturnComplexType/d:City/@m:type", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.POSTAL_CODE, "/d:TestFunctionReturnComplexType/d:City/d:PostalCode/text()", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.CITY, "/d:TestFunctionReturnComplexType/d:City/d:CityName/text()", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.COUNTRY, "/d:TestFunctionReturnComplexType/d:Country/text()", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(MetadataUtil.TEST_FUNCTION_RETURN_COMPLEX_TYPE));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.CITY));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.COUNTRY));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.POSTAL_CODE));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void testFunctionReturnCollectionString() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_COLLECTION_STRING + "?" + this.formatQuery(format));
      this.logger.debug(resource);

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnCollectionString", resource);
        assertXpathNotExists("/d:TestFunctionReturnCollectionString/d:element/@m:type", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.COLLECTION_STRING1, "/d:TestFunctionReturnCollectionString/d:element[1]/text()", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.COLLECTION_STRING2, "/d:TestFunctionReturnCollectionString/d:element[2]/text()", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.COLLECTION_STRING1));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.COLLECTION_STRING2));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void testFunctionReturnCollectionDouble() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_COLLECTION_DOUBLE + "?" + this.formatQuery(format));
      this.logger.debug(resource);

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnCollectionDouble", resource);
        assertXpathNotExists("/d:TestFunctionReturnCollectionDouble/d:element/@m:type", resource);
        assertXpathEvaluatesTo(Double.toString(FunctionImportProducerMock.COLLECTION_DOUBLE1), "/d:TestFunctionReturnCollectionDouble/d:element[1]/text()", resource);
        assertXpathEvaluatesTo(Double.toString(FunctionImportProducerMock.COLLECTION_DOUBLE2), "/d:TestFunctionReturnCollectionDouble/d:element[2]/text()", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(Double.toString(FunctionImportProducerMock.COLLECTION_DOUBLE1)));
        assertTrue(format.toString(), resource.contains(Double.toString(FunctionImportProducerMock.COLLECTION_DOUBLE2)));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void testFunctionReturnCollectionComplexType() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_COLLECTION_COMPLEX_TYPE + "?" + this.formatQuery(format));
      this.logger.debug(resource);

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:TestFunctionReturnCollectionComplexType", resource);
        assertXpathEvaluatesTo("RefScenario.c_Location", "/d:TestFunctionReturnCollectionComplexType/d:element/@m:type", resource);
        assertXpathEvaluatesTo("RefScenario.c_City", "/d:TestFunctionReturnCollectionComplexType/d:element[1]/d:City/@m:type", resource);
        assertXpathEvaluatesTo("RefScenario.c_City", "/d:TestFunctionReturnCollectionComplexType/d:element[1]/d:City/@m:type", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.CITY, "/d:TestFunctionReturnCollectionComplexType/d:element[1]/d:City/d:CityName/text()", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.CITY, "/d:TestFunctionReturnCollectionComplexType/d:element[2]/d:City/d:CityName/text()", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.POSTAL_CODE, "/d:TestFunctionReturnCollectionComplexType/d:element[1]/d:City/d:PostalCode/text()", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.POSTAL_CODE, "/d:TestFunctionReturnCollectionComplexType/d:element[2]/d:City/d:PostalCode/text()", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.COUNTRY, "/d:TestFunctionReturnCollectionComplexType/d:element[1]/d:Country", resource);
        assertXpathEvaluatesTo(FunctionImportProducerMock.COUNTRY, "/d:TestFunctionReturnCollectionComplexType/d:element[2]/d:Country", resource);
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.COUNTRY));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.POSTAL_CODE));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.CITY));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void testFunctionReturnCollectionEntityType() throws XpathException, IOException, SAXException {
    for (FormatType format : FunctionImportTest.formats) {
      String resource = this.rtFacade.getWebResource(endpointUri + MetadataUtil.TEST_FUNCTION_RETURN_COLLECTION_ENTITY + "?" + this.formatQuery(format));
      this.logger.debug(resource);

      assertEquals(format.toString(), 200, this.rtFacade.getLastStatusCode());

      switch (format) {
      case ATOM:

        /*
         * TODO
         * BUG in XMLUNnit (1.3): Built in XPath engine does not handle default namespace of atom correctly
         * 
         *  assertXpathExists("/feed", resource);
         *  assertXpathEvaluatesTo("RefScenario.Employee", "/feed/entry/category/@term", resource);
         *  assertXpathEvaluatesTo(FunctionImportProducerMock.EMPLOYEE_ID, "/feed/entry/content/m:properties/d:EmployeeId/text()", resource);
         *  assertXpathEvaluatesTo(FunctionImportProducerMock.EMPLOYEE_NAME, "/feed/entry/content/m:properties/d:EmployeeName/text()", resource);
         */

        assertTrue(format.toString(), resource.contains("<feed"));
        assertTrue(format.toString(), resource.contains("Employees"));
               assertTrue(format.toString(), resource.contains("RefScenario.Employee"));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_NAME));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_ID));

        break;
      case JSON:
        assertTrue(format.toString(), resource.contains("\"results\" : ["));
        assertTrue(format.toString(), resource.contains("\"__metadata\" : {"));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_NAME));
        assertTrue(format.toString(), resource.contains(FunctionImportProducerMock.EMPLOYEE_ID));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

}
