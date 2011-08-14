package org.odata4j.core;

import org.odata4j.edm.EdmType;
import org.odata4j.edm.EdmSimpleType;

/**
 * A static factory to create immutable {@link OSimpleObject} instances.
 */
public class OSimpleObjects {

  private OSimpleObjects() {}

  public static <V> OSimpleObject<V> create(V value, EdmSimpleType type) {
    return new OSimpleObjectImpl<V>(OProperties.simple(null, type, value, true));
  }

  private static class OSimpleObjectImpl<V> implements OSimpleObject<V> {

    private final OProperty<V> prop;

    public OSimpleObjectImpl(OProperty<V> prop) {
      this.prop = prop;
    }

    @Override
    public V getValue() {
      return prop.getValue();
    }

    @Override
    public EdmType getType() {
      return prop.getType();
    }
  }
  
}
