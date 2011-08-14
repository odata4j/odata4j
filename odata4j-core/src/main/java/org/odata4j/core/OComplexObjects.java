package org.odata4j.core;

import java.util.Collections;
import java.util.List;
import org.core4j.Enumerable;
import org.odata4j.edm.EdmComplexType;

/**
 * A static factory to create immutable {@link OComplexObject} instances.
 */
public class OComplexObjects {

  private OComplexObjects() {}

  public static OComplexObject create(EdmComplexType type, List<OProperty<?>> properties) {
    return new OComplexObjectImpl(type, properties);
  }

  private static class OComplexObjectImpl implements OComplexObject {

    private final EdmComplexType complexType;
    private final List<OProperty<?>> properties;

    public OComplexObjectImpl(EdmComplexType complexType, List<OProperty<?>> properties) {
      if (complexType == null)
        throw new IllegalArgumentException("complexType cannot be null");

      this.complexType = complexType;
      this.properties = Collections.unmodifiableList(properties);
    }

    @Override
    public String toString() {
      return "OComplexObject[" + Enumerable.create(getProperties()).join(",") + "]";
    }

    @Override
    public List<OProperty<?>> getProperties() {
      return properties;
    }

    @Override
    public OProperty<?> getProperty(String propName) {
      return Enumerable.create(properties).first(OPredicates.propertyNameEquals(propName));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> OProperty<T> getProperty(String propName, Class<T> propClass) {
      return (OProperty<T>) getProperty(propName);
    }

    @Override
    public EdmComplexType getType() {
      return this.complexType;
    }

  }
}
