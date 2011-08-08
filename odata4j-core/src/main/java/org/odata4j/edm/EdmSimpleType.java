package org.odata4j.edm;

import java.util.Set;

/**
 * An EDMSimpleType
 */
public class EdmSimpleType extends EdmType {

  public EdmSimpleType(String fqTypeName, Set<Class<?>> javaTypes) {
    super(fqTypeName);
    this.javaTypes = javaTypes;
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  /**
  * Gets the java-types associated with this edm-type.  Only valid for simple types.
  *
  * @return the associated java-types.
  */
  @Override
  public Set<Class<?>> getJavaTypes() {
    return javaTypes;
  }

  private final Set<Class<?>> javaTypes;
}
