package org.odata4j.producer.jpa.northwind.test;

import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmFunctionParameter.Mode;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.ODataProducerDelegate;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.jpa.JPAProducer;

/**
 * a producer delegate that implements a few functions for testing purposes.
 * The function implementations are in no way JPA specific.
 */
public class NorthwindProducerWithFunctions extends ODataProducerDelegate {

  private final JPAProducer producer;

  public NorthwindProducerWithFunctions(JPAProducer p) {
    producer = p;
    extendModel();
  }

  @Override
  public ODataProducer getDelegate() {
    return producer;
  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport function, java.util.Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    if (function.getName().equals("TestFunction1")) {
      return testFunction1(function, params, queryInfo);
    } else if (function.getName().equals("TestFunction2")) {
      return testFunction2(function, params, queryInfo);
    } else {
      throw new RuntimeException("unknown function"); // TODO 404?
    }

  }

  private BaseResponse testFunction1(EdmFunctionImport function, java.util.Map<String, OFunctionParameter> params, QueryInfo queryInfo) {

    List<OProperty<?>> props = new ArrayList<OProperty<?>>(2);
    props.add(OProperties.int32("OrderID", 33));
    props.add(OProperties.int32("ProductID", 44));

    OComplexObject o = OComplexObjects.create(this.getMetadata().findEdmComplexType("NorthwindModel.Order_DetailsPK"), props);
    return Responses.complexObject(o);
  }

  private BaseResponse testFunction2(EdmFunctionImport function, java.util.Map<String, OFunctionParameter> params, QueryInfo queryInfo) {

    OFunctionParameter fp = params.get("NResults");
    if (null == fp) {
      throw new RuntimeException("missing parameter NResults");
    }

    Short nresults = (Short) ((OSimpleObject<?>) fp.getValue()).getValue();

    EdmComplexType ct = this.getMetadata().findEdmComplexType("NorthwindModel.Order_DetailsPK");
    OCollection.Builder<OComplexObject> c = OCollections.<OComplexObject> newBuilder(ct);

    for (int i = 0, orderid = 1, productid = 2; i < nresults; i++, orderid += 2, productid += 2) {
      List<OProperty<?>> props = new ArrayList<OProperty<?>>(2);
      props.add(OProperties.int32("OrderID", orderid));
      props.add(OProperties.int32("ProductID", productid));
      c = c.add(OComplexObjects.create(ct, props));
    }

    return Responses.collection(c.build());
  }

  private void extendModel() {
    // add some functions to the edm
    EdmDataServices ds = this.getMetadata();

    EdmSchema schema = ds.findSchema("NorthwindContainer");
    EdmEntityContainer container = schema.findEntityContainer("NorthwindEntities");

    EdmComplexType ct = ds.findEdmComplexType("NorthwindModel.Order_DetailsPK");
    List<EdmFunctionParameter> params = new ArrayList<EdmFunctionParameter>(15);
    params.add(new EdmFunctionParameter(
        "PBoolean", // String name,
        EdmSimpleType.BOOLEAN, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PByte", // String name,
        EdmSimpleType.BYTE, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PDateTime", // String name,
        EdmSimpleType.DATETIME, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PDateTimeOffset", // String name,
        EdmSimpleType.DATETIMEOFFSET, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PDecimal", // String name,
        EdmSimpleType.DECIMAL, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PDouble", // String name,
        EdmSimpleType.DOUBLE, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PGuid", // String name,
        EdmSimpleType.GUID, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PInt16", // String name,
        EdmSimpleType.INT16, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PInt32", // String name,
        EdmSimpleType.INT32, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PInt64", // String name,
        EdmSimpleType.INT64, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PSingle", // String name,
        EdmSimpleType.SINGLE, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PString", // String name,
        EdmSimpleType.STRING, // EdmBaseType type,
        Mode.IN));

    params.add(new EdmFunctionParameter(
        "PTime", // String name,
        EdmSimpleType.TIME, // EdmBaseType type,
        Mode.IN));

    EdmFunctionImport f = new EdmFunctionImport(
        "TestFunction1", //String name,
        null, // EdmEntitySet entitySet,
        ct, // EdmBaseType returnType,
        "GET", // String httpMethod,
        params); //List<EdmFunctionParameter> parameters)
    container.getFunctionImports().add(f);

    params = new ArrayList<EdmFunctionParameter>(1);
    params.add(new EdmFunctionParameter(
        "NResults", // String name,
        EdmSimpleType.INT16, // EdmBaseType type,
        Mode.IN));

    f = new EdmFunctionImport(
        "TestFunction2", //String name,
        null, // EdmEntitySet entitySet,
        new EdmCollectionType("Collection(" + ct.getFullyQualifiedTypeName() + ")", ct), // EdmBaseType returnType,
        "GET", // String httpMethod,
        params); //List<EdmFunctionParameter> parameters)
    container.getFunctionImports().add(f);
  }
}
