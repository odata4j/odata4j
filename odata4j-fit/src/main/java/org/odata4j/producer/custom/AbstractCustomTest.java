package org.odata4j.producer.custom;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OCollection;
import org.odata4j.core.OEntity;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperty;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmStructuralType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatType;

public abstract class AbstractCustomTest extends AbstractCustomTestBase {

  public AbstractCustomTest() {}

  @Test
  public void testPropertiesJSON() {
    dumpResourceJSON("Type1s");
    testProperties(FormatType.JSON);
  }

  @Test
  public void testPropertiesAtom() {
    // TODO when the xml parsers/writers support Bag properties
    // testProperties(FormatType.ATOM);
  }

  // TODO this probably belongs in a more comprehensize metadata test class...

  @Test
  public void testEdmxCreationTypeResolution() {

    // when producers create EdmDataServices, there should not be multiple
    // instances of an EdmType object representing the same type.
    checkForDups(producer.getMetadata());
    checkForDups(producer.getMetadataProducer().getMetadata());
  }

  private void checkForDups(EdmDataServices md) {
    for (EdmStructuralType st : md.getStructuralTypes()) {
      for (EdmProperty p : st.getDeclaredProperties()) {
        assertTrue(p.getDeclaringType() == st);
        checkType(p.getType(), md);
      }
    }

    for (EdmSchema s : md.getSchemas()) {
      for (EdmEntityContainer c : s.getEntityContainers()) {
        for (EdmFunctionImport f : c.getFunctionImports()) {
          checkType(f.getReturnType(), md);
          for (EdmFunctionParameter p : f.getParameters()) {
            checkType(p.getType(), md);
          }
        }
      }
    }
  }

  private void checkType(EdmType type, EdmDataServices md) {
    EdmType checkType = null;
    if (type.isSimple()) {
      assertTrue(type instanceof EdmSimpleType);
      checkType = EdmType.getSimple(type.getFullyQualifiedTypeName());
    } else if (type instanceof EdmComplexType) {
      checkType = md.findEdmComplexType(type.getFullyQualifiedTypeName());
    } else {
      // hmmh...EdmCollectionType is still a little weird...can't make this assertion
    }
    if (checkType != null) {
      assertTrue("two instances of type: " + checkType.getFullyQualifiedTypeName(), type == checkType);
    }
  }

  @Test
  public void testEdmxFormatParserTypeResolution() {
    // when consumers parse an edm, they should only create one type object
    // for each unique type.
    ODataConsumer c = createConsumer(FormatType.JSON);
    checkForDups(c.getMetadata());
  }

  @Test
  public void testEdmxBuilderContext() {
    dumpResource("$metadata", FormatType.JSON);
    // add some functions to the edm
    EdmDataServices.Builder ds = EdmDataServices.newBuilder(producer.getMetadata());

    EdmSchema.Builder schema = ds.findSchema("myns");
    EdmEntityContainer.Builder container = schema.findEntityContainer("Container1");

    EdmComplexType.Builder ct = ds.findEdmComplexType("myns.ComplexType1");
    List<EdmFunctionParameter.Builder> params = new ArrayList<EdmFunctionParameter.Builder>(15);
    params.add(EdmFunctionParameter.newBuilder().input("PBoolean", EdmSimpleType.BOOLEAN));
    params.add(EdmFunctionParameter.newBuilder().input("PByte", EdmSimpleType.BYTE));
    params.add(EdmFunctionParameter.newBuilder().input("PDateTime", EdmSimpleType.DATETIME));
    params.add(EdmFunctionParameter.newBuilder().input("PDateTimeOffset", EdmSimpleType.DATETIMEOFFSET));
    params.add(EdmFunctionParameter.newBuilder().input("PDecimal", EdmSimpleType.DECIMAL));
    params.add(EdmFunctionParameter.newBuilder().input("PDouble", EdmSimpleType.DOUBLE));
    params.add(EdmFunctionParameter.newBuilder().input("PGuid", EdmSimpleType.GUID));
    params.add(EdmFunctionParameter.newBuilder().input("PInt16", EdmSimpleType.INT16));
    params.add(EdmFunctionParameter.newBuilder().input("PInt32", EdmSimpleType.INT32));
    params.add(EdmFunctionParameter.newBuilder().input("PInt64", EdmSimpleType.INT64));
    params.add(EdmFunctionParameter.newBuilder().input("PSingle", EdmSimpleType.SINGLE));
    params.add(EdmFunctionParameter.newBuilder().input("PString", EdmSimpleType.STRING));
    params.add(EdmFunctionParameter.newBuilder().input("PTime", EdmSimpleType.TIME));

    EdmFunctionImport.Builder f = EdmFunctionImport.newBuilder()
        .setName("TestFunction1")
        .setReturnType(ct.build())
        .setHttpMethod("GET")
        .addParameters(params);
    container.addFunctionImports(f);

    params = new ArrayList<EdmFunctionParameter.Builder>(1);
    params.add(EdmFunctionParameter.newBuilder().input("NResults", EdmSimpleType.INT16));

    f = EdmFunctionImport.newBuilder()
        .setName("TestFunction2")
        .setReturnType(new EdmCollectionType(CollectionKind.Collection, ct.build()))
        .setHttpMethod("GET")
        .addParameters(params);
    container.addFunctionImports(f);
    checkForDups(ds.build());
  }

