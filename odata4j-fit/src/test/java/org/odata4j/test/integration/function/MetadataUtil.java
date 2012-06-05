package org.odata4j.test.integration.function;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.format.xml.EdmxFormatWriter;
import org.odata4j.stax2.XMLInputFactory2;
import org.odata4j.stax2.staximpl.StaxXMLFactoryProvider2;

public class MetadataUtil {

  public static final String TEST_FUNCTION_RETURN_STRING = "TestFunctionReturnString";
  public static final String TEST_FUNCTION_RETURN_BOOLEAN = "TestFunctionReturnBoolean";
  public static final String TEST_FUNCTION_RETURN_EMPLOYEE = "TestFunctionReturnEmployee";
  public static final String TEST_FUNCTION_RETURN_COMPLEX_TYPE = "TestFunctionReturnComplexType";
  public static final String TEST_FUNCTION_RETURN_INT16 = "TestFunctionReturnInt16";
  public static final String TEST_FUNCTION_RETURN_COLLECTION_STRING = "TestFunctionReturnCollectionString";
  public static final String TEST_FUNCTION_RETURN_COLLECTION_COMPLEX_TYPE = "TestFunctionReturnCollectionComplexType";
  public static final String TEST_FUNCTION_RETURN_COLLECTION_EMPLOYEES = "TestFunctionReturnCollectionEmployees";

  private static final String REF_SCENARIO_EDMX = "/META-INF/FunctionImportScenario.edmx.xml";

  public static String readMetadataFromFile() {
    EdmDataServices eds = MetadataUtil.readMetadataServiceFromFile();

    StringWriter writer = new StringWriter();
    EdmxFormatWriter.write(eds, writer);

    return writer.toString();
  }

  public static EdmDataServices readMetadataServiceFromFile() {
    InputStream inputStream = FunctionImportProducerMock.class.getResourceAsStream(MetadataUtil.REF_SCENARIO_EDMX);
    Reader reader = new InputStreamReader(inputStream);

    XMLInputFactory2 inputFactory = StaxXMLFactoryProvider2.getInstance().newXMLInputFactory2();
    EdmxFormatParser parser = new EdmxFormatParser();
    EdmDataServices edmDataService = parser.parseMetadata(inputFactory.createXMLEventReader(reader));

    return edmDataService;
  }

}
