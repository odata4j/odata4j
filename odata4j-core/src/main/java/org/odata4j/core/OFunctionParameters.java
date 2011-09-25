package org.odata4j.core;

import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.Expression;
import org.odata4j.expression.ExpressionParser;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.producer.exceptions.NotImplementedException;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

/**
 * A static factory to create immutable {@link OFunctionParameter} instances.
 */
public class OFunctionParameters {

  private OFunctionParameters() {}

  /**
   * Creates a new OFunctionParameter, inferring the edm-type from the value provided, which cannot be null.
   * 
   * @param <T>  the property value's java-type
   * @param name  the property name
   * @param value  the property value
   * @return a new OData property instance
   */
  public static <T> OFunctionParameter create(String name, T value) {
    if (value == null)
      throw new IllegalArgumentException("Cannot infer EdmType if value is null");

    if (value instanceof OObject) {
      return new FunctionParameterImpl(name, (OObject) value);
    }

    EdmType type = EdmSimpleType.forJavaType(value.getClass());
    if (type == null) {
      // it is not a simple or complex property type.
      // TODO:
      // support: OEntity, OComplexObject, Collection<OEntity|OComplexObject|EdmSimpleType>
      throw new IllegalArgumentException("Cannot infer EdmType for java type: " + value.getClass().getName());
    }

    if (type instanceof EdmSimpleType) {
      return new FunctionParameterImpl(name, OSimpleObjects.create(value, (EdmSimpleType) type));
    } else {
      throw new IllegalArgumentException("type not supported for function parameter: " + type.getFullyQualifiedTypeName());
    }
  }

  public static OFunctionParameter parse(String name, EdmType type, String value) {
    if (type instanceof EdmSimpleType) {
      CommonExpression ce = ExpressionParser.parse(value);
      if (ce instanceof LiteralExpression) {
        // may have to case the literalValue based on type...
        Object val = convert(Expression.literalValue((LiteralExpression) ce), (EdmSimpleType) type);
        return new FunctionParameterImpl(name, OSimpleObjects.create(val, (EdmSimpleType) type));
      }
    }
    // TODO for other types
    throw new NotImplementedException();
  }

  private static Object convert(Object val, EdmSimpleType type) {

    Object v = val;
    if (type.equals(EdmSimpleType.INT16) && (!(val instanceof Short))) {
      // parser gave us an Integer
      v = new Short(((Number) val).shortValue());
    } else if (type.equals(EdmSimpleType.SINGLE) && (!(val instanceof Float))) {
      // parser gave us an Double
      v = new Float(((Number) val).floatValue());
    } else if (type.equals(EdmSimpleType.BYTE) && (!(val instanceof Byte))) {
      // parser gave us an Double
      v = new Byte(((Number) val).byteValue());
    }

    return v;
  }

  private static class FunctionParameterImpl implements OFunctionParameter {

    private final String name;
    private final OObject obj;

    public FunctionParameterImpl(String name, OObject obj) {
      this.name = name;
      this.obj = obj;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public EdmType getType() {
      return obj.getType();
    }

    @Override
    public OObject getValue() {
      return obj;
    }

    @Override
    public String toString() {
      Object value = this.getValue();
      if (value instanceof byte[]) {
        value = "0x" + Hex.encodeHexString((byte[]) value);
      }
      return String.format("OFunctionParameter[%s,%s,%s]", getName(), getType(), value);
    }
  }

  // TODO: this doesn't belong here..not sure where it goes yet...
  public static Class<? extends OObject> getResultClass(EdmType edmType) {
    // this prolly belongs elsewhere...TODO
    if (edmType instanceof EdmComplexType) {
      return OComplexObject.class;
    } else if (edmType instanceof EdmCollectionType) {
      return OCollection.class;
    } else if (edmType instanceof EdmEntityType) {
      return OEntity.class;
    } else {
      throw new NotImplementedException("function return type " + edmType.getFullyQualifiedTypeName() + " not supported");
    }
  }
}