  @SuppressWarnings("unchecked")
  private void testProperties(FormatType format) {
    ODataConsumer c = createConsumer(format);

    OEntity e = c.getEntity("Type1s", "0").execute();

    checkCollection(e.getProperty("EmptyStrings"), EdmSimpleType.STRING, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return null;
      }

      @Override
      public int getNExpected() {
        return 0;
      }

    });

    checkCollection(e.getProperty("BagOStrings"), EdmSimpleType.STRING, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return "bagstring-" + idx;
      }

      @Override
      public int getNExpected() {
        return 3;
      }

    });

    checkCollection(e.getProperty("ListOStrings"), EdmSimpleType.STRING, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return "liststring-" + idx;
      }

      @Override
      public int getNExpected() {
        return 5;
      }

    });

    checkCollection(e.getProperty("BagOInts"), EdmSimpleType.INT32, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return idx;
      }

      @Override
      public int getNExpected() {
        return 5;
      }

    });

    OProperty<?> cx = e.getProperty("Complex1");
    assertTrue(cx.getType().getFullyQualifiedTypeName().equals("myns.ComplexType1"));
    List<OProperty<?>> props = (List<OProperty<?>>) cx.getValue(); // uggh...why isn't this an OComplexObject?
    assertTrue(props.size() == 2);
    OProperty<?> prop = findProp("Prop1", props);
    assertTrue(null != prop);
    assertTrue(prop.getValue() instanceof String);
    assertTrue(((String) prop.getValue()).equals("Val1"));
    prop = findProp("Prop2", props);
    assertTrue(null != prop);
    assertTrue(prop.getValue() instanceof String);
    assertTrue(((String) prop.getValue()).equals("Val2"));

    OProperty<?> ccx = e.getProperty("ListOComplex");
    assertTrue(ccx.getType() instanceof EdmCollectionType);
    EdmCollectionType ct = (EdmCollectionType) ccx.getType();
    assertTrue(ct.getItemType().getFullyQualifiedTypeName().equals("myns.ComplexType1"));
    assertTrue(((OCollection<?>) ccx.getValue()).size() == 2);
  }

  private OProperty<?> findProp(String name, List<OProperty<?>> props) {
    for (OProperty<?> p : props) {
      if (name.equals(p.getName())) {
        return p;
      }
    }
    return null;
  }

  private static interface ValueGenerator {
    int getNExpected();

    Object getValue(int idx);
  }

  @SuppressWarnings("unchecked")
  private void checkCollection(OProperty<?> prop, EdmType itemType, ValueGenerator vg) {
    //OProperty<?> prop = e.getProperty("BagOStrings");
    assertTrue(null != prop);
    assertTrue(prop.getType() instanceof EdmCollectionType);
    EdmCollectionType ct = (EdmCollectionType) prop.getType();
    assertTrue(ct.getItemType().equals(itemType));
    OCollection<? extends OObject> coll = (OCollection<? extends OObject>) prop.getValue();
    assertTrue(coll.size() == vg.getNExpected());
    int idx = 0;
    for (OObject obj : coll) {
      assertTrue(obj.getType().equals(itemType));
      assertTrue(((OSimpleObject<?>) obj).getValue().equals(vg.getValue(idx)));
      idx += 1;
    }
  }
}
