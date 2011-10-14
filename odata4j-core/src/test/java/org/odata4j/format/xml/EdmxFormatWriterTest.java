
package org.odata4j.format.xml;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.core4j.Func;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.core.Annotation;
import org.odata4j.core.Namespace;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmAnnotation;
import org.odata4j.edm.EdmAnnotationAttribute;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmDecorator;
import org.odata4j.edm.EdmDocumentation;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmItem;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmStructuralType;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.Path;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.jpa.airline.Airport;
import org.odata4j.producer.jpa.northwind.test.NorthwindTestUtils;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;



/**
 * a simple test for writing annotations and documentation in the edmx.
 *
 * This test also demonstrates the use of {@link EdmDecorator}.
 */
public class EdmxFormatWriterTest implements EdmDecorator {

  public EdmxFormatWriterTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  private JerseyServer server = null;
  @SuppressWarnings("unused")
  private EdmDataServices ds = null;

  @Before
  public void setUp() {
    ds = buildModel();
  }

  @After
  public void tearDown() {
    if (null != server) {
      server.stop();
    }
  }

  @Ignore("Re-enable when it passes")
  @Test
  public void testExtensions() throws InterruptedException, SAXException, IOException {
    // test to see that documentation and annotations get written correctly

    WebResource webResource = new Client().resource(endpointUri + "$metadata");

    String metadata = webResource.get(
            String.class);

    //System.out.println(metadata);

    String expected = NorthwindTestUtils.readFileToString("/META-INF/uri-conventions/xml/DocAnnotTest.xml");

    XMLUnit.setIgnoreWhitespace(true);
    Diff myDiff = new Diff(expected, metadata);
    myDiff.overrideElementQualifier(new ElementNameAndTextQualifier());

    assertXMLEqual("bad $metadata", myDiff, true);

    EdmDataServices pds = new EdmxFormatParser().parseMetadata(InternalUtil.newXMLEventReader(new StringReader(metadata)));
    Assert.assertTrue(null != pds); // we parsed it!

    // TODO: once EdmxFormatParser supports doc and annotations we can check pds
    // for the expected objects.

    // TODO: once we support doc and annots for all EdmItem types we should
    // include them.

    // also, this test doesn't ensure the placement of annotations and documenation in the edmx,
    // for example, Documentation is always the first sub-element.
  }

  public static final String endpointUri = "http://localhost:8887/flights.svc/";

  private EdmDataServices buildModel() {

    InMemoryProducer p = new InMemoryProducer("flights", 50, this);

    final List<Airport> airports = new ArrayList<Airport>();
    Airport denver = new Airport();
    denver.setCode("DIA");
    denver.setCountry("USA");
    denver.setName("Denver International Airport");
    airports.add(denver);

    Airport honolulu = new Airport();
    honolulu.setCode("HNL");
    honolulu.setCountry("USA");
    honolulu.setName("Honolulu International Airport");
    airports.add(honolulu);

    p.register(Airport.class, String.class, "Airports", new Func<Iterable<Airport>>() {

      @Override
      public Iterable<Airport> apply() {
        return airports;
      }

    }, "Code");

     // register the producer as the static instance, then launch the http server
    ODataProducerProvider.setInstance(p);
    server = ProducerUtil.startODataServer(endpointUri);
    return p.getMetadata();
  }

  public static final String namespaceUri = "http://tempuri.org/test";
  public static final String prefix = "od4j";

  @Override
  public List<Namespace> getNamespaces() {
    List<Namespace> l = new ArrayList<Namespace>();
    l.add(new Namespace(namespaceUri, prefix));
    return l;
  }

  @Override
  public EdmDocumentation getDocumentationForEntityType(String namespace, String typeName) {
    if (typeName.equals("Airports")) {
      return new EdmDocumentation("This is the summary documentation for Airport",
              "This is the long description for Airport.");
    }
    return null;
  }

  @Override
  public Object resolveStructuralTypeProperty(EdmStructuralType st, Path path) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object resolvePropertyProperty(EdmProperty st, Path path) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object getAnnotationValueOverride(EdmItem item, Annotation<?> annot, boolean flatten, Locale locale, Map<String, String> options) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void decorateEntity(EdmEntitySet entitySet, EdmItem item, EdmItem originalQueryItem,
    List<OProperty<?>> props, boolean flatten, Locale locale, Map<String, String> options) {
  }

  @Override
  public EdmDocumentation getDocumentationForSchema(String namespace, String typeName) {
    return null;
  }

  @Override
  public List<EdmAnnotation<?>> getAnnotationsForSchema(String namespace, String typeName) {
    return null;
  }

  public static class ComplexAnnot {
    public ComplexAnnot(String a1, String a2) {
      this.annotprop1 = a1;
      this.annotprop2 = a2;
    }

    public String annotprop1;
    public String annotprop2;
  }

  @Override
  public List<EdmAnnotation<?>> getAnnotationsForEntityType(String namespace, String typeName) {
    List<EdmAnnotation<?>> a = null;
    if (typeName.equals("Airports")) {
      a = new ArrayList<EdmAnnotation<?>>();
      a.add(new EdmAnnotationAttribute(namespaceUri, prefix, "writable", "false"));
      a.add(EdmAnnotation.element(namespaceUri, prefix, "foo", ComplexAnnot.class, new ComplexAnnot("annotation one", "annotation two")));
    }
    return a;
  }

  @Override
  public EdmDocumentation getDocumentationForProperty(String namespace, String typename, String propName) {
    if (typename.equals("Airports")) {
      if (propName.equals("Code")) {
        return new EdmDocumentation("The FAA airport code", "the code..blah...blah");
      }
    }
    return null;
  }

  @Override
  public List<EdmAnnotation<?>> getAnnotationsForProperty(String namespace, String typeName, String propName) {
    List<EdmAnnotation<?>> a = null;
    if (typeName.equals("Airports")) {
      if (propName.equals("Code")) {
        a = new ArrayList<EdmAnnotation<?>>();
        a.add(new EdmAnnotationAttribute(namespaceUri, prefix, "localizedName", "Kode"));
        a.add(EdmAnnotation.element(namespaceUri, prefix, "perms", ComplexAnnot.class, new ComplexAnnot("prop annotation one", "prop annotation two")));
      }
    }
    return a;
  }
}
