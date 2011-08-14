package org.odata4j.edm;

import java.util.List;

public class EdmComplexType extends EdmStructuralType {

  public EdmComplexType(String namespace, String name, List<EdmProperty> properties) {
    super(null, namespace, name, properties);
  }

  public static EdmComplexType create(String typeString) {
    int lastDot = typeString.lastIndexOf('.');
    String namespace = typeString.substring(0, lastDot);
    String name = typeString.substring(lastDot+1);
    return new EdmComplexType(namespace, name, null);
  }
  
  @Override
  public String toString() {
    return String.format("EdmComplexType[%s.%s]", namespace, name);
  }

}
